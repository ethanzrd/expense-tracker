package com.example.trackingexpenses.models;

import java.util.Date;

public class Entry {
    private String entryId;
    private String type; // income or expense
    private double amount;
    private String category;
    private String date;
    private String note;

    // Required empty constructor for Firebase
    public Entry() {
    }

    public Entry(String entryId, String type, double amount, String category, String date, String note) {
        this.entryId = entryId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
