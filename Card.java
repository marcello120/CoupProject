package com.coup;

/**
 * Created by Sarosi on 24/10/2017.
 */
public class Card {
    private String name;
    private String action;
    private String description;

    public Card(String name, String action, String description) {
        this.name = name;
        this.action = action;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }


    public String toString(){
        switch (name){
            case "Duke" :
                return ((char)27 + "[35m" + name + (char)27 + "[0m");

            case "Assassin" :
                return ((char)27 + "[33m" + name + (char)27 + "[0m");

            case "Captain" :
                return ((char)27 + "[34m" + name + (char)27 + "[0m");

            case "Ambassador" :
                return ((char)27 + "[32m" + name + (char)27 + "[0m");

            case "Contessa" :
                return ((char)27 + "[31m" + name + (char)27 + "[0m");
            default :
                return name;

        }
    }


}
