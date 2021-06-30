package com.example.myexpenses.fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myexpenses.R;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.other.CurrentMonthData;
import com.example.myexpenses.repository.TransactionRepository;
import com.example.myexpenses.viewDrawer.AverageTransactionPerDayDrawer;
import com.example.myexpenses.viewDrawer.BarChartDrawer;
import com.example.myexpenses.viewDrawer.InnerPieChartDrawer;
import com.example.myexpenses.viewDrawer.OuterPieChartDrawer;
import com.github.mikephil.charting.charts.PieChart;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class ChartsFragment extends Fragment {

    private TransactionRepository transactionRepository;
    private RelativeLayout chartContainer;
    private int currentChosenMonth;
    private int currentChosenYear;
    private TextView noDataText;

    private SpannableStringBuilder formattedTotalSum;

    private int actualMonth;
    private int actualYear;

    private int monthTransactionsSize;

    private final int idOfOuterPieChart = 1;
    private final int idOfAverageTransactionPerDay = 2;
    private final int idOfFirstElementOfBarChart = 3;

    private boolean sharedPrefShouldShowIncomesBarCharts;
    private boolean sharedPrefShouldShowPieChartAnimation;
    private boolean sharedPrefShouldPresentTotalValues;

    private PieChart outerPieChart;
    private PieChart innerPieChart;

    private ScrollView chartScrollContainer;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionRepository = new TransactionRepository(getContext());

        int monthOffset = getArguments().getInt("monthOffset");
        actualMonth = getArguments().getInt("actualMonth");
        actualYear = getArguments().getInt("actualYear");
        sharedPrefShouldShowIncomesBarCharts = getArguments().getBoolean("sharedPrefShouldShowIncomesBarCharts");
        sharedPrefShouldShowPieChartAnimation = getArguments().getBoolean("sharedPrefShouldShowPieChartAnimation");
        sharedPrefShouldPresentTotalValues = getArguments().getBoolean("sharedPrefShouldPresentTotalValues");

        calculateCurrentMonthAndYear(monthOffset);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //onCreateView
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        chartContainer = view.findViewById(R.id.chartContainer);
        chartScrollContainer = view.findViewById(R.id.chartsScrollContainer);

        noDataText = view.findViewById(R.id.noDataText);

        updateChartData();

        return view;
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateChartData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentChosenMonth);
        calendar.set(Calendar.YEAR, currentChosenYear);
        int numberOfDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<Transaction> monthTransactions = transactionRepository.findTransactionsInMonth(currentChosenMonth, currentChosenYear);
        monthTransactionsSize = monthTransactions.size();
        int totalSum = monthTransactions.stream()
                .mapToInt(Transaction::getAmount)
                .sum();

        formattedTotalSum = new SpannableStringBuilder();
        Spannable sumOfMonthlyTransactions;
        if (totalSum >= 0) {
            sumOfMonthlyTransactions = new SpannableString(String.format("+%.2f", getValueInCurrency(totalSum)));
            sumOfMonthlyTransactions.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.sum_greater_than_zero)), 0, sumOfMonthlyTransactions.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            sumOfMonthlyTransactions = new SpannableString(String.format("%.2f", getValueInCurrency(totalSum)));
            sumOfMonthlyTransactions.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.sum_lesser_than_zero)), 0, sumOfMonthlyTransactions.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        formattedTotalSum.append(sumOfMonthlyTransactions);

        chartContainer.removeAllViews();

        if (monthTransactionsSize > 0) {
            drawMonthlyTransactionOuterPieChart(monthTransactions);
            drawMonthlyTransactionInnerPieChart(monthTransactions);
            drawAverageTransactionPerDay(totalSum, numberOfDaysInMonth);
            drawMonthlyTransactionBarCharts(monthTransactions, numberOfDaysInMonth);

            noDataText.setVisibility(View.GONE);
//            chartScrollContainer.fullScroll(ScrollView.FOCUS_UP);
//            chartScrollContainer.smoothScrollTo(0, 0);
        } else {
            noDataText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //when fragment is not visible scroll view to the top
        chartScrollContainer.fullScroll(ScrollView.FOCUS_UP);
        chartScrollContainer.smoothScrollTo(0, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawMonthlyTransactionOuterPieChart(List<Transaction> monthTransactions) {
        OuterPieChartDrawer outerPieChartDrawer = new OuterPieChartDrawer(getContext(), monthTransactions, idOfOuterPieChart, sharedPrefShouldPresentTotalValues);
        outerPieChart = outerPieChartDrawer.getOuterPieChart();
        chartContainer.addView(outerPieChart);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawMonthlyTransactionInnerPieChart(List<Transaction> monthTransactions) {
        InnerPieChartDrawer innerPieChartDrawer = new InnerPieChartDrawer(getContext(), monthTransactions, sharedPrefShouldPresentTotalValues);
        innerPieChart = innerPieChartDrawer.getInnerPieChart();
        chartContainer.addView(innerPieChartDrawer.getInnerPieChart());
    }

    private void drawAverageTransactionPerDay(int totalSum, int numberOfDaysInMonth) {
        AverageTransactionPerDayDrawer averageTransactionPerDayDrawer = new AverageTransactionPerDayDrawer(getContext(), idOfAverageTransactionPerDay, totalSum, numberOfDaysInMonth);
        chartContainer.addView(averageTransactionPerDayDrawer.getMonthlyTransactionAverage());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawMonthlyTransactionBarCharts(List<Transaction> monthTransactions, int numberOfDaysInMonth) {
        //return lists of transactions from whole month grouped by category (f.e. sport, health)
        List<List<Transaction>> listsOfMonthTransactionsGroupedByCategory = new LinkedList<>(
                monthTransactions.stream()
//                        .filter(shouldShowIncomesBarCharts? p -> p.getType() == 1 : p -> true)
                        .filter(p -> sharedPrefShouldShowIncomesBarCharts || p.getType() == 1)
                        .collect(Collectors.groupingBy(Transaction::getCategory))
                        .values());

        int numberOfBarChartsToDraw = listsOfMonthTransactionsGroupedByCategory.size();

        for (int i = 0; i < numberOfBarChartsToDraw; i++) {
            BarChartDrawer barChartDrawer = new BarChartDrawer(getContext(), listsOfMonthTransactionsGroupedByCategory.get(i), i, numberOfDaysInMonth, idOfFirstElementOfBarChart);
            chartContainer.addView(barChartDrawer.getSeparatorLine());
            chartContainer.addView(barChartDrawer.getCategoryImage());
            chartContainer.addView(barChartDrawer.getCategoryName());
            chartContainer.addView(barChartDrawer.getMoneyIcon());
            chartContainer.addView(barChartDrawer.getTotalAmountForMonth());
            chartContainer.addView(barChartDrawer.getAverageForMonth());
            chartContainer.addView(barChartDrawer.getBarChart());
        }
    }

    private void calculateCurrentMonthAndYear(int monthOffset) {
        if (actualMonth + monthOffset < 0) {
            currentChosenYear = actualYear + ((actualMonth + monthOffset + 1) / 12 - 1);
        } else {
            currentChosenYear = actualYear + (actualMonth + monthOffset) / 12;
        }

        currentChosenMonth = (actualMonth + monthOffset) % 12;
        if (currentChosenMonth < 0) {
            currentChosenMonth += 12;
        }
    }

    public CurrentMonthData getDataFromChartsFragment() {
        return new CurrentMonthData(currentChosenMonth, currentChosenYear, formattedTotalSum);
    }

    public void animateChart() {
        if (monthTransactionsSize > 0 && sharedPrefShouldShowPieChartAnimation) {
            outerPieChart.animateXY(750, 750);
            innerPieChart.animateXY(750, 750);
        }
    }
}

