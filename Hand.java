package com.coup;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Sarosi on 31/10/2017.
 */
public class Hand {
    private Card cardOne;
    private Card cardTwo;
    private int influence = 2;

    public Hand(Card cardOne, Card cardTwo) {
        this.cardOne = cardOne;
        this.cardTwo = cardTwo;
    }

    public int getInfluence() {
        return influence;
    }

    public void setInfluence(int influence) {
        this.influence = influence;
    }

    public Card getCardOne() {
        return cardOne;
    }

    public void setCardOne(Card cardOne) {
        this.cardOne = cardOne;
    }

    public Card getCardTwo() {
        return cardTwo;
    }

    public void setCardTwo(Card cardTwo) {
        this.cardTwo = cardTwo;
    }

    public ArrayList<Card> getHandList(){
        ArrayList<Card> cards = new ArrayList<>();
        if(cardOne != null) {
            cards.add(cardOne);
        }
        if(cardTwo != null) {
            cards.add(cardTwo);
        }
        return cards;
    }

    public ArrayList<String> getHandStringList(){
        ArrayList<String> cardStrings = new ArrayList<>();
        if(cardOne != null) {
            cardStrings.add(cardOne.getName());
        }
        if(cardTwo != null) {
            cardStrings.add(cardTwo.getName());
        }
        return cardStrings;
    }

    public void loseInfluence(){
        influence--;
    }

    @Override
    public String toString() {
        return "Hand{" +
                "cardOne=" + cardOne +
                ", cardTwo=" + cardTwo +
                ", influence=" + influence +
                '}';
    }

    public String cardsAsBits(){
        String s = "";
        if (Objects.equals(cardOne.getName(), "Contessa") || Objects.equals(cardTwo.getName(), "Contessa")){
            s = s+"1";
        }
        else{
            s = s+"0";
        }
        if (Objects.equals(cardOne.getName(), "Captain") || Objects.equals(cardTwo.getName(), "Captain")){
            s = s+"1";
        }
        else{
            s = s+"0";
        }
        if (Objects.equals(cardOne.getName(), "Ambassador") || Objects.equals(cardTwo.getName(), "Ambassador")){
            s = s+"1";
        }
        else{
            s = s+"0";
        }
        if (Objects.equals(cardOne.getName(), "Assassin") || Objects.equals(cardTwo.getName(), "Assassin")){
            s = s+"1";
        }
        else{
            s = s+"0";
        }
        if (Objects.equals(cardOne.getName(), "Duke") || Objects.equals(cardTwo.getName(), "Duke")){
            s = s+"1";
        }
        else{
            s = s+"0";
        }

        return s;

    }

    public boolean contains(String s){
        if (cardOne!= null){
            if (Objects.equals(cardOne.getName(), s)){
                return true;
            }
        }
        if (cardTwo!= null){
            if (Objects.equals(cardTwo.getName(), s)){
                return true;
            }
        }
        return  false;
    }
}
