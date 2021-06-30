package com.example.myexpenses.viewDrawer;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;

import com.example.myexpenses.model.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public class InnerPieChartDrawer {
    private final Context context;
    private final List<Transaction> monthTransactions;
    private final boolean sharedPrefShouldPresentTotalValues;

    private PieChart pieChart;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public InnerPieChartDrawer(Context context, List<Transaction> transactionList, boolean sharedPrefShouldPresentTotalValues) {
        this.context = context;
        this.monthTransactions = transactionList;
        this.sharedPrefShouldPresentTotalValues = sharedPrefShouldPresentTotalValues;

        configurePieChartAppearance();
        preparePieChartDataAndView();
    }

    private void configurePieChartAppearance() {
        pieChart = new PieChart(context);
        Legend legendInner = pieChart.getLegend();
        legendInner.setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        if (!sharedPrefShouldPresentTotalValues) {
            pieChart.setUsePercentValues(true);
        }
        pieChart.setDrawHoleEnabled(true);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.setHoleRadius(58f);
        pieChart.setNoDataText("");

        RelativeLayout.LayoutParams layoutParamsForBarInnerPieChart = new RelativeLayout.LayoutParams(dpToPx(188), dpToPx(188));
        layoutParamsForBarInnerPieChart.setMargins(0, dpToPx(71), 0, 0);
        layoutParamsForBarInnerPieChart.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pieChart.setLayoutParams(layoutParamsForBarInnerPieChart);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void preparePieChartDataAndView() {
        //prepare map which contains sum of transactions with the same category
        //by using LinkedHashMap I keep order needed when outer and inner charts are drawn
        Map<Integer, Integer> map = monthTransactions.stream().
                collect(groupingBy(Transaction::getType, LinkedHashMap::new, summingInt(Transaction::getAmount)));

        List<Integer> transactionColor = new LinkedList<>();
        List<PieEntry> transactionValue = new LinkedList<>();

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            //prepare values for PieChart
            transactionValue.add(new PieEntry(Math.abs(getValueInCurrency(entry.getValue())), entry.getKey()));
            //prepare colors for values
            String colorName;
            if (entry.getKey().equals(1)) {
                colorName = ("expenses_color").toLowerCase().replace(" ", "_");
            } else {
                colorName = ("incomes_color").toLowerCase().replace(" ", "_");
            }
            int colorId = context.getResources().getColor(context.getResources().getIdentifier(colorName, "color", context.getPackageName()));
            transactionColor.add(colorId);
        }

        PieDataSet pieDataSet = new PieDataSet(transactionValue, "Transactions");
        pieDataSet.setSliceSpace(0.5f);
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(13f);
        pieData.setValueTextColor(Color.DKGRAY);
        pieDataSet.setColors(transactionColor);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    public PieChart getInnerPieChart() {
        return pieChart;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }
}
