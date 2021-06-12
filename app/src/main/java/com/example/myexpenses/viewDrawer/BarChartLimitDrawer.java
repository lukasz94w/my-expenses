package com.example.myexpenses.viewDrawer;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.myexpenses.R;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.valueFormatter.IntValueFormatter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class BarChartLimitDrawer {
    private final Context context;
    private final List<Transaction> monthlyExpenses;
    private final int numberOfDaysInMonth;
    private final int monthlyLimit;

    private final BarChart monthlyBarChart;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarChartLimitDrawer(BarChart monthlyBarChart, Context context, List<Transaction> monthlyExpenses, int numberOfDaysInMonth, int monthlyLimit) {
        this.monthlyBarChart = monthlyBarChart;
        this.context = context;
        this.monthlyExpenses = monthlyExpenses;
        this.numberOfDaysInMonth = numberOfDaysInMonth;
        this.monthlyLimit = monthlyLimit;

        configureBarChartAppearance();
        prepareBarChartDataAndView();
    }

    private void configureBarChartAppearance() {
        monthlyBarChart.getLegend().setEnabled(false);
        monthlyBarChart.getDescription().setEnabled(false);
        monthlyBarChart.getAxisRight().setDrawLabels(false);
        monthlyBarChart.getAxisRight().setDrawAxisLine(false);
        monthlyBarChart.getAxisRight().setDrawGridLines(false);
        LimitLine limitLine = new LimitLine(getValueInCurrency(monthlyLimit), "");
        limitLine.setLineColor(Color.DKGRAY);
        limitLine.setLineWidth(4f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        limitLine.setTextSize(10f);
        monthlyBarChart.getAxisLeft().removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        monthlyBarChart.getAxisLeft().addLimitLine(limitLine);
        monthlyBarChart.getAxisLeft().setDrawAxisLine(false);
        monthlyBarChart.getAxisLeft().setGridColor(context.getResources().getColor(R.color.grid_color)); //color of left Y axis
        monthlyBarChart.getAxisLeft().setTextSize(12);
        monthlyBarChart.getAxisLeft().setXOffset(10);
        monthlyBarChart.getAxisLeft().setValueFormatter(new IntValueFormatter());
        XAxis xAxis = monthlyBarChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(context.getResources().getColor(R.color.grid_color));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(-1);
        xAxis.setTextSize(12);
        if (numberOfDaysInMonth == 31) {
            xAxis.setAxisMaximum(31);
        } else if (numberOfDaysInMonth == 30) {
            xAxis.setAxisMaximum(29.5f); //to last bar look better
        } else xAxis.setAxisMaximum(28); //february leap and non-leap year
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value + 1); //this is not the most elegant way but it works...
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void prepareBarChartDataAndView() {
        List<BarChartLimitDrawer.BarEntryHolder> holderOfBarEntries = new ArrayList<>();
        int numberOfDayInMonth = 0;
        int sumOfExpensesInMonth = 0;
        Calendar calendarToGetDayOfMonth = Calendar.getInstance();

        for (int i = 0; i < monthlyExpenses.size(); i++) {
            Date dateOfTransaction = monthlyExpenses.get(i).getDate();
            calendarToGetDayOfMonth.setTime(dateOfTransaction);
            int dayOfMonth = calendarToGetDayOfMonth.get(Calendar.DAY_OF_MONTH);
            int dayOfTransaction = dayOfMonth - 1; //because at getFormattedValue for xAxis +1 is added

            while (numberOfDayInMonth <= dayOfTransaction) {
                holderOfBarEntries.add(new BarChartLimitDrawer.BarEntryHolder(numberOfDayInMonth, sumOfExpensesInMonth));
                numberOfDayInMonth++;
            }

            numberOfDayInMonth = dayOfTransaction;
            int valueOfExpense = Math.abs(monthlyExpenses.get(i).getAmount()); //abs because expense numbers are stored with '-' sign
            sumOfExpensesInMonth = sumOfExpensesInMonth + valueOfExpense;

            //checking if there is already a transaction with the same date, if so, actualize total sum
            int index = IntStream.range(0, holderOfBarEntries.size())
                    .filter(k -> holderOfBarEntries.get(k).getxVal() == dayOfTransaction)
                    .findFirst()
                    .orElse(-1);
            if (index != -1) {
                holderOfBarEntries.set(index, new BarChartLimitDrawer.BarEntryHolder(dayOfTransaction, sumOfExpensesInMonth));
            } else {
                holderOfBarEntries.add(new BarChartLimitDrawer.BarEntryHolder(dayOfTransaction, sumOfExpensesInMonth));
            }
        }
        //fil rest of months day
        for (int j = numberOfDayInMonth; j < numberOfDaysInMonth; j++) {
            holderOfBarEntries.add(new BarChartLimitDrawer.BarEntryHolder(j, sumOfExpensesInMonth));
        }

        monthlyBarChart.getAxisLeft().setAxisMinimum(-(getValueInCurrency(sumOfExpensesInMonth) * 0.075f)); //make additional space between bottom of the chart and labels
        if (sumOfExpensesInMonth < monthlyLimit) {
            monthlyBarChart.getAxisLeft().setAxisMaximum(getValueInCurrency(monthlyLimit) * 1.075f);
        } else {
            monthlyBarChart.getAxisLeft().setAxisMaximum(getValueInCurrency(sumOfExpensesInMonth) * 1.075f);
        }

        //copy data from barEntryHolder to barEntriesList
        ArrayList barEntriesOfCurrentCategoryInMonth = new ArrayList<>();
        for (int m = 0; m < holderOfBarEntries.size(); m++) {
            barEntriesOfCurrentCategoryInMonth.add(new BarEntry(holderOfBarEntries.get(m).getxVal(), getValueInCurrency(holderOfBarEntries.get(m).getyVal())));
        }

        BarChartLimitDrawer.MyBarDataSet myBarDataSet = new BarChartLimitDrawer.MyBarDataSet(barEntriesOfCurrentCategoryInMonth, "");
        myBarDataSet.setColors(
                ContextCompat.getColor(context, R.color.below_limit),
                ContextCompat.getColor(context, R.color.over_limit));
        myBarDataSet.setDrawValues(false);

        BarData barData = new BarData(myBarDataSet);

        monthlyBarChart.setData(barData);
        monthlyBarChart.invalidate();
    }

    private static class BarEntryHolder {
        int xVal;
        int yVal;

        public BarEntryHolder(int xVal, int yVal) {
            this.xVal = xVal;
            this.yVal = yVal;
        }

        public int getxVal() {
            return xVal;
        }

        public int getyVal() {
            return yVal;
        }
    }

    private class MyBarDataSet extends BarDataSet {
        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            if (getEntryForIndex(index).getY() < getValueInCurrency(monthlyLimit))
                return mColors.get(0);
            else
                return mColors.get(1);
        }
    }
}
