package com.example.myexpenses.model;

import java.util.Date;

public class Transaction implements Comparable<Transaction> {

    private boolean isSectionHeader;
    private int id;
    private int type;
    private String name;
    private double amount;
    private String category;
    private Date date;

    public Transaction() {
    }

    public Transaction(Date date) {
        this.date = date;
    }

    public Transaction(int type, String name, float amount, String category, Date date) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.isSectionHeader = false;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSectionHeader() {
        return isSectionHeader;
    }

    public void setSectionHeader(boolean isSectionHeader) {
        this.isSectionHeader = isSectionHeader;
    }

    @Override
    public int compareTo(Transaction transaction) {
        return this.date.compareTo(transaction.date);
    }
}
