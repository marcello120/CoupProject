package com.coup;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;

/**
 * Created by Sarosi on 09/11/2017.
 */
public class Log {

    private ArrayList<Event> events = new ArrayList<>();

    public Log() {
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public int size() {
        return events.size();
    }

    public void add(Event element) {
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println(element.getOrigin().random +" " + element.getOrigin()+ " " +element.getAction() + " " + element.getTarget() + " " + element.getCard() + "  coins: " + element.getOrigin().coins );
        String card = element.getCard();
        //System.out.println("Player " + element.getOrigin().getNumber()+ " " +element.getAction() + " " + "Player " + element.getTarget().getNumber() + " " + printCard(element.getCard()));

        events.add(element);
    }

    public ArrayList<Event> searchForOrigin(Player p){
        ArrayList<Event> selected = new ArrayList<>();
        for (Event e: events) {
            if (e.getOrigin() == p){
                selected.add(e);
            }
        }
        return selected;
    }

    public ArrayList<Event> searchForTarget(Player p){
        ArrayList<Event> selected = new ArrayList<>();
        for (Event e: events) {
            if (e.getTarget() == p){
                selected.add(e);
            }
        }
        return selected;
    }

    public ArrayList<Event> searchForAction(String s){
        ArrayList<Event> selected = new ArrayList<>();
        for (Event e: events) {
            if (Objects.equals(e.getAction(), s)){
                selected.add(e);
            }
        }
        return selected;
    }

    public boolean hasExchangedSince(Event e){
        int i;
        int j= 0;
        Player p = e.getOrigin();
        for ( i = events.size()-1; i >= 0  ; i--) {
           if (events.get(i) == e){
               j = i;
           }
        }
        for (int k = j; k < events.size(); k++) {
            if (events.get(k).getOrigin() == p )
                if (Objects.equals(events.get(k).getAction(), "Exchange") || Objects.equals(events.get(k).getAction(), "Redraw")){
                return true;
                }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Log{" +
                "events=" + events +
                '}';
    }

    public Event getLastEvent(){
        return events.get(events.size()-1);
    }


    public Map<String, Integer> getEventFreq(int playernum){
        Map<String, Long> counts =
                getEventsforPlayer(playernum).stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        Map<String, Integer> result = new LinkedHashMap<>();
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEachOrdered(x -> result.put(x.getKey(),toIntExact(x.getValue())));

        return result;

    }


   public ArrayList<String> getEventsforPlayer(int playernum){
       ArrayList<String> actions = new ArrayList<>();
       for (int i = events.size()-1; i > 0; i--) {
           if (events.get(i).getOrigin().getNumber() == playernum){
               if (Objects.equals(events.get(i).getAction(), "Exchange") || Objects.equals(events.get(i).getAction(), "RevealedCard")|| Objects.equals(events.get(i).getAction(), "PutToTable")){
                   actions.add(events.get(i).getAction());
                   return actions;
               }else {
                   actions.add(events.get(i).getAction());
               }
           }
       }
       return actions;
   }


   public String printCard(String inCard){
       switch (inCard){
           case "Duke" :
               return ((char)27 + "[35m" + inCard + (char)27 + "[0m");

           case "Assassin" :
               return ((char)27 + "[33m" + inCard + (char)27 + "[0m");

           case "Captain" :
               return ((char)27 + "[34m" + inCard + (char)27 + "[0m");

           case "Ambassador" :
               return ((char)27 + "[32m" + inCard + (char)27 + "[0m");

           case "Contessa" :
               return ((char)27 + "[31m" + inCard + (char)27 + "[0m");
           default :
               return inCard;
       }
   }



}
