package com.coup;

/**
 * Created by Sarosi on 09/11/2017.
 */
public class Event {
    private Player origin;
    private Player target;
    private  String action;
    private  String card;


    public Event(Player origin, String action, Player target, String card) {
        this.origin = origin;
        this.target = target;
        this.action = action;
        this.card = card;
    }

    public String getCard() {
        return card;
    }

    public Player getOrigin() {
        return origin;
    }

    public Player getTarget() {
        return target;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "Event{" +
                "origin=" + origin +
                ", target=" + target +
                ", action='" + action + '\'' +
                ", card='" + card + '\'' +
                '}';
    }
}
