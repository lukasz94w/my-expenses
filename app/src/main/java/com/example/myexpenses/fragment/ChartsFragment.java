package com.example.myexpenses.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myexpenses.R;
import com.example.myexpenses.viewDrawer.AverageTransactionPerDayDrawer;
import com.example.myexpenses.viewDrawer.BarChartDrawer;
import com.example.myexpenses.viewDrawer.InnerPieChartDrawer;
import com.example.myexpenses.viewDrawer.OuterPieChartDrawer;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class ChartsFragment extends Fragment implements View.OnClickListener {

    private TransactionRepository transactionRepository;
    private TextView actualChosenMonth;
    private TextView monthlyTransactionSum;
    private RelativeLayout chartContainer;
    private int currentChosenMonth;
    private int currentChosenYear;
    private Calendar calendar;
    private ScrollView chartScrollContainer;
    private TextView noDataText;

    private final int idOfOuterPieChart = 1;
    private final int idOfAverageTransactionPerDay = 2;
    private final int idOfFirstElementOfBarChart = 3;

    private boolean sharedPrefShouldShowIncomesBarCharts;
    private boolean sharedPrefShouldShowPieChartAnimation;
    private boolean sharedPrefShouldPresentTotalValues;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionRepository = new TransactionRepository(getContext());
        calendar = Calendar.getInstance();
        currentChosenMonth = calendar.get(Calendar.MONTH);
        currentChosenYear = calendar.get(Calendar.YEAR);

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        //default value if it not have been initialized yet
        sharedPrefShouldShowIncomesBarCharts = sharedPreferences.getBoolean("Should show incomes bar charts", true);
        sharedPrefShouldShowPieChartAnimation = sharedPreferences.getBoolean("Should show pie chart animation", true);
        sharedPrefShouldPresentTotalValues = sharedPreferences.getBoolean("Should present total values on pie chart", false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //onCreateView
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        ImageButton previousMonth = view.findViewById(R.id.previousMonth);
        previousMonth.setOnClickListener(this);

        actualChosenMonth = view.findViewById(R.id.currentChosenMonthAndYear);

        ImageButton nextMonth = view.findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);

        monthlyTransactionSum = view.findViewById(R.id.monthlyTransactionSum);

        chartContainer = view.findViewById(R.id.chartContainer);
        chartScrollContainer = view.findViewById(R.id.chartsScrollContainer);

        noDataText = view.findViewById(R.id.noDataText);

        updateView("NO_ANIMATION");

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previousMonth: {
                if (currentChosenMonth == 0) { //months are indexed starting from zero
                    currentChosenMonth = 11;
                    currentChosenYear--;
                } else {
                    currentChosenMonth--;
                }
                updateView("LEFT_TO_RIGHT_ANIMATION");
                break;
            }

            case R.id.nextMonth: {
                if (currentChosenMonth == 11) { //months are indexed starting from zero
                    currentChosenMonth = 0;
                    currentChosenYear++;
                } else {
                    currentChosenMonth++;
                }
                updateView("RIGHT_TO_LEFT_ANIMATION");
                break;
            }

            default:
                break;
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateView(String typeOfAnimation) {
        calendar.set(Calendar.MONTH, currentChosenMonth);
        calendar.set(Calendar.YEAR, currentChosenYear);
        int numberOfDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<Transaction> monthTransactions = transactionRepository.findTransactionsInMonth(currentChosenMonth, currentChosenYear);
        int totalSum = monthTransactions.stream()
                .mapToInt(Transaction::getAmount)
                .sum();

        actualChosenMonth.setText(convertMonthToString(currentChosenMonth + 1, currentChosenYear)); //months are indexed starting from zero
        if (totalSum >= 0) {
            monthlyTransactionSum.setText(String.format("+%.2f", getValueInCurrency(totalSum)));
            monthlyTransactionSum.setTextColor(ContextCompat.getColor(requireContext(), R.color.sum_greater_than_zero));
        } else {
            monthlyTransactionSum.setText(String.format("%.2f", getValueInCurrency(totalSum)));
            monthlyTransactionSum.setTextColor(ContextCompat.getColor(requireContext(), R.color.sum_lesser_than_zero));
        }

        chartContainer.removeAllViews();

        if (monthTransactions.size() > 0) {
            drawMonthlyTransactionOuterPieChart(monthTransactions);
            drawMonthlyTransactionInnerPieChart(monthTransactions);
            drawAverageTransactionPerDay(totalSum, numberOfDaysInMonth);
            drawMonthlyTransactionBarCharts(monthTransactions, numberOfDaysInMonth);

            noDataText.setVisibility(View.GONE);

            chartScrollContainer.fullScroll(ScrollView.FOCUS_UP);
            chartScrollContainer.smoothScrollTo(0, 0);
        } else {
            noDataText.setVisibility(View.VISIBLE);
        }


        switch (typeOfAnimation) {
            case "NO_ANIMATION": {
                break;
            }
//            case "RIGHT_TO_LEFT_ANIMATION": {
//                chartContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.right_to_left));
//                break;
//            }
//            case "LEFT_TO_RIGHT_ANIMATION": {
//                chartContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.left_to_right));
//                break;
//            }
            default:
                break;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawMonthlyTransactionOuterPieChart(List<Transaction> monthTransactions) {
        OuterPieChartDrawer outerPieChartDrawer = new OuterPieChartDrawer(getContext(), monthTransactions, idOfOuterPieChart, sharedPrefShouldShowPieChartAnimation, sharedPrefShouldPresentTotalValues);
        chartContainer.addView(outerPieChartDrawer.getOuterPieChart());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawMonthlyTransactionInnerPieChart(List<Transaction> monthTransactions) {
        InnerPieChartDrawer innerPieChartDrawer = new InnerPieChartDrawer(getContext(), monthTransactions, sharedPrefShouldShowPieChartAnimation, sharedPrefShouldPresentTotalValues);
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

    public String convertMonthToString(int month, int year) {
        //months are indexed starting at 0
        String MM = "" + month;
        String yyyy = "" + year;

        if (month < 10) {
            MM = "0" + month;
        }

        return MM + "/" + yyyy;
    }
}

