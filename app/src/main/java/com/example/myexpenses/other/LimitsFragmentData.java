package com.example.myexpenses.other;

public class LimitsFragmentData {
    private final int currentChosenMonth;
    private final int currentChosenYear;
    private final int sumOfMonthlyExpenses;

    public LimitsFragmentData(int currentChosenMonth, int currentChosenYear, int sumOfMonthlyExpenses) {
        this.currentChosenMonth = currentChosenMonth;
        this.currentChosenYear = currentChosenYear;
        this.sumOfMonthlyExpenses = sumOfMonthlyExpenses;
    }

    public int getCurrentChosenMonth() {
        return currentChosenMonth;
    }

    public int getCurrentChosenYear() {
        return currentChosenYear;
    }

    public int getSumOfMonthlyExpenses() {
        return sumOfMonthlyExpenses;
    }
}