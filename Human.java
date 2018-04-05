package com.coup;

import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by Sarosi on 13/02/2018.
 */
public class Human extends Player {

    public Human(){
        super();
    }

    public Human(int number, int playernum, Game g) {
       super(number, playernum,g);
    }

    public void EnemiesInfo(){
        for (Player p: containingGame.players) {
            if (p.getNumber()!= this.getNumber()){
                System.out.println("Player " + p.getNumber()+" has " + p.getHand().getInfuence() +" cards and " + p.getCoins() + " coins.");
            }
        }
    }

    public void showTable(){
        System.out.print("The cards on the table are: ");
        for (Card c : containingGame.table) {
            System.out.print(" " + c.getName() + ",");
        }
    }
    public void showHand(){
        System.out.print("There are " + getHand().getHandStringList().size() + " cards in your hand:" );
        for (String s: getHand().getHandStringList()) {
            System.out.print(" "+ s + ",");
        }
    }
    public void showCoins(){
        System.out.println("You have " + super.coins + " coins.");
    }

    public ArrayList<Card> exchange(Card draw, Card draw1) {
        ArrayList<Card> options = new ArrayList<>();
        ArrayList<Card> ret = new ArrayList<>();

        options.add(draw);
        options.add(draw1);
        options.addAll(getHand().getHandList());
        System.out.println("Drew cards: " + draw.getName() + " and "+ draw1.getName());
        System.out.println("Choose a cards to reshuffle");
        int num = 0;
        if (options.size() == 4){
            System.out.print("Press 1 for " + options.get(0) + ", 2 for " + options.get(1) + ", 3 for " + options.get(2) + ", 4 for " + options.get(3) );
            Scanner reader = new Scanner(System.in);
            int n = reader.nextInt();
            while (n>4 || n<1){
                System.out.println("Incorrect input");
                System.out.print("Press 1 for " + options.get(0) + ", 2 for " + options.get(1) + ", 3 for " + options.get(2) + ", 4 for " + options.get(3) );
                reader = new Scanner(System.in);
                n = reader.nextInt();
            }
            ret.add(options.remove(n-1));
            System.out.println("Choose an other cards to reshuffle");
            System.out.print("Press 1 for " + options.get(0) + ", 2 for " + options.get(1) + ", 3 for " + options.get(2));
            reader = new Scanner(System.in);
            n = reader.nextInt();
            while (n>3 || n<1){
                System.out.println("Incorrect input");
                System.out.print("Press 1 for " + options.get(0) + ", 2 for " + options.get(1) + ", 3 for " + options.get(2));
                reader = new Scanner(System.in);
                n = reader.nextInt();
            }
            ret.add(options.remove(n-1));
        }

        if (options.size() == 3){
            System.out.print("Press 1 for " + options.get(0) + ", 2 for " + options.get(1) + ", 3 for " + options.get(2));
            Scanner reader = new Scanner(System.in);
            int n = reader.nextInt();
            while (n>3 || n<1){
                System.out.println("Incorrect input");
                System.out.print("Press 1 for " + options.get(0) + ", 2 for " + options.get(1) + ", 3 for " + options.get(2));
                reader = new Scanner(System.in);
                n = reader.nextInt();
            }
            ret.add(options.remove(n-1));
            System.out.println("Choose an other cards to reshuffle");
            System.out.print("Press 1 for " + options.get(0) + ", 2 for " + options.get(1));
            reader = new Scanner(System.in);
            n = reader.nextInt();
            while (n>2 || n<1){
                System.out.println("Incorrect input");
                System.out.print("Press 1 for " + options.get(0) + ", 2 for " + options.get(1));
                reader = new Scanner(System.in);
                n = reader.nextInt();
            }
            ret.add(options.remove(n-1));
        }

        if (getHand().getInfuence() == 1){
            if (getHand().getCardOne() != null){
                getHand().setCardOne(options.get(0));
            }else {
                getHand().setCardTwo(options.get(0));
            }
        }else {
            getHand().setCardOne(options.get(0));
            getHand().setCardTwo(options.get(1));
        }

        showHand();

        return ret;
    }

    public boolean askBlockAid(Player p) {
        System.out.println("Player " + number + ": " +"Player " + p.getNumber() + " is attempting to foreign aid. Would you like to block? (Y/N)");
        return decider();
    }

    public boolean askBlockAssassinWithContessa(Player origin) {
        System.out.println("Player " + number + ": " +"Player " + origin.getNumber() + " is attempting to Assassinate you. Would you like to block with Contessa? (Y/N)");
         return decider();
    }

    public String askBlockSteal(Player origin) {
        System.out.println("Player " + number + ": " +"Player " + origin.getNumber() + " is attempting to Steal from  you. Would you like to block it? (Y/N)");
        if (decider()) {
            System.out.println("Press 1 to block with Ambassador, Press 2 to block with Captain");
            Scanner reader = new Scanner(System.in);
            int n = reader.nextInt();
            while (n > 2 || n < 1) {
                System.out.println("Incorrect input");
                System.out.println("Press 1 to block with Ambassador, Press 2 to block with Captain");
                reader = new Scanner(System.in);
                n = reader.nextInt();
            }

            if (n == 1) {
                return "Ambassador";
            } else {
                return "Captain";
            }
        }
        return null;
    }

    public String askChallenge(Event e) {
        System.out.println("Player " + number + ": " + "Player " + e.getOrigin().getNumber() + " is trying to " + e.getAction() + " with target Player " + e.getTarget().getNumber() + ". Would you like to challenge this action? (Y/N)") ;

        if (decider()){
            return e.getCard();
        }else{
            return null;
        }
    }

