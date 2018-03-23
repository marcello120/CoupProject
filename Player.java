package com.coup;

import com.sun.org.apache.regexp.internal.RE;

import java.util.*;
import java.util.stream.Collectors;

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
    private  ArrayList<MindPlayer> enemies;
    private MindPlayer me;
    public ArrayList<String> fromExchange = new ArrayList<>();
    public ArrayList<String> fromshuffle = new ArrayList<>();
    public String lastshuffled;
    public ArrayList<String> known = new ArrayList<>();
    public int aggression = 5;
    public double suspicion = 0.1;

    public ArrayList<String> cardsclaimed;


    public Player(int number, int playernum, Game g) {
        this.number = number;
        me = new MindPlayer(number,playernum, this);
        enemies = new ArrayList<>();
        for (int i = 0; i < playernum; i++) {
            if (i != number){
                enemies.add(new MindPlayer(i,playernum,this));
            }
        }
        containingGame = g;
    }

    public Event askDo() {

        for (MindPlayer p:
           enemies  ) {
            if (containingGame.getPlayerByNumber(p.getNumber())!= null){
                p.updateThreat();
            }
        }
        if (coins > 10){
            Player target = getCoupTarget();
            return new Event(this, "Coup", target, "-");
        }
        ArrayList<String> options = new ArrayList<>();
        options.add("Income");
        boolean aid = true;
        for (MindPlayer p: enemies) {
            if (p.getCardsClaimed().contains("Duke")){
                aid = false;
            }
        }
        if (aid){
            options.add("ForeignAid");
        }
        if (coins >= 3){
            options.add("Assassinate");
        }
        if (coins >= 7){
            options.add("Coup");
        }
        options.add("Exchange");
        options.add("Steal");

        String action = options.get(r.nextInt(options.size()));

        if (action == "Steal"){
            Player target = getStealTarget(containingGame.coinOrder(this));
            if (target == null){
               return askDo();
            }else{
                return new Event(this, "Steal", target,"-");
            }
        }

        return new Event(this, action, getCoupTarget(),"-");

    }


    public void update(Event e){
        if (e.getAction().contains("Trying") && !Objects.equals(e.getCard(), "-") && e.getOrigin()!=this){
            getMindplayerbyNumber(e.getOrigin().getNumber()).addToClaimed(e.getCard());
        }
        if ((Objects.equals(e.getAction(), "Income") || Objects.equals(e.getAction(), "ForeignAid") ) && e.getTarget()!= this){
            getMindplayerbyNumber(e.getOrigin().getNumber()).addNotClaimedCard("Duke");
        }
        if ((Objects.equals(e.getAction(), "Steal")) && e.getTarget()!= this){
            getMindplayerbyNumber(e.getTarget().getNumber()).addNotClaimedCard("Ambassador");
            getMindplayerbyNumber(e.getTarget().getNumber()).addNotClaimedCard("Captain");
        }
        if ((Objects.equals(e.getAction(), "Assassinate")) && e.getTarget()!= this){
            getMindplayerbyNumber(e.getTarget().getNumber()).addNotClaimedCard("Contessa");
        }

        if (Objects.equals(e.getAction(), "Exchange") && e.getTarget()!= this){
            getMindplayerbyNumber(e.getOrigin().getNumber()).clearKnown();
            getMindplayerbyNumber(e.getOrigin().getNumber()).clearKnown();
        }
        if(Objects.equals(e.getAction(), "RevealedCard") && e.getOrigin()!= this){
            getMindplayerbyNumber(e.getOrigin().getNumber()).addtoKnow(e.getCard());
            getMindplayerbyNumber(e.getOrigin().getNumber()).clearKnown();
        }
    }

    public ArrayList<String> updateKnown(){
        known.clear();
        known.addAll(fromExchange);
        known.addAll(fromshuffle);
        known.addAll(containingGame.getTableString());
        known.addAll(getHand().getHandStringList());
        return known;
    }

    public boolean MathEliminate(Event e){
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

    public ArrayList<Card> exchange(Card draw, Card draw1) {
        //choice
        ArrayList<Card> options = new ArrayList<>();
        options.add(draw);
        options.add(draw1);
        removefromKnown(draw1.getName());
        removefromKnown(draw.getName());
        for (int i = 0; i < hand.getHandList().size(); i++) {
            options.add(hand.getHandList().get(i));
        }
        Card tobe;
        if (hand.getInfuence() == 1) {
            if (hand.getCardOne() == null) {
                tobe = options.remove(r.nextInt(3));
                hand.setCardTwo(tobe);
            } else {
                tobe = options.remove(r.nextInt(3));
                hand.setCardOne(tobe);
            }
        } else {
            Card tobe1 = options.remove(r.nextInt(4));
            Card tobe2 = options.remove(r.nextInt(3));
            hand.setCardOne(tobe1);
            hand.setCardTwo(tobe2);
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
        if (e.getTarget().number == number){
            if (Objects.equals(e.getAction(), "Assassinate") && hand.contains("Contessa")){
                return null;
            }
            if (Objects.equals(e.getAction(), "Steal") && ( hand.contains("Ambassador") || hand.contains("Captain"))){
                return null;
            }
        }
        double modifier = 12.5;

        double blocklimit = suspicion;
        double blockchance = aggression;
        //up numbers if near death or near coup or if negatively effected
        if (e.getTarget().getNumber() == number){
           modifier+=2.5;
            //stay alive
            if (Objects.equals(e.getAction(), "Assassinate") && hand.getInfuence() == 1){
                return e.getCard();
            }
            //stay near coup
            if (Objects.equals(e.getAction(), "Steal") && coins > 5 && coins < 9){
                modifier+=2.5;
            }
            if (Objects.equals(e.getAction(), "BlockAssassination")){
                modifier+=2.5;
            }
        }


       double prob = getProb(e.getOrigin().getNumber(), e.getCard());

       double chance = function(prob*100, modifier);

       if (r.nextInt(100) < chance ){
           return e.getCard();
       }


       if(!MathEliminate(e)){
         if ( e.getOrigin().getHand().contains(e.getCard())){
             System.out.println("Here is where I almost dieded");
           }
            return e.getCard();
        }
        else return null;
    }

    public double function(double holding, double modifier){
        return -21+(100 + 21)/Math.pow((1+(holding/modifier)),1.1);
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
        } if (getHand().getCardTwo() != null) {
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


    public MindPlayer getMindplayerbyNumber(int i){
        if (i == number){
            return me;
        }else {
            for (MindPlayer p : enemies) {
                if (p.getNumber() == i) {
                    return p;
                }
            }
        }
        return null;
    }

    public void addToAllKnowns(String s){
        updateKnown();
        for (MindPlayer p: enemies) {
            p.addtoKnow(s);
        }
    }
    public void removefromKnown(String a){
        for (MindPlayer p: enemies) {
            p.removefromKnow(a);
        }
    }
    public void removeException(String a, int num){
        for (MindPlayer p: enemies) {
            if (num != p.getNumber()) {
                p.removefromKnow(a);
            }
        }
    }

    public double getProb(int playernum, String card){
      MindPlayer target =  getMindplayerbyNumber(playernum);
      double prob = target.getProb(card);
        return prob;
    }

    public Player getCoupTarget(){
        ArrayList<Player> options = containingGame.coinOrder(this);
        Map<Player, Integer> list = new HashMap<>();
        for (Player nme : options) {
            int priority = nme.coins;
            priority+=nme.getHand().getInfuence() *10;
            if (nme.coins > 6){
                priority+=10;
            }
            if (getMindplayerbyNumber(nme.getNumber()).getCardsClaimed().contains("Assassin") && !hand.contains("Contessa")){
                priority+=(7);
            }
            if (getMindplayerbyNumber(nme.getNumber()).getCardsClaimed().contains("Captain") && !hand.contains("Captain") && !hand.contains("Ambassador")){
                priority+=(7);
            }
            list.put(nme, priority);
        }
        Map<Player, Integer> result = new LinkedHashMap<>();
        list.entrySet().stream()
                .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed())
                .forEachOrdered(x -> result.put(x.getKey(),x.getValue()));

        return (Player) result.keySet().toArray()[0];
    }


    public Player getStealTarget(ArrayList<Player> options){
        ArrayList<Player> playeroptions = new ArrayList<>(options);
        for (int i = 0; i < playeroptions.size(); i++) {
            if (playeroptions.get(i).coins == 0){
                playeroptions.remove(playeroptions.get(i));
            }
        }
        if (playeroptions.isEmpty()){
            return null;
        }
        Map<Player, Double> chance = new HashMap<>();
        for (Player p: playeroptions) {
            double probCaptain = getMindplayerbyNumber(p.getNumber()).getProb("Captain");
            double probAmbassador = getMindplayerbyNumber(p.getNumber()).getProb("Ambassador");
            double combprob = probAmbassador + probCaptain;
            if (getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Ambassador")||getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Captain")){
                combprob-=50;
            }
            if (getMindplayerbyNumber(p.getNumber()).getCardsClaimed().isEmpty()){
                combprob-=21;
            }
            if (p.coins > 3 && getMindplayerbyNumber(p.getNumber()).getCardsClaimed().contains("Assassin")){
                combprob+=30;
            }
            combprob+=p.coins*3;
            chance.put(p, combprob);
        }
        Map<Player, Double> result = new LinkedHashMap<>();
        chance.entrySet().stream()
                .sorted(Map.Entry.<Player, Double>comparingByValue().reversed())
                .forEachOrdered(x -> result.put(x.getKey(),x.getValue()));

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


}
