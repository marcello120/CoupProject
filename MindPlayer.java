package com.coup;

import com.sun.applet2.AppletParameters;

import java.util.*;

/**
 * Created by Sarosi on 25/11/2017.
 */
public class MindPlayer {
    private ArrayList<String> cardsClaimed;
    private ArrayList<String> cardsNotClaimed;
    private ArrayList<String> cardsStrategy;
    private HashMap<String, Double> percentageAndCards;
    private ArrayList<String> cardsConflicted;
    private ArrayList<String> allcardsClaimed;
    private int number;
    private ArrayList<String> sureCards;
    private ArrayList<String> cardsFalselyClaimed;
    private Player owner;
    private Set<String> cards = new HashSet<>();
    private double threat = 0;



    public ArrayList<String> inTheDeck;

    private ArrayList<String> nothave;

    public MindPlayer(int number, int playernum, Player owner){
        this.owner = owner;
        this.number = number;
        cardsNotClaimed = new ArrayList<>();
        cardsConflicted = new ArrayList<>();
        percentageAndCards = new HashMap<>();
        cardsStrategy = new ArrayList<>();
        cardsFalselyClaimed =  new ArrayList<>();
        sureCards = new ArrayList<>();
        cardsClaimed = new ArrayList<>();
        allcardsClaimed = new ArrayList<>();
        percentageAndCards = new HashMap<>();
        cards = initCards();
        inTheDeck = new ArrayList<>();
        nothave = new ArrayList<>();
    }


    public double updateThreat(){
        double retthreat;
        int turnsFromCoup;
        int influence;
        double personalThreat;
        double attackchance;
        int addationalfactors = 0;
        int gainperturn;

        influence = owner.containingGame.getPlayerByNumber(number).getHand().getInfuence();

        //set turn until coup
        if ((cardsClaimed.contains("Captain") || cardsStrategy.contains("Captain")) && !cardsNotClaimed.contains("Captain")){
            gainperturn = 2;
        }
        else if ((Collections.frequency(owner.containingGame.table, "Duke") + Collections.frequency(owner.getHand().getHandStringList(), "Duke")) == 3 ){
            gainperturn = 2;
        }
        else if ((cardsClaimed.contains("Duke") || cardsStrategy.contains("Duke")) && !cardsNotClaimed.contains("Duke")){
             gainperturn = 3;
        } else {
            gainperturn = 1;
        }

        if (owner.containingGame.getPlayerByNumber(number).coins > 7){
            turnsFromCoup = 0;
        }
        else{
            turnsFromCoup = (7 - owner.containingGame.getPlayerByNumber(number).coins)/ gainperturn ;
        }

        double enemies = owner.containingGame.players.size() - 1;

        attackchance = 1.0 / enemies ;

        personalThreat = ((7-turnsFromCoup) + (influence * 7))* attackchance;

        if ((cardsClaimed.contains("Captain") || cardsStrategy.contains("Captain")) && !cardsNotClaimed.contains("Captain") && ((!owner.getHand().getHandStringList().contains("Captain")) || !owner.getHand().getHandStringList().contains("Ambassador"))){
            addationalfactors += 2;
        }
        if ((cardsClaimed.contains("Assassin") || cardsStrategy.contains("Assassin")) && !cardsNotClaimed.contains("Assassin") && !owner.getHand().getHandStringList().contains("Contessa")){
            addationalfactors += 3;
        }
        if ((cardsClaimed.contains("Duke") || cardsStrategy.contains("Duke")) && !cardsNotClaimed.contains("Duke") && !owner.getHand().getHandStringList().contains("Duke")){
            addationalfactors += 1;
        }

        retthreat = personalThreat * (1.0 + (addationalfactors/6.0));

        threat = retthreat;

        return threat;

    }

    private HashSet<String> initCards(){
        Deck d = new Deck();
        HashSet<String> cards = new HashSet<>();
        for (int i = 0; i < d.cards.size(); i++) {
            cards.add(d.cards.get(i).getName());
        }
        return cards;
    }

