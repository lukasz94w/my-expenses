package com.example.myexpenses.other;

public class CurrencyConverter {

    public static float getValueInCurrency(int subUnit) {
        return subUnit / 100f;
    }

    public static int getValueInSubUnit(float currency) {
        return Math.round(currency * 100);
    }
}
