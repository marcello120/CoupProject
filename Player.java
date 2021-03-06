package com.coup;

import java.util.*;

/**
 * Created by Sarosi on 24/10/2017.
 */
public class Player implements Cloneable {
    public boolean random;

    private Random r = new Random();
    private Hand hand;
    public int number;
    int coins;
    public Hand originalHand;
    public Game containingGame;
    private ArrayList<MindPlayer> enemies;
    private MindPlayer me;
    private ArrayList<String> fromExchange = new ArrayList<>();
    public ArrayList<String> fromshuffle = new ArrayList<>();
    public String lastshuffled;
    public ArrayList<String> known = new ArrayList<>();
    public int aggression = 0;
    public double suspicion;
    private int playernum;


    public ArrayList<String> cardsClaimed = new ArrayList<>();
    private ArrayList<String> cardsNotClaimed = new ArrayList<>();
    private Player assassinationTarget = this;
    private Player stealTarget;
    private ArrayList<String> fishFor = new ArrayList<>();


    public Player(int number, int playernum, Game g) {
        if (number%2==0){
            System.out.println("RANDOM");
            random = true;
        }
        this.number = number;
        me = new MindPlayer(number, playernum, this);
        initEnemies(number, playernum);
        containingGame = g;
        suspicion = 11 + (13 - 11) * r.nextDouble();
        this.playernum = playernum;
    }

    public void initEnemies(int number, int playernum) {
        enemies = new ArrayList<>();
        for (int i = 0; i < playernum; i++) {
            if (i != number) {
                enemies.add(new MindPlayer(i, playernum, this));
            }
        }
    }

    public Player getTarget(Integer targetnum){

        if (targetnum != null){
            return containingGame.getPlayerByNumber(targetnum);
        }else{
         return containingGame.getPlayerByNumber(((MindPlayer) gatherThreats().keySet().toArray()[0]).getNumber());
        }
    }



    public boolean isThreatTooHigh() {
        //is RandWin level too high and is it possible to reduce it below an other player's level.

        // calculate possible reduction

        ArrayList<Integer> listOfCoins = new ArrayList<>();
        for (MindPlayer mp: enemies) {
            if (containingGame.getPlayerByNumber(mp.getNumber())!=null){
               int coin= containingGame.getPlayerByNumber(mp.getNumber()).coins;
               listOfCoins.add(coins);
            }
        }
        listOfCoins.sort(Collections.reverseOrder());

        if (listOfCoins.get(0) <= coins-3 && coins<7){
            return false;
        }



        if (ownThreat() > averagethreats() * 1.5) {
            return true;
        }
        int maxcoins = 0;
        int maxinfluence = 0;
        for (Player p : containingGame.players) {
            if (p != this) {
                if (p.coins > maxcoins) {
                    maxcoins = p.coins;
                }
                if (p.getHand().getInfluence() > maxinfluence) {
                    maxinfluence = p.getHand().getInfluence();
                }
            }
        }
        if (maxinfluence < getHand().getInfluence() && listOfCoins.get(0) > 6) {
            return true;
        }
        if (maxcoins < coins && coins > 6) {
            return true;
        }
        double maxthreat = 0;
        for (Double d : gatherThreats().values()) {
            if (d > maxthreat) {
                maxthreat = d;
            }
        }
        return maxthreat < ownThreat() && maxcoins > 7;

    }

    private ArrayList<Integer> choiceList() {

        //prep random choice;
        int fist = 1;
        int second = 2;
        int third = 3;

        ArrayList<Integer> choiceList = new ArrayList<>();

        for (int i = 0; i < 85; i++) {
            choiceList.add(fist);
        }
        for (int i = 0; i < 10; i++) {
            choiceList.add(second);
        }
        for (int i = 0; i < 5; i++) {
            choiceList.add(third);
        }
        Collections.shuffle(choiceList);

        return choiceList;
    }

    private Event spendCoins() {
        if (coins > 7) {
            return new Event(this, "Coup",getTarget(null), "-");
        } else if (coins >= 3 && (safeGeneral("Assassin", null))) {
            cardsClaimed.add("Assassin");
            return new Event(this, "Assassinate", assassinationTarget, "Assassin");
        } else {
            return new Event(this, "NEGATIVE", this, "-");
        }
    }

    private Event prefForCoup() {
        if (safeGeneral("Duke", null)) {
            return new Event(this, "Tax", this, "-");
        }
        if (safeGeneral("Captain", null)) {
            return new Event(this, "Steal", stealTarget, "-");
        }
        if (safeGeneral("ForeignAid", null)) {
            return new Event(this, "ForeignAid", this, "-");
        }
        return lowTempo();
    }