    private void upDateNotHave(){
        nothave.clear();
        nothave.addAll(owner.getHand().getHandStringList());
        nothave.addAll(owner.containingGame.getTableString());
        nothave.addAll(inTheDeck);
    }

    public void addtoKnow(String input){
        inTheDeck.add(input);
        upDateNotHave();
    }
    public void removefromKnow(String input){
        if(inTheDeck.contains(input)){
            inTheDeck.remove(input);
        }
        upDateNotHave();
    }
    public void clearKnown(){
        cardsClaimed.clear();
        allcardsClaimed.clear();
        cardsConflicted.clear();
        cardsNotClaimed.clear();
        for ( String s: cards ) {
            if (inTheDeck.contains(s)){
                inTheDeck.remove(s);
            }
        }
        upDateNotHave();
    }

    public boolean canHave(String s){
       if(Collections.frequency(nothave, s) == 3){
           return false;
       }
       else{
           return  true;
       }
    }

    public double getProb(String card){
        upDateNotHave();
        double kx = Collections.frequency(nothave, card);
        double k = nothave.size();
        if (kx == 3){
            return 0;
        }
        if (owner.containingGame.getPlayerByNumber(number).getHand().getInfuence() == 1){
            // has 1 card
            return (3-kx)/(15-k);
        }else{
            //has 2 cards
            if (kx == 2){
                //only 1 unknown
              return  2*  (3-kx)/(15-k);
            }else{
                // more then 1 unknown
                double same1 = (3-kx)/(15-k);
                double same2 = (2-kx)/(15-1-k);
                double same = 0.5*(((3-kx)/(15-k))*((2-kx)/(15-1-k)));
                double diff = 2.0 *(((3-kx)/(15-k))*((15-3-k+kx)/(15-1-k)));
                return same + diff;
            }
        }
    }

    public void updateCardsandProb(){
        for (String card : cards) {
            percentageAndCards.put(card, getProb(card));
        }
        percentageAndCards.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEachOrdered(x -> percentageAndCards.put(x.getKey(),x.getValue()));
    }

    private void deduceStrategy(){
        Map<String, Integer> freq = owner.containingGame.log.getEventFreq(number);
       String first = (String) freq.keySet().toArray()[0];
       if(owner.containingGame.turnNumber > 2)
       if (Objects.equals(first, "Income") || Objects.equals(first, "ForeignAid")){
           cardsStrategy.add("Contessa");
           cardsStrategy.add("Assassin");
       }
       if (Objects.equals(first, "TryingToTax") && freq.get(first) > 1){
            cardsStrategy.add("Duke");
       }
       if (Objects.equals(first, "TryingToSteal" )&& freq.get(first) > 1){
            cardsStrategy.add("Captain");
       }
       if (Objects.equals(first, "TryingToAssassinate")&& freq.get(first) > 1){
            cardsStrategy.add("Assassin");
       }
    }

    public  void addNotClaimedCard(String s){
        if (!cardsNotClaimed.contains(s)) {
            cardsNotClaimed.add(s);
        }
        if (cardsClaimed.contains(s)){
            cardsConflicted.add(s);
        }
    }

    public int getNumber() {
        return number;
    }

    public void addToClaimed(String s){
        deduceStrategy();
        allcardsClaimed.add(s);
        if (!cardsClaimed.contains(s)){
            cardsClaimed.add(s);
        }
        if (cardsNotClaimed.contains(s)){
            cardsConflicted.add(s);
        }
    }

    public ArrayList<String> getNothave() {
        upDateNotHave();
        return nothave;
    }

    public ArrayList<String> getCardsClaimed() {
        return cardsClaimed;
    }

    public ArrayList<String> getAllcardsClaimed() {
        return allcardsClaimed;
    }

    public ArrayList<String> getCardsNotClaimed() {
        return cardsNotClaimed;
    }

    public ArrayList<String> getCardsConflicted() {
        return cardsConflicted;
    }

    public ArrayList<String> getCardsStrategy() {
        return cardsStrategy;
    }
}
