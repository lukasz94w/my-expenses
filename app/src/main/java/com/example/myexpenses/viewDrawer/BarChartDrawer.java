package com.example.myexpenses.viewDrawer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.myexpenses.R;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.valueFormatter.IntValueFormatter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class BarChartDrawer {
    private final Context context;
    private final List<Transaction> transactionsGroupedByCategory;
    private final int numberOfDaysInMonth;
    private final int numberOfBarChartDrawn;
    private final int idOfFirstElementOfBarChart;

    private View separatorLine;
    private ImageView categoryImage;
    private TextView categoryName;
    private ImageView moneyIcon;
    private TextView totalAmountForMonth;
    private BarChart barChart;
    private TextView averageForMonth;

    private int sumOfTransactionsOfCurrentCategoryInMonth;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarChartDrawer(Context context, List<Transaction> transactionsGroupedByCategory, int numberOfBarChartDrawn, int numberOfDaysInMonth, int idOfFirstElementOfBarChart) {
        this.context = context;
        this.transactionsGroupedByCategory = transactionsGroupedByCategory;
        this.numberOfBarChartDrawn = numberOfBarChartDrawn;
        this.numberOfDaysInMonth = numberOfDaysInMonth;
        this.idOfFirstElementOfBarChart = idOfFirstElementOfBarChart;

        configureBarChartAppearance();
        prepareBarChartData(); //it needs to be there
        prepareSeparatorLineView();
        prepareCategoryImageView();
        prepareCategoryNameView();
        prepareMoneyIconView();
        prepareTotalAmountForMonthView();
        prepareAverageForMonthView();
        prepareBarChartView();
    }

    private void configureBarChartAppearance() {
        //configure barChart appearance
        barChart = new BarChart(context);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.getAxisLeft().setGridColor(context.getResources().getColor(R.color.grid_color)); //color of left Y axis
        barChart.getAxisLeft().setTextSize(12);
        barChart.getAxisLeft().setXOffset(10);
        barChart.getAxisLeft().setValueFormatter(new IntValueFormatter());
        XAxis xAxis = barChart.getXAxis();
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
    private void prepareBarChartData() {
        List<BarEntryHolder> holderOfBarEntries = new LinkedList<>();
        sumOfTransactionsOfCurrentCategoryInMonth = 0;
        int maximumTransactionValueOfCurrentCategoryInMonth = 0;

        for (int j = 0; j < transactionsGroupedByCategory.size(); j++) {

            Calendar calendarToGetDayOfMonth = Calendar.getInstance();
            Date dateOfTransaction = transactionsGroupedByCategory.get(j).getDate();
            calendarToGetDayOfMonth.setTime(dateOfTransaction);
            int dayOfMonth = calendarToGetDayOfMonth.get(Calendar.DAY_OF_MONTH);

            int dayOfTransaction = dayOfMonth - 1; //because at getFormattedValue for xAxis +1 is added
            int valueOfTransaction = Math.abs(transactionsGroupedByCategory.get(j).getAmount()); //abs because expense numbers are stored with '-' sign

            //checking if there is already a transaction with the same date, if so, actualize total sum
            int index = IntStream.range(0, holderOfBarEntries.size())
                    .filter(k -> holderOfBarEntries.get(k).getxVal() == dayOfTransaction)
                    .findFirst()
                    .orElse(-1);
            if (index != -1) {
                int currentDayTotalSumOfTransactions = holderOfBarEntries.get(index).getyVal();
                holderOfBarEntries.set(index, new BarChartDrawer.BarEntryHolder(dayOfTransaction, currentDayTotalSumOfTransactions + valueOfTransaction));
            } else {
                holderOfBarEntries.add(new BarChartDrawer.BarEntryHolder(dayOfTransaction, valueOfTransaction));
            }

            if (valueOfTransaction > maximumTransactionValueOfCurrentCategoryInMonth) {
                maximumTransactionValueOfCurrentCategoryInMonth = valueOfTransaction;
            }
            sumOfTransactionsOfCurrentCategoryInMonth = sumOfTransactionsOfCurrentCategoryInMonth + valueOfTransaction;
        }
        barChart.getAxisLeft().setAxisMinimum(-(getValueInCurrency(maximumTransactionValueOfCurrentCategoryInMonth) * 0.075f)); //make additional space between bottom of the chart and labels
        //copy data from barEntryHolder to barEntriesList
        List<BarEntry> barEntriesOfCurrentCategoryInMonth = new LinkedList();
        for (int m = 0; m < holderOfBarEntries.size(); m++) {
            barEntriesOfCurrentCategoryInMonth.add(new BarEntry(holderOfBarEntries.get(m).getxVal(), getValueInCurrency(holderOfBarEntries.get(m).getyVal())));
        }

        BarDataSet barDataSet = new BarDataSet(barEntriesOfCurrentCategoryInMonth, "");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barDataSet.setValueTextSize(6);
        barDataSet.setValueFormatter(new IntValueFormatter()); //value without decimal points
        barDataSet.setValueTextColor(Color.BLACK);

        //prepare color for category bars
        String barColorName = (transactionsGroupedByCategory.get(0).getCategory() + "_color").toLowerCase().replace(" ", "_");
        int barColorId = context.getResources().getColor(context.getResources().getIdentifier(barColorName, "color", context.getPackageName()));
        barDataSet.setColors(barColorId);
    }

    private void prepareSeparatorLineView() {
        //start drawing barChart
        separatorLine = new View(context);
        separatorLine.setId(numberOfBarChartDrawn * 10 + idOfFirstElementOfBarChart);
        RelativeLayout.LayoutParams separatorLineLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1));
        separatorLineLayout.setMargins(dpToPx(0), dpToPx(10), dpToPx(0), dpToPx(7));
        if (numberOfBarChartDrawn == 0) {
            separatorLineLayout.addRule(RelativeLayout.BELOW, idOfFirstElementOfBarChart - 1); //if it's first barChart draw line under monthlyTransactionAverage (id = 2)
        } else {
            separatorLineLayout.addRule(RelativeLayout.BELOW, (numberOfBarChartDrawn - 1) * 10 + 9); //draw liner under previous barChart
        }
        separatorLine.setBackgroundColor(context.getResources().getColor(R.color.separator_color));
        separatorLine.setLayoutParams(separatorLineLayout);
    }

    private void prepareCategoryImageView() {
        categoryImage = new ImageView(context);
        categoryImage.setId(numberOfBarChartDrawn * 10 + idOfFirstElementOfBarChart + 1);
        String categoryImageResource = transactionsGroupedByCategory.get(0).getCategory().toLowerCase().replace(" ", "_"); //prepare R.drawable.name: toLowerCase() because Android restrict Drawable filenames to not use Capital letters in their names, and also simple replace
        int res = context.getResources().getIdentifier(categoryImageResource, "drawable", context.getPackageName());
        categoryImage.setImageResource(res);
        RelativeLayout.LayoutParams layoutParamsForCategoryImage = new RelativeLayout.LayoutParams(dpToPx(35), dpToPx(35));
        layoutParamsForCategoryImage.addRule(RelativeLayout.BELOW, separatorLine.getId());
        layoutParamsForCategoryImage.setMargins(dpToPx(10), dpToPx(0), 0, 0);
        categoryImage.setLayoutParams(layoutParamsForCategoryImage);
    }

    private void prepareCategoryNameView() {
        categoryName = new TextView(context);
        categoryName.setId(numberOfBarChartDrawn * 10 + idOfFirstElementOfBarChart + 2);
        categoryName.setTextSize(18);
        categoryName.setText(transactionsGroupedByCategory.get(0).getCategory());
        RelativeLayout.LayoutParams layoutParamsForCategoryName = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsForCategoryName.addRule(RelativeLayout.BELOW, separatorLine.getId());
        layoutParamsForCategoryName.addRule(RelativeLayout.RIGHT_OF, categoryImage.getId());
        layoutParamsForCategoryName.setMargins(dpToPx(8), dpToPx(7), 0, 0);
        categoryName.setLayoutParams(layoutParamsForCategoryName);
    }

    private void prepareMoneyIconView() {
        moneyIcon = new ImageView(context);
        moneyIcon.setId(numberOfBarChartDrawn * 10 + idOfFirstElementOfBarChart + 3);
        moneyIcon.setImageResource(R.drawable.money);
        RelativeLayout.LayoutParams layoutParamsForMoneyIcon = new RelativeLayout.LayoutParams(dpToPx(28), dpToPx(28));
        layoutParamsForMoneyIcon.addRule(RelativeLayout.BELOW, separatorLine.getId());
        layoutParamsForMoneyIcon.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParamsForMoneyIcon.setMargins(0, dpToPx(5), dpToPx(3), 0);
        moneyIcon.setLayoutParams(layoutParamsForMoneyIcon);
    }

    private void prepareTotalAmountForMonthView() {
        totalAmountForMonth = new TextView(context);
        totalAmountForMonth.setId(numberOfBarChartDrawn * 10 + idOfFirstElementOfBarChart + 4);
        totalAmountForMonth.setTextSize(20);
        totalAmountForMonth.setTypeface(null, Typeface.BOLD);
        if (transactionsGroupedByCategory.get(0).getType() == 1) {
            sumOfTransactionsOfCurrentCategoryInMonth = -sumOfTransactionsOfCurrentCategoryInMonth; //if it's expense
        }
        @SuppressLint("DefaultLocale") String totalAmountForMonthText = String.format("%.2f", getValueInCurrency(sumOfTransactionsOfCurrentCategoryInMonth));
        totalAmountForMonth.setText(totalAmountForMonthText);
        totalAmountForMonth.setTextColor(ContextCompat.getColor(context, R.color.sum_lesser_than_zero));
        RelativeLayout.LayoutParams layoutParamsForTotalAmountForMonth = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsForTotalAmountForMonth.addRule(RelativeLayout.BELOW, separatorLine.getId());
        layoutParamsForTotalAmountForMonth.addRule(RelativeLayout.LEFT_OF, moneyIcon.getId());
        layoutParamsForTotalAmountForMonth.setMargins(0, dpToPx(5), 0, 0);
        totalAmountForMonth.setLayoutParams(layoutParamsForTotalAmountForMonth);
    }

    private void prepareAverageForMonthView() {
        averageForMonth = new TextView(context);
        averageForMonth.setId(numberOfBarChartDrawn * 10 + idOfFirstElementOfBarChart + 5);
        averageForMonth.setTextSize(15);
        @SuppressLint("DefaultLocale") String averageForMonthText = String.format("Average: %.2f / day", getValueInCurrency(sumOfTransactionsOfCurrentCategoryInMonth / numberOfDaysInMonth));
        averageForMonth.setText(averageForMonthText);
        RelativeLayout.LayoutParams layoutParamsForAverageForMonth = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsForAverageForMonth.addRule(RelativeLayout.BELOW, categoryImage.getId());
        layoutParamsForAverageForMonth.setMargins(dpToPx(10), dpToPx(3), 0, 0);
        averageForMonth.setLayoutParams(layoutParamsForAverageForMonth);
    }

    private void prepareBarChartView() {
        RelativeLayout.LayoutParams layoutParamsForBarChart = new RelativeLayout.LayoutParams(dpToPx(365), dpToPx(140));
        barChart.setId(numberOfBarChartDrawn * 10 + idOfFirstElementOfBarChart + 6);
        layoutParamsForBarChart.addRule(RelativeLayout.BELOW, averageForMonth.getId());
        layoutParamsForBarChart.addRule(RelativeLayout.CENTER_HORIZONTAL);
        barChart.setLayoutParams(layoutParamsForBarChart);
    }

    public View getSeparatorLine() {
        return separatorLine;
    }

    public ImageView getCategoryImage() {
        return categoryImage;
    }

    public TextView getCategoryName() {
        return categoryName;
    }

    public ImageView getMoneyIcon() {
        return moneyIcon;
    }

    public TextView getTotalAmountForMonth() {
        return totalAmountForMonth;
    }

    public TextView getAverageForMonth() {
        return averageForMonth;
    }

    public BarChart getBarChart() {
        return barChart;
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

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }
}