    private Event thisIsPressure(int targetnum) {
        //this is pressure
        if (choiceList().get(0) == 1) {
            if (safeGeneral("Captain", containingGame.getPlayerByNumber(targetnum))) {
                cardsClaimed.add("Captain");
                return new Event(this, "Steal", containingGame.getPlayerByNumber(targetnum), "Captain");
            } else if (cardsAndCounters() && safeGeneral("Ambassador", null)) {
                cardsClaimed.add("Ambassador");
                return new Event(this, "Exchange", this, "Ambassador");
                //prepcoup
            }
        } else if (choiceList().get(0) == 2) {
            if (cardsAndCounters() && safeGeneral("Ambassador", null)) {
                cardsClaimed.add("Ambassador");
                return new Event(this, "Exchange", this, "Ambassador");
                //prepcoup
            }
        }
        return prefForCoup();
    }


    private Event riskyplay() {
        Player target = getTarget(null);
        if (coins >= 3 && !(Collections.frequency(containingGame.getTableString(), "Assassin") == 3) && !cardsNotClaimed.contains("Assassin") && !getMindplayerbyNumber(target.getNumber()).getCardsClaimed().contains("Contessa")) {
            cardsClaimed.add("Assassin");
            return new Event(this, "Assassinate", target, "Assassin");
        }
        if (!(Collections.frequency(containingGame.getTableString(), "Captain") == 3) && !cardsNotClaimed.contains("Captain") && !getMindplayerbyNumber(target.getNumber()).getCardsClaimed().contains("Captain")) {
            cardsClaimed.add("Captain");
            return new Event(this, "Steal", target, "Captain");
        } else {
            cardsClaimed.add("Duke");
            return new Event(this, "Tax", target, "Duke");
        }
    }

    private Event fakeAssassinate() {
        for (int i = 0; i < containingGame.players.size() - 1; i++) {
            Player target = containingGame.getPlayerByNumber(((MindPlayer) gatherThreats().keySet().toArray()[i]).getNumber());
            if (getMindplayerbyNumber(target.getNumber()).getCardsClaimed().contains("Contessa")) {
                return new Event(this, "Assassinate", target, "Assassin");
            }
        }
        cardsNotClaimed.add("Duke");
        return new Event(this, "Income", this, "-");
    }


    public Event askDo() {
        if (coins > 10) {
            Player target = getTarget(null);
            return new Event(this, "Coup", target, "-");
        }
        Integer threat = assessThreats();
        if (threat==null && containingGame.players.size()==2){
            threat = containingGame.getOnlyEnemy(this).getNumber();
        }
        if(coins > 6 && (hand.getInfluence() == 1 || imminentThreat())){
           return new Event(this, "Coup",getTarget(null), "-");
        }
        if (threat == null) {
            containingGame.threatcount++;
            if (isThreatTooHigh() && containingGame.players.size()!=2) {
                //own RandWin is too high
                if (r.nextInt(100) > 90) {
                    //2
                    return riskyplay();
                } else {
                    //1
                    Event spendCoinsEvent = spendCoins();
                    if (!Objects.equals(spendCoinsEvent.getAction(), "NEGATIVE")) {
                        return spendCoinsEvent;
                    }
                    if (imminentThreat()) {
                        //relinguish adventage
                        if (choiceList().get(0) == 2) {
                            for (Player p : containingGame.players) {
                                if (p.coins >= 7) {
                                    return fakeAssassinate();
                                }
                            }
                        } else if (choiceList().get(0) == 3) {
                            return riskyplay();
                        }
                        return lowTempo();
                    } else {
                        if (cardsAndCounters()) {
                            if (safeGeneral("Ambassador", null) || hand.contains("Ambassador")) {
                                cardsClaimed.add("Ambassador");
                                return new Event(this, "Exchange", this, "Ambassador");
                            }
                        }
                        cardsNotClaimed.add("Duke");
                        return new Event(this, "Income", this, "-");
                    }
                }
            } else {
                //RandWin level is fine
                int exchangechance =  10;
                if (hand.getInfluence()==2){
                    if (Objects.equals(hand.getCardTwo().getName(), hand.getCardOne().getName()) && !hand.contains("Duke")){
                        exchangechance= 90;
                    }
                }
                if ((r.nextInt(100) < exchangechance)) {
                    if (cardsAndCounters()) {
                        if (safeGeneral("Ambassador", null)) {
                            cardsClaimed.add("Ambassador");
                            return new Event(this, "Exchange", this, "Ambassador");
                        }
                    }
                    if (safeGeneral("Duke", null)) {
                        return new Event(this, "Tax", this, "-");
                    }
                    if (safeGeneral("Captain", null)) {
                        return new Event(this, "Steal", stealTarget, "-");
                    }
                    if (safeGeneral("ForeignAid", null)) {
                        return new Event(this, "ForeignAid", this, "-");
                    }
                    cardsClaimed.add("Duke");
                    return new Event(this, "Income", this, "-");
                } else {
                    return prefForCoup();
                }
            }
        } else {
            containingGame.nothreatcount++;
            if (imminentThreat()) {
                //now RandWin
                if (choiceList().get(0) == 1) {
                    if (coins >= 7) {
                        return new Event(this, "Coup", containingGame.getPlayerByNumber(threat), "-");
                    } else if (safeGeneral("Assassin", containingGame.getPlayerByNumber(threat))) {
                        cardsClaimed.add("Assassin");
                        return new Event(this, "Assassinate", containingGame.getPlayerByNumber(threat), "Assassin");
                    } else {
                        return thisIsPressure(threat);
                    }
                } else if (choiceList().get(0) == 2) {
                    if (coins >= 3 && (safeGeneral("Assassin", containingGame.getPlayerByNumber(threat)))) {
                        cardsClaimed.add("Assassin");
                        return new Event(this, "Assassinate", assassinationTarget, "Assassin");
                    } else {
                        return thisIsPressure(threat);
                    }
                } else {
                    return thisIsPressure(threat);
                }
            }
            return thisIsPressure(threat);
        }
    }

