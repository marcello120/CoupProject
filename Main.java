package com.coup;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        ArrayList<String> hands = new ArrayList<>();

        double threats=0;
        double avg=0;

        int nothreats=0;


        for (int i = 0; i < 1 ; i++) { // set number of matches
            Game g = new Game(3, 0); //set number of players
            //g.gameState();
            hands.add(g.turns().originalHand.cardsAsBits());
        }
        System.out.println(avg);
        System.out.println(nothreats);


        System.out.println(hands);

        Map<String, Long> counts =
                hands.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        System.out.println(counts);
    }
}
