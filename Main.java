package com.coup;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        boolean correctinput = false;

        int playercount = 3;
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

        int RandWin = 0;
        int AIWin = 0;
         int assassinationCount = 0;
         int incomeCount = 0;
         int foreignAidCount = 0;
         int exchangeCount = 0;
         int coupCount = 0;
         int stealCount = 0;
         int taxCount = 0;
         int threatcount = 0;
         int nothreatcount = 0;
         int succChallenge = 0;
         int unSuccChallenge = 0;
         int nochallange = 0;




        for (int i = 0; i < 1000; i++) { // set number of matches
            Game g = new Game(playercount, humancount); //set number of players
            //g.gameState();
            hands.add(g.turns().originalHand.cardsAsBits());

            RandWin += g.RandWin;
            AIWin += g.AIWin;
            assassinationCount+=g.assassinationCount;
            incomeCount+=g.incomeCount;
            foreignAidCount+=g.foreignAidCount;
            exchangeCount+=g.exchangeCount;
            coupCount+=g.coupCount;
            stealCount+=g.stealCount;
            taxCount+=g.taxCount;
            threatcount+=g.threatcount;
            nothreatcount+=g.nothreatcount;
            succChallenge+=g.succChallenges;
            unSuccChallenge+=g.unSuccChallenge;
            nochallange+=g.nochallenge;

        }
        System.out.println("AssassinationCount " + assassinationCount);
        System.out.println("IncomeCount " + incomeCount);
        System.out.println("ForeignAidCount " + foreignAidCount);
        System.out.println("ExchangeCount " + exchangeCount);
        System.out.println("CoupCount " + coupCount);
        System.out.println("StealCount " + stealCount);
        System.out.println("taxCount " + taxCount);
        System.out.println("Successfully challenged " + succChallenge);
        System.out.println("Unsuccessfully challenged " + unSuccChallenge);
        System.out.println("NoChallenge " + nochallange);


        System.out.println( "Random wins: " + RandWin);
        System.out.println( "AI wins: " + AIWin);


        //System.out.println(hands);

        Map<String, Long> counts =
                hands.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        //System.out.println(counts);
    }
}
