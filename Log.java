package com.coup;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        System.out.println(element.getOrigin()+ " " +element.getAction() + " " + element.getTarget() + " " + element.getCard());
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
               if (Objects.equals(events.get(i).getAction(), "Exchange") || Objects.equals(events.get(i).getAction(), "RevealedCard")){
                   actions.add(events.get(i).getAction());
                   return actions;
               }else {
                   actions.add(events.get(i).getAction());
               }
           }
       }
       return actions;
   }


}