    private Event lowTempo() {
        if (r.nextInt(100) > 90) {
            cardsNotClaimed.add("Duke");
            return new Event(this, "Income", this, "-");
        }
        if (safeGeneral("Ambassador", null)) {
            cardsClaimed.add("Ambassador");
            return new Event(this, "Exchange", this, "Ambassador");
        } else {
            cardsNotClaimed.add("Duke");
            return new Event(this, "Income", this, "-");
        }
    }

    private boolean cardsAndCounters() {
        boolean returningBoolean = false;
        if(hand.getInfluence()==2) {
            if (hand.getCardOne() == hand.getCardTwo() && !hand.contains("Duke")) {
                returningBoolean = true;
            }
        }
        for (MindPlayer mp : enemies) {
            if (containingGame.getPlayerByNumber(mp.getNumber()) != null) {
                if ((mp.getCardsClaimed().contains("Assassin") || mp.getCardsStrategy().contains("Assassin")) && !getHand().contains("Contessa")) {
                    returningBoolean = true;
                    fishFor.add("Contessa");
                }
                if ((mp.getCardsClaimed().contains("Captain") || mp.getCardsStrategy().contains("Captain")) && !getHand().contains("Ambassador") && !getHand().contains("Captain")) {
                    returningBoolean = true;
                    fishFor.add("Ambassador");
                    fishFor.add("Captain");
                }
                if ((mp.getCardsClaimed().contains("Duke") || mp.getCardsStrategy().contains("Duke")) && !getHand().contains("Captain") && !getHand().contains("Duke")) {
                    returningBoolean = true;
                    fishFor.add("Duke");
                    fishFor.add("Captain");
                }
            }
        }
        return returningBoolean;
    }


    private boolean imminentThreat() {
        for (MindPlayer mp : enemies) {
            if (containingGame.getPlayerByNumber(mp.getNumber()) != null) {
                if (mp.getCardsClaimed().contains("Assassin") || containingGame.getPlayerByNumber(mp.getNumber()).coins >= 7 || (mp.getCardsClaimed().contains("Assassasin") && !hand.contains("Contessa") && isThreatTooHigh())){
                    return true;
                }
            }
        }
        return containingGame.players.size() == 2 && safeGeneral("Assassin", null);
    }

    private boolean safeGeneral(String card, Player p) {
        if (Objects.equals(card, "ForeignAid")) {
            return safeSpecific(card, p);
        }
        if (hand.contains(card)) {
            return safeSpecific(card, p);
        }
        if (cardsNotClaimed.contains(card)) {
            return false;
        }
        if (Collections.frequency(containingGame.getTableString(), card) == 3) {
            return false;
        }

        if (hand.getInfluence()==2){
            if (Objects.equals(hand.getCardOne().getName(), hand.getCardTwo().getName()) && !hand.contains("Duke")){
                if (Objects.equals(card, "Ambassador")){
                    return true;
                }
            }
        }

        int claimedCardsAdded = 0;
        for (MindPlayer mp : enemies) {
            if (mp.getCardsClaimed().contains(card)) {
                claimedCardsAdded++;
            }
        }
        claimedCardsAdded += Collections.frequency(containingGame.getTableString(), card);
        if (claimedCardsAdded >= 3) {
            return false;
        } else {
            if (cardsClaimed.contains(card)) {
                return safeSpecific(card, p);
            }
            //free card
            for (MindPlayer mp : enemies) {
                if (containingGame.getPlayerByNumber(mp.getNumber()) != null) {
                    if (mp.getCardsStrategy().contains(card) && mp.getProb(card) > 30 && !mp.getCardsClaimed().contains(card)) {
                        claimedCardsAdded++;
                    }
                }
            }
            if (claimedCardsAdded > 3) {
                //probably all cards are accounted for
                return false;
            } else {
                return safeSpecific(card, p);
            }
        }
    }


