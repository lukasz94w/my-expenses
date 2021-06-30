package com.example.myexpenses.other;

public class SearchFilter {
    private int transactionsType;
    private String transactionsName;
    private String transactionsCategory;
    private int transactionsOrder;

    public SearchFilter(int transactionsType, String transactionsName, String transactionsCategory, int transactionsOrder) {
        this.transactionsType = transactionsType;
        this.transactionsName = transactionsName;
        this.transactionsCategory = transactionsCategory;
        this.transactionsOrder = transactionsOrder;
    }

    public int getTransactionsOrder() {
        return transactionsOrder;
    }

    public int getTransactionsType() {
        return transactionsType;
    }

    public String getTransactionsName() {
        return transactionsName;
    }

    public String getTransactionsCategory() {
        return transactionsCategory;
    }

    public void setTransactionsType(int transactionsType) {
        this.transactionsType = transactionsType;
    }

    public void setTransactionsName(String transactionsName) {
        this.transactionsName = transactionsName;
    }

    public void setTransactionsCategory(String transactionsCategory) {
        this.transactionsCategory = transactionsCategory;
    }

    public void setTransactionsOrder(int transactionsOrder) {
        this.transactionsOrder = transactionsOrder;
    }
}
