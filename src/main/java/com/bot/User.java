package com.bot;

import java.util.HashMap;
import java.util.HashSet;

public class User {
    private String id;
    private String name;
    public int countOfCities;
    public boolean flag;
    public HashSet<String> answers = new HashSet<String>();
    public HashMap<String, Integer> counterForCities = new HashMap<>();
    public String lastLetter;

    public User(String id, String name, int currentQuestion, boolean start) {
        this.id = id;
        this.setName(name);
        this.countOfCities = currentQuestion;
        this.flag = start;
        this.initializeCounters();
    }

    public void initializeCounters(){
        for(char symb = 'а'; symb<='я'; symb++){
            String convert = String.valueOf(symb);
            this.counterForCities.put(convert, new Integer(0));
        }
    }

    public void setTrue() {
        this.flag = true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void incrementCities() {
        this.countOfCities++;
    }
}