    private Boolean safeSpecific(String card, Player p) {
        if (Objects.equals(card, "Ambassador")) {
            return true;
        }
        if (Objects.equals(card, "Duke")) {
            if (containingGame.log.getEventsforPlayer(number).contains("Income")) {
                return false;
            }
            return !(hand.contains("Captain") && safeSpecific("Captain", null) && !cardsClaimed.contains("Duke"));
        }
        if (Objects.equals(card, "Assassin")) {
            if (coins < 3) {
                return false;
            }
            if (Collections.frequency(containingGame.getTableString(), "Contessa") + Collections.frequency(hand.getHandStringList(), "Contessa") == 3) {
                if (p == null){
                assassinationTarget = getTarget(null);
                }
                return true;
            }
            if (p == null) {
                for (MindPlayer mp : enemies) {
                    if (containingGame.getPlayerByNumber(mp.getNumber()) != null) {
                        if (mp.getCardsNotClaimed().contains("Contessa")){
                            assassinationTarget = containingGame.getPlayerByNumber(mp.getNumber());
                            return true;
                        }

                        if (mp.getCardsClaimed().size() >= containingGame.getPlayerByNumber(mp.getNumber()).hand.getInfluence() && !mp.getCardsClaimed().contains("Contessa") && mp.getProb("Contessa") < 40) {
                            assassinationTarget = containingGame.getPlayerByNumber(mp.getNumber());
                            return true;
                        }
                        if (!mp.getCardsClaimed().contains("Contessa") && mp.getProb("Contessa") < 40 && r.nextInt(100) < 20){
                            assassinationTarget = containingGame.getPlayerByNumber(mp.getNumber());
                            return true;
                        }
                    }
                }
            } else {
                if (getMindplayerbyNumber(p.getNumber()).getCardsNotClaimed().contains("Contessa") && !getMindplayerbyNumber(p.getNumber()).getCardsStrategy().contains("Contessa")) {
                    return true;
                }
                if (getMindplayerbyNumber(p.getNumber()).getCardsClaimed().size() >= containingGame.getPlayerByNumber(getMindplayerbyNumber(p.getNumber()).getNumber()).hand.getInfluence() && !getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Contessa") && getMindplayerbyNumber(p.getNumber()).getProb("Contessa") < 40) {
                    return true;
                }
                if (!getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Contessa") && getMindplayerbyNumber(p.getNumber()).getProb("Contessa") < 40 && r.nextInt(100) < 20){
                    return true;
                }
            }
        }
        if (Objects.equals(card, "ForeignAid")) {
            if (containingGame.turnNumber > 2) {
                for (MindPlayer mp : enemies) {
                    if (mp.getCardsClaimed().contains("Duke")) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        if (Objects.equals(card, "Captain")) {
            if (Collections.frequency(containingGame.getTableString(), "Ambassador") + Collections.frequency(hand.getHandStringList(), "Ambassador") == 3) {
                if (Collections.frequency(containingGame.getTableString(), "Captain") + Collections.frequency(hand.getHandStringList(), "Captain") == 3) {
                    stealTarget = getTarget(null);
                    return true;
                }
            }
            if (p == null) {
                int claimedCaptain = 0;
                int claimedAmbassador = 0;
                for (MindPlayer mp : enemies) {
                    if (containingGame.getPlayerByNumber(mp.getNumber()) != null) {
                        if (mp.getCardsNotClaimed().contains("Ambassador") || mp.getCardsNotClaimed().contains("Captain") && containingGame.getPlayerByNumber(mp.getNumber()).coins >0) {
                            stealTarget = containingGame.getPlayerByNumber(mp.getNumber());
                            return true;
                        }
                        if (mp.getCardsClaimed().size() >= containingGame.getPlayerByNumber(mp.getNumber()).hand.getInfluence() && !mp.getCardsClaimed().contains("Captain") && !mp.getCardsClaimed().contains("Ambassador") && mp.getProb("Ambassador") < 40 && mp.getProb("Captain") < 40 && containingGame.getPlayerByNumber(mp.getNumber()).coins >0) {
                            stealTarget = containingGame.getPlayerByNumber(mp.getNumber());
                            return true;
                        }
                        if (mp.getCardsClaimed().contains("Ambassador")) {
                            claimedAmbassador++;
                        }
                        if (mp.getCardsClaimed().contains("Captain")) {
                            claimedCaptain++;
                        }
                        if (!mp.getCardsClaimed().contains("Ambassador") && !mp.getCardsClaimed().contains("Captain") && mp.getProb("Captain")<40 && mp.getProb("Captain")<40 && r.nextInt(100)<20 && containingGame.getPlayerByNumber(mp.getNumber()).coins >0){
                            stealTarget = containingGame.getPlayerByNumber(mp.getNumber());
                            return true;
                        }
                    }
                }
                int countersout = claimedAmbassador + claimedCaptain + Collections.frequency(containingGame.table, "Ambassador") + Collections.frequency(containingGame.table, "Captain");

                if (countersout > 4) {
                    for (MindPlayer mp : enemies) {
                        if (containingGame.getPlayerByNumber(mp.getNumber()) != null) {
                            if (!mp.getCardsClaimed().contains("Ambassador") && !mp.getCardsClaimed().contains("Captain") && mp.getProb("Ambassador") < 40 && mp.getProb("Captain") < 40 && containingGame.getPlayerByNumber(mp.getNumber()).coins >0 ){
                                stealTarget = containingGame.getPlayerByNumber(mp.getNumber());
                                return true;
                            }
                        }
                    }
                }
            } else {

                if (getMindplayerbyNumber(p.getNumber()).getCardsNotClaimed().contains("Ambassador") && getMindplayerbyNumber(p.getNumber()).getCardsNotClaimed().contains("Captain")) {
                    return true;

                }
                if (getMindplayerbyNumber(p.getNumber()).getCardsClaimed().size() >= containingGame.getPlayerByNumber(getMindplayerbyNumber(p.getNumber()).getNumber()).hand.getInfluence() && !getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Captain") && !getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Ambassador") && getMindplayerbyNumber(p.getNumber()).getProb("Captain") < 40 && getMindplayerbyNumber(p.getNumber()).getProb("Ambassador") < 40) {
                    return true;
                }
            }


        }
        return false;
    }


    public void update(Event e) {
        if (Objects.equals(e.getAction(), "UnsuccessfullyChallenged") && e.getTarget()!=this){
            getMindplayerbyNumber(e.getTarget().getNumber()).addHonest();
        }
        if (e.getAction().contains("Trying") && !Objects.equals(e.getCard(), "-") && e.getOrigin() != this) {
            getMindplayerbyNumber(e.getOrigin().getNumber()).addToClaimed(e.getCard());
        }
        if ((Objects.equals(e.getAction(), "Income") || Objects.equals(e.getAction(), "ForeignAid")) && e.getTarget() != this) {
            getMindplayerbyNumber(e.getOrigin().getNumber()).addNotClaimedCard("Duke");
        }
        if ((Objects.equals(e.getAction(), "Steal")) && e.getTarget() != this) {
            getMindplayerbyNumber(e.getTarget().getNumber()).addNotClaimedCard("Ambassador");
            getMindplayerbyNumber(e.getTarget().getNumber()).addNotClaimedCard("Captain");
        }
        if ((Objects.equals(e.getAction(), "Assassinate")) && e.getTarget() != this) {
            getMindplayerbyNumber(e.getTarget().getNumber()).addNotClaimedCard("Contessa");
        }
        if (Objects.equals(e.getAction(), "Exchange") && e.getTarget() != this) {
            getMindplayerbyNumber(e.getOrigin().getNumber()).clearKnown();
            getMindplayerbyNumber(e.getOrigin().getNumber()).clearKnown();
        }
        if (Objects.equals(e.getAction(), "RevealedCard") && e.getOrigin() != this) {
            getMindplayerbyNumber(e.getOrigin().getNumber()).addtoKnow(e.getCard());
            getMindplayerbyNumber(e.getOrigin().getNumber()).clearKnown();
        }
        if ((Objects.equals(e.getAction(), "Steal")) && e.getTarget() == this) {
            cardsClaimed.removeAll(Collections.singleton("Ambassador"));
            cardsClaimed.removeAll(Collections.singleton("Captain"));
            cardsNotClaimed.add("Ambassador");
            cardsNotClaimed.add("Captain");
        }
        if ((Objects.equals(e.getAction(), "Assassinate")) && e.getTarget() == this) {
            cardsClaimed.removeAll(Collections.singleton(e.getCard()));
            cardsNotClaimed.add("Contessa");
        }
        if ((Objects.equals(e.getAction(), "Income")) && e.getTarget() == this) {
            cardsNotClaimed.add("Duke");
        }
        if ((Objects.equals(e.getAction(), "SuccessfullyChallenged")) && e.getTarget() == this) {
            cardsNotClaimed.add(e.getCard());
            cardsClaimed.removeAll(Collections.singleton(e.getCard()));
        }
        if (e.getAction().contains("Trying") && !Objects.equals(e.getCard(), "-") && e.getOrigin() == this) {
            cardsClaimed.add(e.getCard());
        }
        if (e.getAction().contains("PutToTable") || e.getAction().contains("RevealedCard") && e.getOrigin() == this) {
            cardsClaimed.removeAll(Collections.singleton(e.getCard()));
            if (!hand.contains(e.getCard())){
                cardsNotClaimed.add(e.getCard());
            }
        }
    }

    public ArrayList<String> updateKnown() {
        known.clear();
        known.addAll(fromExchange);
        known.addAll(fromshuffle);
        known.addAll(containingGame.getTableString());
        known.addAll(getHand().getHandStringList());
        return known;
    }

    //check if based on known cards its possible for the enemy to hold the card
    public boolean MathEliminate(Event e) {
        return getMindplayerbyNumber(e.getOrigin().getNumber()).canHave(e.getCard());
    }


    public Player() {
    }

    public Card loseCard() {
        //choice
        if (getHand().getInfluence() == 2) {
            //choice
        ArrayList<Card> cardList = sortCards(getHand().getHandList());
                Card ditch = cardList.get(1);
            if (ditch == hand.getCardOne()) {
                Card ret = getHand().getCardOne();
                getHand().setCardOne(null);
                return ret;
            } else {
                Card ret = getHand().getCardTwo();
                getHand().setCardTwo(null);
                return ret;
            }
        } else {
            return getHand().getHandList().get(0);
        }
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        coins = coins + amount;
    }

    public void removeCoins(int amount) {
        coins = coins - amount;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public int getNumber() {
        return number;
    }


    @Override
    public String toString() {
        return "Player{" +
                "hand=" + hand +
                ", number=" + number +
                '}';
    }

    private ArrayList<Card> sortCards(ArrayList<Card> cardlist) {
        HashMap<Card, Integer> mapOfCards = new HashMap<>();
        for (Card c : cardlist) {
            if (Objects.equals(c.getName(), "Duke")) {
                mapOfCards.put(c, 0 - Collections.frequency(fishFor, "Duke"));
            }
            if (Objects.equals(c.getName(), "Assassin")) {
                int rank = 1 - Collections.frequency(fishFor, "Assassin");
                if (containingGame.players.size() == 2 && !getMindplayerbyNumber(containingGame.getOnlyEnemy(this).number).getCardsClaimed().contains("Contessa") && !getMindplayerbyNumber(containingGame.getOnlyEnemy(this).number).getStrategy().contains("Contessa") ){
                    rank = 0;
                }
                mapOfCards.put(c, rank);
            }
            if (Objects.equals(c.getName(), "Captain")) {
                int rank = 2;
                if (containingGame.turnNumber <1 ){
                    rank = 1;
                }
                mapOfCards.put(c,rank - Collections.frequency(fishFor, "Captain"));
            }
            if (Objects.equals(c.getName(), "Contessa")) {
                mapOfCards.put(c, 3 - Collections.frequency(fishFor, "Contessa"));
            }
            if (Objects.equals(c.getName(), "Ambassador")) {
                mapOfCards.put(c, 4 - Collections.frequency(fishFor, "Ambassador"));
            }
        }
        Map<Card, Integer> result = new LinkedHashMap<>();
        mapOfCards.entrySet().stream()
                .sorted(Map.Entry.<Card, Integer>comparingByValue().reversed())
                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));

        ArrayList<Card> newestCards = new ArrayList<Card>(result.keySet());

        Collections.reverse(newestCards);
        return newestCards;

    }

    public ArrayList<Card> exchange(Card draw, Card draw1) {
        //choice
        ArrayList<Card> options = new ArrayList<>();
        Card tobe;
        Card tobe1;
        Card tobe2;
        options.add(draw);
        options.add(draw1);
        removefromKnown(draw1.getName());
        removefromKnown(draw.getName());
        int cardsToFill = getHand().getInfluence();
        ArrayList<Card> keep = new ArrayList<>();
        for (int i = 0; i < hand.getHandList().size(); i++) {
            options.add(hand.getHandList().get(i));
        }
        ArrayList<Card> options2 = new ArrayList<>(options);
        ArrayList<Card> pref = new ArrayList<>();
        for (Card c : options) {
            if (fishFor.contains(c.getName()) && !pref.contains(c)) {
                pref.add(c);
            }
        }
        pref = sortCards(pref);
        options2 = sortCards(options2);
        keep.addAll(pref);
        options2.removeAll(pref);
        keep.addAll(options2);
        if (hand.getInfluence() == 1) {
            if (hand.getCardOne() == null) {
                tobe = keep.remove(0);
                options.remove(tobe);
                hand.setCardTwo(tobe);
                fishFor.removeIf(card -> Objects.equals(card, getHand().getCardTwo().getName()));
            } else {
                tobe = keep.remove(0);
                options.remove(tobe);
                hand.setCardOne(tobe);
                fishFor.removeIf(card -> Objects.equals(card, getHand().getCardOne().getName()));
            }
        } else {
            tobe1 = keep.get(0);
            keep.removeIf(card -> Objects.equals(card.getName(), tobe1.getName()));
            tobe2 = keep.remove(0);
            options.remove(tobe1);
            options.remove(tobe2);
            hand.setCardOne(tobe1);
            hand.setCardTwo(tobe2);
            fishFor.removeIf(card -> Objects.equals(card, getHand().getCardOne().getName()));
            fishFor.removeIf(card -> Objects.equals(card, getHand().getCardTwo().getName()));
        }
        addToAllKnowns(options.get(0).getName());
        addToAllKnowns(options.get(1).getName());
        cardsClaimed.clear();
        cardsNotClaimed.clear();
        return options;

    }

    public boolean askBlockAid(Player p) {
        return (getHand().contains("Duke") || cardsClaimed.contains("Duke") || containingGame.turnNumber < 1) && !cardsNotClaimed.contains("Duke");
    }

    public boolean askBlockAssassinWithContessa(Player origin) {
        if (hand.getInfluence() == 1) {
            return true;
        }
        return getHand().contains("Contessa") || cardsClaimed.contains("Contessa");
    }

    public String askBlockSteal(Player origin) {
        if (coins == 0){
            return null;
        }
        if (getHand().contains("Ambassador") || cardsClaimed.contains("Ambassador") ) {
            return "Ambassador";
        } else if (getHand().contains("Captain") || cardsClaimed.contains("Captain")) {
            return "Captain";
        }
        return null;
    }

    public String askChallenge(Event e) {
        double modifier = suspicion;
        double challangeChance = aggression;

        if (e.getTarget().number == number) {
            if (Objects.equals(e.getAction(), "Assassinate") && hand.contains("Contessa")) {
                return null;
            }
            if (Objects.equals(e.getAction(), "Steal") && (hand.contains("Ambassador") || hand.contains("Captain"))) {
                return null;
            }
            if (Objects.equals(e.getAction(), "Steal") && coins == 0) {
                return null;
            }
        }

        if (getHand().getInfluence() == 1) {
            modifier -= 1;
        }

        //up numbers if near death or near coup or if negatively effected
        if (e.getTarget().getNumber() == number) {
            modifier += 2.5;
            //stay alive
            if (Objects.equals(e.getAction(), "Assassinate") && hand.getInfluence() == 1 && !cardsClaimed.contains("Contessa")) {
                return e.getCard();
            }
            if (containingGame.players.size() == 2 && containingGame.getOnlyEnemy(this).coins >= 5 && coins < 9 && Objects.equals(e.getAction(), "Steal") && !hand.contains("Assassin")) {
                return e.getCard();
            }
            if (containingGame.players.size() == 2 && containingGame.getOnlyEnemy(this).coins >= 4 && coins < 7 && Objects.equals(e.getAction(), "Tax") && !hand.contains("Assassin")) {
                return e.getCard();
            }
            //stay near coup
            if (Objects.equals(e.getAction(), "Steal") && coins > 5 && coins < 9) {
                modifier += 2.5;
            }
            if (Objects.equals(e.getAction(), "BlockAssassination")) {
                modifier += 2.5;
            }
        }else {
            challangeChance -= 10;
        }
        int prevoiusclaims = Collections.frequency(getMindplayerbyNumber(e.getOrigin().getNumber()).getAllcardsClaimed(), e.getCard());
        if (prevoiusclaims > 1) {
            modifier -= ((prevoiusclaims - 1) * 2.5);
        } else if (getMindplayerbyNumber(e.getOrigin().getNumber()).getCardsStrategy().contains(e.getCard())) {
            modifier -= 1;
        }
        if (!getMindplayerbyNumber(e.getOrigin().getNumber()).getAllcardsClaimed().contains(e.getCard())){
            challangeChance+= getMindplayerbyNumber(e.getOrigin().getNumber()).getCardsClaimed().size() * 10;
        }

        if (getMindplayerbyNumber(e.getOrigin().getNumber()).getCardsNotClaimed().contains(e.getCard())) {
            challangeChance += 30;
            modifier += 10;

        }
        if (getMindplayerbyNumber(e.getOrigin().getNumber()).getCardsClaimed().size() > 2) {
            modifier += 1;
        }
        if (containingGame.turnNumber < 2 && Objects.equals(e.getCard(), "Duke")) {
            challangeChance = (15 / (containingGame.turnNumber + 1)) + 16 * Collections.frequency(getMindplayerbyNumber(e.getOrigin().getNumber()).getNothave(), "Duke");

        }

        if (Objects.equals(getMindplayerbyNumber(e.getOrigin().getNumber()).getStrategy(), "Honest")){
            challangeChance-=20*getMindplayerbyNumber(e.getOrigin().getNumber()).getHonest();
        }

        double prob = getProb(e.getOrigin().getNumber(), e.getCard());
        double chance = function(prob * 100, modifier);
        chance += challangeChance;

        if (chance< 0){
            chance=0;
        }
        if (r.nextInt(100) < chance) {
            //return  null;
            return e.getCard();
        }

        //check if based on known cards its possible for the enemy to hold the card
        if (!MathEliminate(e)) {
            //debug
            //check if enemy hand contains card we find impossible to have
            if (e.getOrigin().getHand().contains(e.getCard())){
                //alert if Inconsistency is found between player mind and gamestate.
                System.out.println("Inconsistency in code");
            }

            return e.getCard();
        } else return null;
    }



    public double function(double holding, double modifier) {
        return -21 + (100 + 21) / (1 + Math.pow((holding / modifier), 1.1));
    }

    public Card redraw1(String card) {

        if (getHand().getCardOne() != null) {
            if (Objects.equals(getHand().getCardOne().getName(), card)) {
                Card ret = getHand().getCardOne();
                getHand().setCardOne(null);
                addToAllKnowns(ret.getName());
                return ret;
            }
        }
        if (getHand().getCardTwo() != null) {
            if (Objects.equals(getHand().getCardTwo().getName(), card)) {
                Card ret = getHand().getCardTwo();
                getHand().setCardTwo(null);
                addToAllKnowns(ret.getName());
                return ret;
            }
        }
        return new Card("Empty", "Empty", "Empty");
    }

    public void redraw2(Card draw) {
        if (getHand().getCardOne() == null) {
            getHand().setCardOne(draw);
        } else if (getHand().getCardTwo() == null) {
            getHand().setCardTwo(draw);
        }
        removefromKnown(draw.getName());
        lastshuffled = null;
    }


    public MindPlayer getMindplayerbyNumber(int i) {
        if (i == number) {
            return me;
        } else {
            for (MindPlayer p : enemies) {
                if (p.getNumber() == i) {
                    return p;
                }
            }
        }
        return null;
    }

    public void addToAllKnowns(String s) {
        updateKnown();
        for (MindPlayer p : enemies) {
            p.addtoKnow(s);
        }
    }

    public void removefromKnown(String a) {
        for (MindPlayer p : enemies) {
            p.removefromKnow(a);
        }
    }

    public void removeException(String a, int num) {
        for (MindPlayer p : enemies) {
            if (num != p.getNumber()) {
                p.removefromKnow(a);
            }
        }
    }

    public double getProb(int playernum, String card) {
        MindPlayer target = getMindplayerbyNumber(playernum);
        double prob = target.getProb(card);
        return prob;
    }


    public Player clone() {
        try {
            return (Player) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return this;
    }

    public LinkedHashMap<MindPlayer, Double> gatherThreats() {
        HashMap<MindPlayer, Double> returnmap = new HashMap<>();
        for (MindPlayer mp : enemies) {
            if (containingGame.getPlayerByNumber(mp.getNumber()) != null) {
                returnmap.put(mp, mp.updateThreat());
            }
        }

        LinkedHashMap<MindPlayer, Double> result = new LinkedHashMap<>();
        returnmap.entrySet().stream()
                .sorted(Map.Entry.<MindPlayer, Double>comparingByValue().reversed())
                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
        return result;
    }

    public Double ownThreat() {
        double retthreat;
        int turnsFromCoup;
        int influence;
        double personalThreat;
        double attackchance;
        int addationalfactors = 0;
        int gainperturn;

        influence = getHand().getInfluence();

        //set turn until coup
        if ((cardsClaimed.contains("Captain"))) {
            gainperturn = 2;
        } else if ((Collections.frequency(containingGame.table, "Duke") + Collections.frequency(getHand().getHandStringList(), "Duke")) == 3) {
            gainperturn = 2;
        } else if ((cardsClaimed.contains("Duke"))) {
            gainperturn = 3;
        } else {
            gainperturn = 1;
        }

        if (coins > 7) {
            turnsFromCoup = 0;
        } else {
            turnsFromCoup = (7 - coins) / gainperturn;
        }

        double enemies = containingGame.players.size() - 1;

        attackchance = 1.0 / enemies;

        personalThreat = ((7 - turnsFromCoup) + (influence * 7)) * attackchance;

        if (cardsClaimed.contains("Captain")) {
            addationalfactors += 1;
        }
        if ((cardsClaimed.contains("Assassin"))) {
            addationalfactors += 1.5;
        }
        if ((cardsClaimed.contains("Duke"))) {
            addationalfactors += 0.5;
        }
        retthreat = personalThreat * (1.0 + (addationalfactors / 6.0));
        return retthreat;
    }

    public double averagethreats() {
        ArrayList<Double> threats = new ArrayList<>();
        HashMap<MindPlayer, Double> map = gatherThreats();
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            threats.add((Double) entry.getValue());
        }
        double sum = 0;
        for (double b : threats) {
            sum += b;
        }
        double average = sum / threats.size();

        return average;
    }

    public Integer assessThreats() {
        ArrayList<MindPlayer> peaks = new ArrayList<>();
        HashMap<MindPlayer, Double> map = gatherThreats();

        double average = averagethreats();

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if ((Double) entry.getValue() > average * 1.33 || (Double) entry.getValue() > (((getHand().getInfluence() * 7 + 8) / (containingGame.players.size() - 1)) + 2)) {
                peaks.add((MindPlayer) entry.getKey());
            }
        }
        if (peaks.size() == 0) {
            return null;
        }
        if (peaks.size() == 1) {
            return peaks.get(0).getNumber();
        }
        if (peaks.size() > 1) {
            Map<MindPlayer, Double> result = new LinkedHashMap<>();
            map.entrySet().stream()
                    .sorted(Map.Entry.<MindPlayer, Double>comparingByValue().reversed())
                    .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
            return ((MindPlayer) result.keySet().toArray()[0]).getNumber();
        } else {
            return null;
        }

    }

    public void setNumber(int number) {
        this.number = number;
    }
}
