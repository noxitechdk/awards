package dk.noxitech.awards.models;

import java.util.List;

public class Award {
    private int time;
    private String type;
    private double money;
    private List<String> items;
    private String message;

    public Award() {}

    public Award(int time, String type, double money, List<String> items, String message) {
        this.time = time;
        this.type = type;
        this.money = money;
        this.items = items;
        this.message = message;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}