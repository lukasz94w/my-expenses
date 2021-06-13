package com.example.myexpenses.viewDrawer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class AverageTransactionPerDayDrawer {
    private final Context context;
    private final int idOfAverageTransactionPerDay;
    private final int totalSum;
    private final int numberOfDaysInMonth;

    private TextView monthlyTransactionAverage;

    public AverageTransactionPerDayDrawer(Context context, int idOfAverageTransactionPerDay, int totalSum, int numberOfDaysInMonth) {
        this.context = context;
        this.idOfAverageTransactionPerDay = idOfAverageTransactionPerDay;
        this.totalSum = totalSum;
        this.numberOfDaysInMonth = numberOfDaysInMonth;

        prepareMonthlyTransactionAverageView();
    }

    private void prepareMonthlyTransactionAverageView() {
        monthlyTransactionAverage = new TextView(context);
        monthlyTransactionAverage.setId(idOfAverageTransactionPerDay);
        monthlyTransactionAverage.setTextSize(16);
        @SuppressLint("DefaultLocale") String text = String.format("Average: %.2f / day", getValueInCurrency(totalSum / numberOfDaysInMonth));
        monthlyTransactionAverage.setText(text);
        RelativeLayout.LayoutParams layoutParamsForMonthlyTransactionAverage = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsForMonthlyTransactionAverage.addRule(RelativeLayout.BELOW, 1); // id = 1 -> outerPieChart
        layoutParamsForMonthlyTransactionAverage.setMargins(dpToPx(10), dpToPx(6), 0, 0);
        monthlyTransactionAverage.setLayoutParams(layoutParamsForMonthlyTransactionAverage);
    }

    public TextView getMonthlyTransactionAverage() {
        return monthlyTransactionAverage;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }
}