    public Event askAction(){
        if (coins > 10){
            System.out.println("Player " + getNumber() + ", it is your turn. Your current number of coins is " + coins + " which is over the limit of 10. You must coup this turn!"   );
            Player target = target();
            return new Event(this, "Coup", target, "-");
        }
        System.out.println("Player " + getNumber() + ", it is your turn. Choose an action. Type Options for help.Hand: " + getHand().getHandStringList() + " Coins: " + super.getCoins());
        Scanner reader = new Scanner(System.in);
        String in = reader.nextLine();
        System.out.println(in);
        boolean action = false;
        while(!action){
            if (Objects.equals(in, "ShowHand") || Objects.equals(in, "Hand")){
                showHand();
            }
            else if (Objects.equals(in, "ShowCoins")|| Objects.equals(in, "Coins")){
                showCoins();
            }
            else if (Objects.equals(in, "ShowTable") || Objects.equals(in, "Table")) {
                showTable();
            }
            else if (Objects.equals(in, "EnemiesInfo") || Objects.equals(in, "Enemies")) {
                EnemiesInfo();
            }
            else if (Objects.equals(in, "Options")) {
                System.out.println("Options are: Income, ForeignAid, Coup, Tax, Assassinate, Exchange, Steal, ShowHand, ShowCoins, ShowTable, EnemiesInfo");
            }
            else if (Objects.equals(in, "Income")) {
                return new Event(this, "Income",this, "-");
            }
            else if (Objects.equals(in, "ForeignAid")) {
                return new Event(this, "ForeignAid",this, "-");
            }
            else if (Objects.equals(in, "Tax")) {
                return new Event(this, "Tax",this, "Duke");
            }
            else if (Objects.equals(in, "Exchange")) {
                return new Event(this, "Exchange",this, "-");
            }
            else if (Objects.equals(in, "Coup")) {
                if (getCoins()<7){
                    System.out.println("Not enough coins for Coup");
                }else{
                  Player target =  target();
                  return new Event(this, "Coup", target, "-");
                }
            }
            else if (Objects.equals(in, "Assassinate")) {
                if (getCoins()<3){
                    System.out.println("Not enough coins for Assassination");
                }else{
                    Player target =  target();
                    return new Event(this, "Assassinate", target, "Assassin");
                }
            }
            else if (Objects.equals(in, "Steal")) {
                    Player target =  target();
                    return new Event(this, "Steal", target, "Captain");

            }else {
                System.out.println("Incorrect Input");
                System.out.println("Player " + getNumber() + ", it is your turn. Please give the action you wish to take. Type Options for help.");

            }
            reader = new Scanner(System.in);
            in = reader.nextLine();
        }
        return new Event(this, "Income",this,"Jajaj");
    }


    public Player target(){
        ArrayList<Integer> ints = new ArrayList<>();
        System.out.println("Please select a target player by entering their number");
        System.out.print("Players in game are: ");
        for (Player p: containingGame.players) {
            if (p.getNumber() != number){
                System.out.print("Player number " + p.getNumber() + ", ");
                ints.add(p.getNumber());
            }
        }
        Scanner reader = new Scanner(System.in);
        String in = reader.nextLine();
        while(!ints.contains(Integer.parseInt(in))){
            System.out.println("Incorrect input.");
            System.out.println("Please select a target player by entering their number");
            System.out.print("Players in game are: ");
            for (Player p: containingGame.players) {
                if (p != this){
                    System.out.print("Player number " + p.getNumber() + ", ");
                }
            }
        }
        return containingGame.getPlayerByNumber(Integer.parseInt(in));

    }


    public boolean decider(){
        Scanner reader = new Scanner(System.in);
        String in = reader.nextLine();
        while(!Objects.equals(in, "Y") && !Objects.equals(in, "N")){
            if (Objects.equals(in, "ShowHand") || Objects.equals(in, "Hand")){
                showHand();
            }
            else if (Objects.equals(in, "ShowCoins")|| Objects.equals(in, "Coins")){
                showCoins();
            }
            else if (Objects.equals(in, "ShowTable") || Objects.equals(in, "Table")) {
                showTable();
            }
            else if (Objects.equals(in, "EnemiesInfo") || Objects.equals(in, "Enemies")) {
                EnemiesInfo();
            }else{
                System.out.println("Incorrect input!");
            }
             reader = new Scanner(System.in);
             in = reader.nextLine();
        }
        return Objects.equals(in, "Y");
    }

    public Card loseCard() {
        System.out.println("Player number " + number + " you must loose a card");
        if (getHand().getInfuence() == 2) {
            System.out.println("Press 1 to loose " + getHand().getCardOne() + ". Press 2 to loose " + getHand().getCardTwo());
            Scanner reader = new Scanner(System.in);
            int n = reader.nextInt();
            while (n!=1 && n!=2){
                System.out.println("Incorrect input");
                System.out.println("Press 1 to loose " + getHand().getCardOne()+ ". Press 2 to loose " + getHand().getCardTwo());
                reader = new Scanner(System.in);
                n = reader.nextInt();

            }
            Card ret;
            if (n == 1){
               ret = getHand().getCardOne();
               getHand().setCardOne(null);
            }else{
                ret = getHand().getCardTwo();
                getHand().setCardTwo(null);
            }
            return ret;

        } else {
            return getHand().getHandList().get(0);
        }
    }


}
