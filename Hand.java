package com.coup;

import java.util.ArrayList;

/**
 * Created by Sarosi on 31/10/2017.
 */
public class Hand {
    private Card cardOne;
    private Card cardTwo;
    private int infuence = 2;

    public Hand(Card cardOne, Card cardTwo) {
        this.cardOne = cardOne;
        this.cardTwo = cardTwo;
    }

    public int getInfuence() {
        return infuence;
    }

    public void setInfuence(int infuence) {
        this.infuence = infuence;
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

    public void loseInfuence(){
        infuence--;
    }

    @Override
    public String toString() {
        return "Hand{" +
                "cardOne=" + cardOne +
                ", cardTwo=" + cardTwo +
                ", infuence=" + infuence +
                '}';
    }

    public String cardsAsBits(){
        String s = "";
        if (cardOne.getName() == "Contessa" || cardTwo.getName() == "Contessa"){
            s = s+"1";
        }
        else{
            s = s+"0";
        }
        if (cardOne.getName() == "Captain" || cardTwo.getName() == "Captain"){
            s = s+"1";
        }
        else{
            s = s+"0";
        }
        if (cardOne.getName() == "Ambassador" || cardTwo.getName() == "Ambassador"){
            s = s+"1";
        }
        else{
            s = s+"0";
        }
        if (cardOne.getName() == "Assassin" || cardTwo.getName() == "Assassin"){
            s = s+"1";
        }
        else{
            s = s+"0";
        }
        if (cardOne.getName() == "Duke" || cardTwo.getName() == "Duke"){
            s = s+"1";
        }
        else{
            s = s+"0";
        }

        return s;

    }

    public boolean contains(String s){
        if (cardOne!= null){
            if (cardOne.getName() == s){
                return true;
            }
        }
        if (cardTwo!= null){
            if (cardTwo.getName() == s){
                return true;
            }
        }
        return  false;
    }
}
