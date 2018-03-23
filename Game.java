package com.coup;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sarosi on 24/10/2017.
 */

public class Game {

    //   UnsuccessfullyChallenged
    //   SuccessfullyChallenged

    private Random rand = new Random();

    public Log log = new Log();

    private Deck deck;

    public ArrayList<Player> players;

    public ArrayList<Card> table = new ArrayList<>();

    public  boolean firstTurn = true;

    public int turnNumber = 0;



    public Game(int playerNum, int humanNum) {
        deck = new Deck();
        players = new ArrayList<>();

        for (int i = 0; i < humanNum; i++) {
            players.add(new Human(i,playerNum,this));
        }
        for (int i = humanNum; i < playerNum; i++) {
            players.add(new Player(i, playerNum, this));
        }
        deal();
    }

    public Player turns() {
        boolean over = false;
        int turn = 0;
        addLog(new Event(players.get(0), "Started", players.get(0), "-"));
        while (!over) {
            if (turn >= players.size()) {
                firstTurn = false;
                turn = 0;
                turnNumber++;
            }
            Player active = players.get(turn);
            if (active instanceof Human){
                doHuman(((Human) active).askAction());
            }else{
                doDecide(active);
            }
            turn++;
            if (players.size() == 1) {
                if (players.get(0) instanceof Human){
                    System.out.println("CONGRATS Player " + players.get(0).number + " has won the game");
                }
                over = true;
                System.out.println("game is over");
            }
        }
        return players.get(0);
    }

    public void doDecide(Player p){
        ArrayList<Player> targets = new ArrayList<>(players);
        targets.remove(p);
        if (log.size() == 1){
            doFirst(p);
        }else{
            //doRandom(p);
            doHuman(p.askDo());
        }
    }

    public void doHonestRandom(Player p) {
        ArrayList<Player> targets = new ArrayList<>(players);
      //  targets.remove(p);
        ArrayList<Integer> moves = new ArrayList<>();
        moves.add(0);
        moves.add(1);
        if (p.getCoins() >= 7) {
            moves.add(2);
        }
        if (p.getHand().getHandStringList().contains("Duke")) {
            moves.add(3);
        }
        if (p.getHand().getHandStringList().contains("Assassin") && p.getCoins() >= 3) {
            moves.add(4);
        }
        if (p.getHand().getHandStringList().contains("Ambassador")) {
            moves.add(5);
        }
        if (p.getHand().getHandStringList().contains("Captain")) {
            moves.add(6);
        }
        if (p.getCoins() > 10) {
            moves = new ArrayList<>();
            moves.add(2);
        }
        Integer i = moves.get(new Random().nextInt(moves.size()));
        if (i.equals(0)) {
            income(p);

        } else if (i.equals(1)) {
            foreignAid(p);

        } else if (i.equals(2)) {
            coup(p, getRandomTarget(p));

        } else if (i.equals(3)) {
            tax(p);

        } else if (i.equals(4)) {
            assassinate(p, getRandomTarget(p));

        } else if (i.equals(5)) {
            exchange(p);

        } else if (i.equals(6)) {
            steal(p, getRandomTarget(p));

        }
    }

    public void doHuman(Event e){
        if (e.getAction() == "Income"){
            income(e.getOrigin());
        }
        if (e.getAction() == "ForeignAid"){
            foreignAid(e.getOrigin());
        }
        if (e.getAction() == "Coup"){
            coup(e.getOrigin(),e.getTarget());
        }
        if (e.getAction() == "Tax"){
            tax(e.getOrigin());
        }
        if (e.getAction() == "Assassinate"){
            assassinate(e.getOrigin(), e.getTarget());
        }
        if (e.getAction() == "Exchange"){
            exchange(e.getOrigin());
        }
        if (e.getAction() == "Steal"){
            steal(e.getOrigin(), e.getTarget());
        }
    }


    public void doRandom(Player p) {
        ArrayList<Player> targets = new ArrayList<>(players);
        targets.remove(p);
        switch (new Random().nextInt(7)) {
            case 0:
                income(p);
                break;
            case 1:
                foreignAid(p);
                break;
            case 2:
               // p.getCoupTarget();
                coinOrder(p);
                coup(p, targets.get(new Random().nextInt(targets.size())));
                break;
            case 3:
                tax(p);
                break;
            case 4:
                assassinate(p, targets.get(new Random().nextInt(targets.size())));
                break;
            case 5:
                exchange(p);
                break;
            case 6:
                steal(p, targets.get(new Random().nextInt(targets.size())));
                break;
        }
    }


