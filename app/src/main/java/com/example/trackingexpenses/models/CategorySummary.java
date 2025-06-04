package com.example.trackingexpenses.models;

public class CategorySummary {
    private String categoryName;
    private double amount;
    private float percentage;

    public CategorySummary() {
        // Required empty constructor for Firebase
    }

    public CategorySummary(String categoryName, double amount, float percentage) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.percentage = percentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
