package com.coup;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.toIntExact;

/**
 * Created by Sarosi on 24/10/2017.
 */
public class Player implements Cloneable {
    public Random r = new Random();
    public Hand hand;
    public int number;
    int coins;
    public Hand originalHand;
    public Game containingGame;
    private ArrayList<MindPlayer> enemies;
    private MindPlayer me;
    public ArrayList<String> fromExchange = new ArrayList<>();
    public ArrayList<String> fromshuffle = new ArrayList<>();
    public String lastshuffled;
    public ArrayList<String> known = new ArrayList<>();
    public int aggression = 5;
    public double suspicion = 0.1;

    public ArrayList<String> cardsClaimed = new ArrayList<>();
    public ArrayList<String> cardsNotClaimed = new ArrayList<>();
    private Player assassinationTarget;
    private ArrayList<String> fishFor = new ArrayList<>();


    public Player(int number, int playernum, Game g) {
        this.number = number;
        me = new MindPlayer(number, playernum, this);
        enemies = new ArrayList<>();
        for (int i = 0; i < playernum; i++) {
            if (i != number) {
                enemies.add(new MindPlayer(i, playernum, this));
            }
        }
        containingGame = g;
    }

    public boolean isThreatTooHigh() {
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
                if (p.getHand().getInfuence() > maxinfluence) {
                    maxinfluence = p.getHand().getInfuence();
                }
            }
        }
        if (maxinfluence < getHand().getInfuence()) {
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
        if (maxthreat < ownThreat() && maxcoins > 7) {
            return true;
        }

        return false;

    }

    public Event askDo() {

        Integer threat = assessThreats();


//        if (threat == null) {
//            if (isThreatTooHigh()) {
//                //own threat is too high
//
//                riskyplay();
//
//                if (coins > 7) {
//                    return new Event(this, "Coup", containingGame.getPlayerByNumber(((MindPlayer) gatherThreats().keySet().toArray()[0]).getNumber()), "-");
//                }
//                if (coins > 3 && (safeGeneral("Assassin", null) || hand.contains("Assassin"))) {
//                    cardsClaimed.add("Assassin");
//                    return new Event(this, "Assassinate", assassinationTarget, "Assassin");
//                }
//
//                if (imminentThreat()) {
//                    //relinguish adventage
//                    riskyplay();
//                    lowTempo();
//                    for (Player p : containingGame.players) {
//                        if (p.coins > 7) {
//                            FakeAssassinate();
//                        }
//
//                    }
//                    if (cardsAndCounters()) {
//                        if (safeGeneral("Ambassador", null) || hand.contains("Ambassador")) {
//                            return new Event(this, "Exchange",this, "Ambassador");
//                        }
//                    }
//                    //blocking
//                }
//                return new Event(this, "Income", this, "-");
//
//            } else {
//                //threat level is fine
//
//                //block
//                if (cardsAndCounters()) {
//                    if (safeGeneral("Ambassador", null)) {
//                        return new Event(this, "Exchange", this, "Ambassador");
//                    }
//                }else {
//                    return new Event(this, "Income", this, "-");
//                }
//
//                //prep for coup
//                if (safeGeneral("Duke,", null)) {
//                    return new Event(this, "Tax", this, "-");
//                }
//                if (safeGeneral("Captain,", null)) {
//                    return new Event(this, "Steal", getStealTarget(), "-");
//                }
//                if (safeGeneral("ForeignAid,", null)) {
//                    return new Event(this, "ForeignAid", this, "-");
//                }
//
//                lowTempo();
//            }
//
//
//        } else {
//            //todo threat
//            //threat
//        }
        if (coins > 10) {
            Player target = containingGame.getPlayerByNumber(((MindPlayer) gatherThreats().keySet().toArray()[0]).getNumber());
            return new Event(this, "Coup", target, "-");
        }
        ArrayList<String> options = new ArrayList<>();
        options.add("Income");
        boolean aid = true;
        for (MindPlayer p : enemies) {
            if (p.getCardsClaimed().contains("Duke")) {
                aid = false;
            }
        }
        if (aid) {
            options.add("ForeignAid");
        }
        if (coins >= 3) {
            options.add("Assassinate");
        }
        if (coins >= 7) {
            options.add("Coup");
        }
        options.add("Exchange");
        options.add("Steal");

        String action = options.get(r.nextInt(options.size()));

        if (Objects.equals(action, "Steal")) {
            Player target = getStealTarget(containingGame.coinOrder(this));
            if (target == null) {
                return askDo();
            } else {
                return new Event(this, "Steal", target, "-");
            }
        }

        return new Event(this, action, getCoupTarget(), "-");

    }

    public Event lowTempo(){
        if (safeGeneral("Ambassador", null)){
            return new Event(this, "Exchange", this, "Ambassador");
        }else{
            return new Event(this, "Income", this, "-");
        }
    }

    public boolean cardsAndCounters(){
        boolean returningBoolean = false;
        for (MindPlayer mp: enemies) {
            if (containingGame.getPlayerByNumber(mp.getNumber())!=null){
                if ((mp.getCardsClaimed().contains("Assassin") || mp.getCardsStrategy().contains("Assassin") ) && !getHand().contains("Contessa")){
                    returningBoolean = true;
                    fishFor.add("Contessa");
                }
                if ((mp.getCardsClaimed().contains("Captain") || mp.getCardsStrategy().contains("Captain") ) && !getHand().contains("Ambassador") && !getHand().contains("Captain")){
                    returningBoolean = true;
                    fishFor.add("Ambassador");
                    fishFor.add("Captain");
                }
                if ((mp.getCardsClaimed().contains("Duke") || mp.getCardsStrategy().contains("Duke") ) && !getHand().contains("Captain") && !getHand().contains("Duke")){
                    returningBoolean = true;
                    fishFor.add("Duke");
                    fishFor.add("Captain");
                }
            }
        }
        return returningBoolean;
    }





    public boolean imminentThreat(){
        for (MindPlayer mp: enemies) {
            if (containingGame.getPlayerByNumber(mp.getNumber())!=null){
                if (mp.getCardsClaimed().contains("Assassin") || containingGame.getPlayerByNumber(mp.getNumber()).coins>=7){
                    return true;
                }
            }

        }
        return false;
    }

    private boolean safeGeneral(String card, Player p) {
        if (Objects.equals(card, "ForeignAid")) {
            safeSpecific(card, p);
        }
        if (hand.contains(card)){
            safeSpecific(card,p);
        }
        if (cardsNotClaimed.contains(card)) {
            return false;
        }
        if (Collections.frequency(containingGame.getTableString(), card) == 3) {
            return false;
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
                if (containingGame.getPlayerByNumber(mp.getNumber())!=null){
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


    public Boolean safeSpecific(String card, Player p) {
        if (Objects.equals(card, "Ambassador")) {
            return true;
        }
        if (Objects.equals(card, "Duke")) {
            return true;
        }
        if (Objects.equals(card, "Assassin")) {
            if (Collections.frequency(containingGame.getTableString(), "Contessa") + Collections.frequency(hand.getHandStringList(), "Contessa") == 3) {
                return true;
            }
            if (p == null) {
                for (MindPlayer mp : enemies) {
                    if (containingGame.getPlayerByNumber(mp.getNumber()) != null) {
                        if (mp.getCardsClaimed().size() >= containingGame.getPlayerByNumber(mp.getNumber()).hand.getInfuence() && !mp.getCardsClaimed().contains("Contessa") && mp.getProb("Contessa") < 40) {
                            assassinationTarget = containingGame.getPlayerByNumber(mp.getNumber());
                            return true;
                        }
                    }
                }
            } else {
                if (getMindplayerbyNumber(p.getNumber()).getCardsClaimed().size() >= containingGame.getPlayerByNumber(getMindplayerbyNumber(p.getNumber()).getNumber()).hand.getInfuence() && !getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Contessa") && getMindplayerbyNumber(p.getNumber()).getProb("Contessa") < 40) {
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
                    return true;
                }
            }
            for (MindPlayer mp : enemies) {
                if (containingGame.getPlayerByNumber(mp.getNumber()) != null) {
                    if (mp.getCardsNotClaimed().contains("Ambassador") && mp.getCardsNotClaimed().contains("Captain")) {
                        return true;
                    }
                    if (mp.getCardsClaimed().size() >= containingGame.getPlayerByNumber(mp.getNumber()).hand.getInfuence() && !mp.getCardsClaimed().contains("Captain") && !mp.getCardsClaimed().contains("Ambassador") && mp.getProb("Ambassador") < 40 && mp.getProb("Captain") < 40) {
                        return true;
                    }
                }
            }

        }
        return false;
    }


    public void update(Event e) {
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
            cardsNotClaimed.add("Ambassador");
            cardsNotClaimed.add("Captain");
        }
        if ((Objects.equals(e.getAction(), "Assassinate")) && e.getTarget() == this) {
            cardsNotClaimed.add("Contessa");
        }
        if ((Objects.equals(e.getAction(), "Income")) && e.getTarget() == this) {
            cardsNotClaimed.add("Duke");
        }
        if (e.getAction().contains("Trying") && !Objects.equals(e.getCard(), "-") && e.getOrigin() == this) {
            cardsClaimed.add(e.getCard());
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

    public boolean MathEliminate(Event e) {
        return getMindplayerbyNumber(e.getOrigin().getNumber()).canHave(e.getCard());
    }


    public Player() {
    }

    public Card loseCard() {
        //choice
        if (getHand().getInfuence() == 2) {
            if (r.nextBoolean()) {
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

    public ArrayList<Card> sortCards(ArrayList<Card> cardlist){
        HashMap<Card, Integer> mapOfCards = new HashMap<>();
        for (Card c: cardlist) {
            if (Objects.equals(c.getName(), "Duke")){
                mapOfCards.put(c,0 - Collections.frequency(fishFor,"Duke"));
            }
            if (Objects.equals(c.getName(), "Assassin")){
                mapOfCards.put(c,1 - Collections.frequency(fishFor,"Assassin"));
            }
            if (Objects.equals(c.getName(), "Captain")){
                mapOfCards.put(c,2 - Collections.frequency(fishFor,"Captain"));
            }
            if (Objects.equals(c.getName(), "Contessa")){
                mapOfCards.put(c,3 - Collections.frequency(fishFor,"Contessa"));
            }
            if (Objects.equals(c.getName(), "Ambassador")){
                mapOfCards.put(c,4 - Collections.frequency(fishFor,"Ambassador"));
            }
        }
        Map<Card, Integer> result = new LinkedHashMap<>();
        mapOfCards.entrySet().stream()
                .sorted(Map.Entry.<Card, Integer>comparingByValue().reversed())
                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));

        ArrayList<Card> newestCards = new ArrayList<Card>(result.keySet());

        Collections.reverse(newestCards);
        
        System.out.println(newestCards);

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
        int cardsToFill = getHand().getInfuence();
        ArrayList<Card> keep = new ArrayList<>();
        for (int i = 0; i < hand.getHandList().size(); i++) {
            options.add(hand.getHandList().get(i));
        }
        ArrayList<Card> options2 = new ArrayList<>(options);
        ArrayList<Card> pref = new ArrayList<>();
        for (Card c:options) {
            if (fishFor.contains(c.getName()) && !pref.contains(c)){
                pref.add(c);
            }
        }
        pref = sortCards(pref);
        options2 = sortCards(options2);
        keep.addAll(pref);
        options2.removeAll(pref);
        keep.addAll(options2);
            if (hand.getInfuence() == 1) {
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


        System.out.println("Put back" + options);

        return options;

    }

    public boolean askBlockAid(Player p) {
        if (getHand().contains("Duke")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean askBlockAssassinWithContessa(Player origin) {
        if (getHand().contains("Contessa")) {
            return true;
        } else {
            return false;
        }
    }

    public String askBlockSteal(Player origin) {
        if (getHand().contains("Ambassador")) {
            return "Ambassador";
        } else if (getHand().contains("Captain")) {
            return "Captain";
        }
        return null;
    }

    public String askChallenge(Event e) {
        if (e.getTarget().number == number) {
            if (Objects.equals(e.getAction(), "Assassinate") && hand.contains("Contessa")) {
                return null;
            }
            if (Objects.equals(e.getAction(), "Steal") && (hand.contains("Ambassador") || hand.contains("Captain"))) {
                return null;
            }
        }
        double modifier = 12.5;

        if (getHand().getInfuence() == 1) {
            modifier -= 1;
        }

        double blocklimit = suspicion;
        double blockchance = aggression;
        //up numbers if near death or near coup or if negatively effected
        if (e.getTarget().getNumber() == number) {
            modifier += 2.5;
            //stay alive
            if (Objects.equals(e.getAction(), "Assassinate") && hand.getInfuence() == 1 && !cardsClaimed.contains("Contessa")) {
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
        }
        int prevoiusclaims = Collections.frequency(getMindplayerbyNumber(e.getOrigin().getNumber()).getAllcardsClaimed(), e.getCard());
        if (prevoiusclaims > 1) {
            modifier -= ((prevoiusclaims - 1) * 2.5);
        } else if (getMindplayerbyNumber(e.getOrigin().getNumber()).getCardsStrategy().contains(e.getCard())) {
            modifier -= 1;
        }
        if (getMindplayerbyNumber(e.getOrigin().getNumber()).getCardsNotClaimed().contains(e.getCard())) {
            modifier += 5;
        }
        if (getMindplayerbyNumber(e.getOrigin().getNumber()).getCardsClaimed().size() > 2) {
            modifier += 1;
        }

        double prob = getProb(e.getOrigin().getNumber(), e.getCard());

        double chance = function(prob * 100, modifier);

        if (r.nextInt(100) < chance) {
            return e.getCard();
        }

        if (!MathEliminate(e)) {
            return e.getCard();
        } else return null;
    }

    public double function(double holding, double modifier) {
        return -21 + (100 + 21) / Math.pow((1 + (holding / modifier)), 1.1);
    }

    public Card redraw1(String card) {

        System.out.println("CARD " + card);

        System.out.println("RETURN Hand " + hand);


        if (getHand().getCardOne() != null) {
            System.out.println(hand.getCardOne().getName() + " to " + card);
            if (Objects.equals(getHand().getCardOne().getName(), card)) {
                Card ret = getHand().getCardOne();
                getHand().setCardOne(null);
                System.out.println("RETURN " + ret);
                addToAllKnowns(ret.getName());
                return ret;
            }
        }
        if (getHand().getCardTwo() != null) {
            System.out.println(hand.getCardTwo().getName() + " to " + card);
            if (Objects.equals(getHand().getCardTwo().getName(), card)) {
                Card ret = getHand().getCardTwo();
                getHand().setCardTwo(null);
                System.out.println("RETURN " + ret);
                addToAllKnowns(ret.getName());
                return ret;
            }
        }
        return new Card("Empty", "Empty", "Empty");
    }

    public void redraw2(Card draw) {
        System.out.println("redraw " + draw);
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

    public Player getCoupTarget() {
        ArrayList<Player> options = containingGame.coinOrder(this);
        Map<Player, Integer> list = new HashMap<>();
        for (Player nme : options) {
            int priority = nme.coins;
            priority += nme.getHand().getInfuence() * 10;
            if (nme.coins > 6) {
                priority += 10;
            }
            if (getMindplayerbyNumber(nme.getNumber()).getCardsClaimed().contains("Assassin") && !hand.contains("Contessa")) {
                priority += (7);
            }
            if (getMindplayerbyNumber(nme.getNumber()).getCardsClaimed().contains("Captain") && !hand.contains("Captain") && !hand.contains("Ambassador")) {
                priority += (7);
            }
            list.put(nme, priority);
        }
        Map<Player, Integer> result = new LinkedHashMap<>();
        list.entrySet().stream()
                .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed())
                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));

        return (Player) result.keySet().toArray()[0];
    }


    public Player getStealTarget(ArrayList<Player> options) {
        ArrayList<Player> playeroptions = new ArrayList<>(options);
        for (int i = 0; i < playeroptions.size(); i++) {
            if (playeroptions.get(i).coins == 0) {
                playeroptions.remove(playeroptions.get(i));
            }
        }
        if (playeroptions.isEmpty()) {
            return null;
        }
        Map<Player, Double> chance = new HashMap<>();
        for (Player p : playeroptions) {
            double probCaptain = getMindplayerbyNumber(p.getNumber()).getProb("Captain");
            double probAmbassador = getMindplayerbyNumber(p.getNumber()).getProb("Ambassador");
            double combprob = probAmbassador + probCaptain;
            if (getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Ambassador") || getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Captain")) {
                combprob -= 50;
            }
            if (getMindplayerbyNumber(p.getNumber()).getCardsClaimed().isEmpty()) {
                combprob -= 21;
            }
            if (p.coins > 3 && getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Assassin")) {
                combprob += 30;
            }
            combprob += p.coins * 3;
            chance.put(p, combprob);
        }
        Map<Player, Double> result = new LinkedHashMap<>();
        chance.entrySet().stream()
                .sorted(Map.Entry.<Player, Double>comparingByValue().reversed())
                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));

        //System.out.println(result);
        return (Player) result.keySet().toArray()[0];
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

        influence = getHand().getInfuence();

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
            System.out.println(((MindPlayer) entry.getKey()).getNumber() + " = " + entry.getValue());
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
            System.out.println(((getHand().getInfuence() * 7 + 8) / (containingGame.players.size() - 1)) + 1);
            if ((Double) entry.getValue() > average * 1.33 || (Double) entry.getValue() > (((getHand().getInfuence() * 7 + 8) / (containingGame.players.size() - 1)) + 2)) {
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


}
