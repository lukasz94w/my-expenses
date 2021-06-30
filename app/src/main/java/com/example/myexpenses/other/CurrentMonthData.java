package com.example.myexpenses.other;

import android.text.SpannableStringBuilder;

public class CurrentMonthData {
    private final int currentChosenMonth;
    private final int currentChosenYear;
    private final SpannableStringBuilder formattedTotalSum;

    public CurrentMonthData(int currentChosenMonth, int currentChosenYear, SpannableStringBuilder formattedTotalSum) {
        this.currentChosenMonth = currentChosenMonth;
        this.currentChosenYear = currentChosenYear;
        this.formattedTotalSum = formattedTotalSum;
    }

    public int getCurrentChosenMonth() {
        return currentChosenMonth;
    }

    public int getCurrentChosenYear() {
        return currentChosenYear;
    }

    public SpannableStringBuilder getFormattedTotalSum() {
        return formattedTotalSum;
    }
}