    public void deal() {
        for (Player p : players) {
            p.setHand(new Hand(deck.draw(), deck.draw()));
            p.setCoins(2);
            p.originalHand = new Hand(p.getHand().getCardOne(), p.getHand().getCardTwo());
        }
        gameState();
    }

    public void income(Player p) {
        p.addCoins(1);
        addLog(new Event(p, "Income", p, "-"));
    }

    public void foreignAid(Player p) {
        addLog(new Event(p, "TryingToForeignAid", p, "-"));
        String c = blockAid(p);
        if (Objects.equals(c, "NoBlock") || Objects.equals(c, "SuccessfullyChallenged")) {
            p.addCoins(2);
            addLog(new Event(p, "ForeignAid", p, "-"));
        } else {
            addLog(new Event(p, "DidNotForeignAid", p, "-"));
        }
    }

    public void coup(Player origin, Player target) {
        if (origin.getCoins() >= 7) {
            origin.removeCoins(7);
            Card c = makePlayerLoseCard(target);
            addLog(new Event(origin, "Coup", target, c.getName()));
        } else {
            System.out.println("not enough coins for coup");
        }

    }

    public void tax(Player p) {
        Event e = new Event(p, "Tax", p, "Duke");
        addLog(new Event(p,"TryingToTax",p,"Duke"));
        if (Objects.equals(askChallenge(e), "SuccessfullyChallenged")) {
            addLog(new Event(p, "DidNotTax", p, "-"));
        } else {
            System.out.println("Coins were added");
            p.addCoins(3);
            System.out.println(p.getCoins());
            addLog(e);
        }
    }

    public boolean assassinate(Player origin, Player target) {
        if (origin.getCoins() >= 3) {
            addLog(new Event(origin, "TryingToAssassinate", target, "Assassin"));
            origin.removeCoins(3);
            Event e = new Event(origin, "Assassinate", target, "Assassin");
            String challenge = askChallenge(e);
            if (challenge == "SuccessfullyChallenged") {
                addLog(new Event(origin, "DidNotAssassinate", target, "Assassin"));
                return false;
            }
            if (challenge == "UnsuccessfullyChallenged" && log.getLastEvent().getAction() == "RevealedCard" && log.getLastEvent().getTarget() == target && log.getLastEvent().getOrigin() == origin) {
                // target cannot block
                makePlayerLoseCard(target);
                addLog(e);
            } else {
                String c = blockAssassination(origin, target);
                if (c == "NoBlock" || c == "SuccessfullyChallenged") {
                    addLog(e);
                    makePlayerLoseCard(target);
                }
                if (c == "NoChallenge" || c == "UnsuccessfullyChallenged") {
                    addLog(new Event(origin, "DidNotAssassinate", target, "Assassin"));
                    return false;

                }
            }
            return true;
        }else{
            System.out.println("not enough coins for assassination");
            return false;
        }
    }



// lifeline, stayin alive

//        if (target.getHand().getInfuence() == 1){
//            String lifeline = target.stayinAlive(origin);
//            if (lifeline == "Block"){
//                if (!askChallenge(new Event(target, "BlockAssassination", origin, "Contessa"))) {
//                    return true;
//                }
//            }else{
//                if (target.getHand().getInfuence() != 0) {
//                    makePlayerLoseCard(target);
//                    log.add(new Event(origin, "Assassinate", target, "Assassin"));
//                }
//            }
//            if (lifeline == "Challenge"){
//                if (arbiter(target, "Assassin",origin)){
//                    return false;
//                }else{
//                    if (target.getHand().getInfuence() != 0) {
//                        makePlayerLoseCard(target);
//                        log.add(new Event(origin, "Assassinate", target, "Assassin"));
//                    }
//                }
//            }
//        }

    public void exchange(Player p) {
        addLog(new Event(p, "TryingToExchange", p, "Ambassador"));
        String c = askChallenge(new Event(p, "Exchange", p, "Ambassador"));
        if (c == "NoChallenge" || c == "UnsuccessfullyChallenged") {
            ArrayList<Card> temp;
            temp = p.exchange(deck.draw(), deck.draw());
            for (int i = 0; i < temp.size(); i++) {
                deck.add(temp.get(i));
            }
            deck.shuffle();
            addLog(new Event(p, "Exchange", p, "Ambassador"));
        }

    }

