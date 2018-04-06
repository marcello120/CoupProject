package com.coup;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        boolean correctinput = false;

        int playercount = 4;
        int humancount = 0;


        //set number of players

//        while (!correctinput) {
//            System.out.print("Number of total players (3-6): ");
//            String playernum = sc.nextLine();
//            System.out.print("Number of human players out of " + playernum + " (0-" + playernum + ") : ");
//            String humannum = sc.nextLine();
//            if (Objects.equals(playernum, "3") || Objects.equals(playernum, "4") || Objects.equals(playernum, "5") || Objects.equals(playernum, "6")) {
//                if (Objects.equals(humannum, "1") || Objects.equals(humannum, "2") || Objects.equals(humannum, "3") || Objects.equals(humannum, "4") || Objects.equals(humannum, "5") || Objects.equals(humannum, "6") || Objects.equals(humannum, "0")) {
//                    playercount = Integer.parseInt(playernum);
//                    humancount = Integer.parseInt(humannum);
//                    if (humancount < playercount) {
//                        System.out.println("Game starting...");
//                        correctinput = true;
//                    }
//                }
//            }
//            if (!correctinput) {
//                System.out.println("Incorrect input!");
//            }
//        }


        ArrayList<String> hands = new ArrayList<>();

        int threats = 0;
        double avg = 0;
        int nothreats = 0;


        for (int i = 0; i < 10000; i++) { // set number of matches
            Game g = new Game(playercount, humancount); //set number of players
            //g.gameState();
            hands.add(g.turns().originalHand.cardsAsBits());
            threats += g.threat;
            nothreats += g.nothreat;
        }
        System.out.println(threats);
        System.out.println(nothreats);


        System.out.println(hands);

        Map<String, Long> counts =
                hands.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        System.out.println(counts);
    }
}
