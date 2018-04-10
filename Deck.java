package com.coup;

import java.util.Collections;
import java.util.Stack;

/**
 * Created by Sarosi on 24/10/2017.
 */
public class Deck {
    public Stack<Card> cards;

    public Deck(){
        cards = new Stack<Card>();

        for (int i = 0; i < 3; i++) {
            cards.push(new Card("Contessa","blckKi","Blocks assassinations"));
        }
        for (int i = 0; i < 3; i++) {
            cards.push(new Card("Captain","St-bclkSt","Steal, take 2 coins from an other player, can be blocked by Captain and Ambassador"));
        }
        for (int i = 0; i < 3; i++) {
            cards.push(new Card("Ambassador","Ch-bclkSt","Draw 2 cards from the deck, then choose 2 to keep and shuffle the others back in."));
        }
        for (int i = 0; i < 3; i++) {
            cards.push(new Card("Assassin","Ki","Pay 3 coins to assassinate a target. Can be blocked by Contessa"));
        }
        for (int i = 0; i < 3; i++) {
            cards.push(new Card("Duke","Ta-blckFa","Tax, get 3 coins, can block foreign aid, can block Foreign aid"));
        }
        shuffle();

    }

    public void shuffle(){
        Collections.shuffle(cards);
    }

    public Card draw(){
        return cards.pop();
    }

    public void add(Card input){
         cards.push(input);
         shuffle();
    }

    public void show(){
        Stack<Card> tempDeck = new Stack<Card>();
        Card tempCard;
        while(!cards.empty()){
           tempCard = cards.pop();
            tempDeck.push(tempCard);
        }
        cards = tempDeck;
        shuffle();
    }

    @Override
    public String toString() {
        return "Deck{" +
                "cards=" + cards +
                '}';
    }
}