    public void steal(Player origin, Player target) {
        addLog(new Event(origin, "TryingToSteal", target, "Captain"));
        Event e = new Event(origin, "Steal", target, "Captain");
        String challenge = askChallenge(e);
        if (challenge == "SuccessfullyChallenged") {
            addLog(new Event(origin, "DidNotSteal", target, "Captain"));
            return;
        }
        if (challenge == "UnsuccessfullyChallenged" && log.getLastEvent().getAction() == "RevealedCard" && log.getLastEvent().getTarget() == target && log.getLastEvent().getOrigin() == origin) {
            // target cannot block
            doSteal(origin,target);
        }
        else {
            String c = blockSteal(origin, target);
            if (c == "NoBlock" || c == "SuccessfullyChallenged") {
                doSteal(origin, target);
            }
            if (c == "NoChallenge" || c == "UnsuccessfullyChallenged") {
                addLog(new Event(origin, "DidNotSteal", target, "Assassin"));
            }
        }
    }


    public void doSteal(Player origin, Player target) {
        addLog(new Event(origin, "Steal", target, "Captain"));
        if (target.getCoins() == 1) {
            target.removeCoins(1);
            origin.addCoins(1);
        } else if (target.getCoins() == 0) {
        } else {
            target.removeCoins(2);
            origin.addCoins(2);
        }
    }

    public Card makePlayerLoseCard(Player target) {
        if (players.contains(target)) {
            System.out.println(target.getHand());
            Card c = target.loseCard();
            table.add(c);
            target.getHand().loseInfuence();
            if (target.getHand().getInfuence() < 1) {
                System.out.println("Player was removed.");
                players.remove(target);
                System.out.println(target + "has lost");
                addLog(new Event(target, "PutToTable", target, c.getName()));
            }
            for (Player p: players) {
                if (p != target){
                    p.removeException(c.getName(), target.getNumber());
                }
            }
            updateAllKnowns();
            return c;
        }else{
            return null;
        }
    }


    public void gameState() {
        System.out.println("Gamestate:");
        System.out.println("Number of players = " + players.size());
        System.out.println(players);
        System.out.println(deck);
        System.out.println("----------------------------");
    }

    public Player getPlayerByNumber(int a) {
        Player ret = null;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getNumber() == a) {
                ret = players.get(i);
            }
        }
        return ret;
    }

    public String blockAid(Player p) {
        ArrayList<Player> blockers = new ArrayList<>(players);
        blockers.remove(p);
        Player blocker = null;
        for (Player op : blockers) {
            if (op.askBlockAid(p)) {
                blocker = op;
                break;
            }
        }
        if (blocker != null) {
            addLog(new Event(blocker, "TryingToBlockForeignAid", p, "Duke"));
            String c = askChallenge(new Event(blocker, "BlockForeignAid", p, "Duke"));
            if (c == "SuccessfullyChallenged") {
                addLog(new Event(blocker, "DidNotBlockForeignAid", p, "Duke"));
                return "SuccessfullyChallenged";
            } else {
                addLog(new Event(blocker, "BlockForeignAid", p, "Duke"));
                if(c == "NoChallenge"){
                    return "NoChallenge";
                }else{
                    return "UnsuccessfullyChallenged";
                }
            }
        }
        return "NoBlock";

    }

    public String blockAssassination(Player origin, Player target) {
        if (target.askBlockAssassinWithContessa(origin)) {
            addLog(new Event(target, "TryingToBlockAssassination", origin, "Contessa"));
            String c = askChallenge(new Event(target, "BlockAssassination", origin, "Contessa"));
            if (c == "SuccessfullyChallenged") {

            } else {
                addLog(new Event(target, "BlockAssassination", origin, "Contessa"));
            }
            return c;

        } else {
            return "NoBlock";
        }
    }

    public String blockSteal(Player origin, Player target) {
        String decision = target.askBlockSteal(origin);
        if (decision != null) {
            addLog(new Event(target, "TryingToBlockStealWith" + decision, origin, decision));
            String c = askChallenge(new Event(target, "BlockStealWith" + decision, origin, decision));
            if ( c == "SuccessfullyChallenged") {
                addLog(new Event(target, "DidNotBlockStealWith" + decision, origin, decision));
                return "SuccessfullyChallenged";
            } else if(c == "UnsuccessfullyChallenged") {
                addLog(new Event(target, "BlockStealWith" + decision, origin, decision));
                return "UnsuccessfullyChallenged";
            }else{
                addLog(new Event(target, "BlockStealWith" + decision, origin, decision));
                return c;
            }
        } else {
            return "NoBlock";
        }
    }

    public String askChallenge(Event e) {
        ArrayList<Player> challengers = new ArrayList<>(players);
        challengers.remove(e.getOrigin());
        Player inquisitor = null;
        String card = null;
        for (Player c : challengers) {
            if (c.getNumber()!= e.getOrigin().getNumber()){
                card = c.askChallenge(e);
                if (card != null) {
                    inquisitor = c;
                    break;
                }
            }
        }
        if (inquisitor != null) {
            addLog(new Event(inquisitor, "challenge", e.getOrigin(), card));
            return arbiter(inquisitor, card, e.getOrigin());
        } else {
            return "NoChallenge";
        }

    }

    public String arbiter(Player challenger, String card, Player challenged) {
        if (challenged.getHand().contains(card)) {
            makePlayerLoseCard(challenger);
            deck.add(challenged.redraw1(card));
            challenged.redraw2(deck.draw());
            addLog(new Event(challenger, "UnsuccessfullyChallenged", challenged, card));
            addLog(new Event(challenged, "RevealedCard", challenger, card));
            return "UnsuccessfullyChallenged";

        } else {
            makePlayerLoseCard(challenged);
            addLog(new Event(challenger, "SuccessfullyChallenged", challenged, card));
            return "SuccessfullyChallenged";
        }
    }

    private void doFirst(Player p){
        System.out.println(p.getHand().cardsAsBits());
        if (p.getHand().contains("Duke")){
            tax(p);
        }else if(Objects.equals(p.getHand().cardsAsBits(), "01000") || Objects.equals(p.getHand().cardsAsBits(), "01100") || Objects.equals(p.getHand().cardsAsBits(), "00100")){
            steal(p,getRandomTarget(p));
        } else if(players.size() >= 5 && rand.nextBoolean() && ((Objects.equals(p.getHand().cardsAsBits(), "10100"))|| Objects.equals(p.getHand().cardsAsBits(), "00110") || Objects.equals(p.getHand().cardsAsBits(), "10000"))){
            exchange(p);
        } else if(Objects.equals(p.getHand().cardsAsBits(), "10000") || Objects.equals(p.getHand().cardsAsBits(), "10010")){
            income(p);
        }else if(Objects.equals(p.getHand().cardsAsBits(), "11000") || Objects.equals(p.getHand().cardsAsBits(), "10100")){
            tax(p);
        }else if(rand.nextBoolean() && (Objects.equals(p.getHand().cardsAsBits(), "00010") || Objects.equals(p.getHand().cardsAsBits(), "00110") || Objects.equals(p.getHand().cardsAsBits(), "01010"))){
            tax(p);
        }else{
            income(p);
        }
    }

    public Player getRandomTarget(Player p){
        ArrayList<Player> targets = new ArrayList<>(players);
        targets.remove(p);
        return targets.get(rand.nextInt(targets.size()));
    }

    public void getAllCards(){
        ArrayList<String> a = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getHand().getCardOne()!= null){
                a.add(players.get(i).getHand().getCardOne().getName());
            }
            if (players.get(i).getHand().getCardTwo()!= null){
                a.add(players.get(i).getHand().getCardTwo().getName());
            }
        }
        if(deck.cards.size() < 3){
            System.out.println("hinye!");
        }

        Map<String, Long> counts =
                a.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        for (Map.Entry<String, Long> entry : counts.entrySet()) {
           if(entry.getValue() > 3){
               System.out.println("problem");
           }
        }

    }

    public void notify(Event e){
        for (Player p: players) {
            p.update(e);
        }
    }

    public void addLog(Event e){
        log.add(e);
        notify(e);
    }

    public ArrayList<String> getTableString(){
        ArrayList<String> ret = new ArrayList<>();
        for (Card c: table) {
            ret.add(c.getName());
        }
        return ret;
    }
    public void updateAllKnowns(){
        for (Player p: players) {
            p.updateKnown();
        }
    }

    public ArrayList<Player> coinOrder(Player p){
        ArrayList<Player> options = new ArrayList<>(players);
        options.remove(p);
        options.sort((o1, o2) -> {
            if (o1.coins == o2.coins)
                return 0;
            return o1.coins < o2.coins ? 1 : -1;
        });
        return options;
    }



}
