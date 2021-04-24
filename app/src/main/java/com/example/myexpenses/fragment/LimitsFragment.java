package com.example.myexpenses.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myexpenses.R;
import com.example.myexpenses.customValueFormatter.IntValueFormatter;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class LimitsFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private SharedPreferences sharedPreferences;
    private TextView dailyLimitSetAmount, monthlyLimitSetAmount;
    private TextView dailyLimitLeftAmount, monthlyLimitLeftAmount;
    private PopupWindow popupWindow;
    private EditText setDailyLimit, setMonthlyLimit;

    private Float dailyLimit;
    private Float monthlyLimit;

    private int numberOfDaysInMonth;
    private List<Transaction> monthlyExpenses;
    private double sumOfDailyExpenses;
    private double sumOfMonthlyExpenses;
    private BarChart monthlyBarChart;

    private static class BarEntryHolder {
        float xVal;
        float yVal;

        public BarEntryHolder(float xVal, float yVal) {
            this.xVal = xVal;
            this.yVal = yVal;
        }

        public float getxVal() {
            return xVal;
        }

        public float getyVal() {
            return yVal;
        }
    }

    private class MyBarDataSet extends BarDataSet {
        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            if (getEntryForIndex(index).getY() < monthlyLimit)
                return mColors.get(0);
            else
                return mColors.get(1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //onCreate
        TransactionRepository transactionRepository = new TransactionRepository(getContext());
        sharedPreferences = this.getActivity().getSharedPreferences("Limits", MODE_PRIVATE);

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentMonthYear = calendar.get(Calendar.YEAR);
        numberOfDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        Date currentDayAsDate = new Date();
        currentDayAsDate.setTime(calendar.getTime().getTime());

        monthlyExpenses = transactionRepository.findExpensesInMonth(currentMonth, currentMonthYear);
        Collections.sort(monthlyExpenses); //dates must be in order

        sumOfDailyExpenses = monthlyExpenses.stream()
                .filter(o -> o.getDate().equals(currentDayAsDate))
                .mapToDouble(o -> Math.abs(o.getAmount()))
                .sum();

        sumOfMonthlyExpenses = monthlyExpenses.stream()
                .mapToDouble(o -> Math.abs(o.getAmount()))
                .sum();

        //default values if they not have been initialized yet
        dailyLimit = sharedPreferences.getFloat("Daily limit", 1000);
        monthlyLimit = sharedPreferences.getFloat("Monthly limit", 5000);
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_limits, container, false);

        dailyLimitSetAmount = view.findViewById(R.id.dailyLimitSetAmount);
        dailyLimitLeftAmount = view.findViewById(R.id.dailyLimitLeftAmount);

        monthlyLimitSetAmount = view.findViewById(R.id.monthlyLimitSetAmount);
        monthlyLimitLeftAmount = view.findViewById(R.id.monthlyLimitLeftAmount);

        //onCreateView
        Button setDailyLimitButton = view.findViewById(R.id.setDailyLimitButton);
        setDailyLimitButton.setOnClickListener(this);

        TextView expenseTotalSum = view.findViewById(R.id.expenseTotalSum);
        expenseTotalSum.setText(String.format("-%.2f", sumOfMonthlyExpenses));

        TextView expenseMonthlyAverage = view.findViewById(R.id.expenseMonthlyAverage);
        expenseMonthlyAverage.setText(String.format("Average: %.2f / day", sumOfMonthlyExpenses / numberOfDaysInMonth));

        monthlyBarChart = view.findViewById(R.id.monthlyBarchart);

        updateView();

        return view;
    }

    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setDailyLimitButton: {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                //popupWindow
                View popupView = inflater.inflate(R.layout.popup_change_limits, null);

                popupWindow = new PopupWindow(popupView, 565, 548, true);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, -125);

                Toolbar toolbar = popupView.findViewById(R.id.tool_bar);
                ImageButton toolbarClose = popupView.findViewById(R.id.toolbarClose);
                toolbarClose.setOnClickListener(this);

                TextView toolbarText = popupView.findViewById(R.id.toolbarText);
                toolbarText.setText("Set limits");

                setDailyLimit = popupView.findViewById(R.id.setDailyLimit);
                setDailyLimit.setText(String.format("%.2f", dailyLimit));
                setDailyLimit.setOnTouchListener(this);

                setMonthlyLimit = popupView.findViewById(R.id.setMonthlyLimit);
                setMonthlyLimit.setText(String.format("%.2f", monthlyLimit));
                setMonthlyLimit.setOnTouchListener(this);

                Button saveNewLimitsButton = popupView.findViewById(R.id.saveNewLimitsButton);
                saveNewLimitsButton.setOnClickListener(this);

                break;
            }

            case R.id.saveNewLimitsButton: {

                SharedPreferences.Editor editor = sharedPreferences.edit();

                //check if amount is empty if so we set it as zero
                try {
                    dailyLimit = Float.parseFloat(String.valueOf(setDailyLimit.getText()));
                } catch (NumberFormatException e) {
                    dailyLimit = 0f;
                }

                //check if amount is empty if so we set it as zero
                try {
                    monthlyLimit = Float.parseFloat(String.valueOf(setMonthlyLimit.getText()));
                } catch (NumberFormatException e) {
                    monthlyLimit = 0f;
                }

                editor.putFloat("Daily limit", dailyLimit);
                editor.putFloat("Monthly limit", monthlyLimit);
                editor.apply();

                updateView();

                Snackbar snackbar = Snackbar.make(getView(), "New limits set", Snackbar.LENGTH_LONG);
                snackbar.show();

                popupWindow.dismiss();
                break;
            }

            case R.id.toolbarClose: {
                popupWindow.dismiss();
                break;
            }

            default:
                break;
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateView() {

        dailyLimitSetAmount.setText(String.format("%.2f", dailyLimit));
        monthlyLimitSetAmount.setText(String.format("%.2f", monthlyLimit));

        double dailyLimitLeft = dailyLimit - sumOfDailyExpenses;
        double monthlyLimitLeft = monthlyLimit - sumOfMonthlyExpenses;

        if (dailyLimitLeft < 0) {
            dailyLimitLeftAmount.setText(String.format("%.2f", dailyLimitLeft));
            dailyLimitLeftAmount.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.limit_reached));
        } else {
            dailyLimitLeftAmount.setText(String.format("+%.2f", dailyLimitLeft));
            dailyLimitLeftAmount.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.sum_greater_than_zero));
        }

        if (monthlyLimitLeft < 0) {
            monthlyLimitLeftAmount.setText(String.format("%.2f", monthlyLimitLeft));
            monthlyLimitLeftAmount.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.limit_reached));
        } else {
            monthlyLimitLeftAmount.setText(String.format("+%.2f", monthlyLimitLeft));
            monthlyLimitLeftAmount.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.sum_greater_than_zero));
        }

        drawMonthlyExpensesBarChart();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawMonthlyExpensesBarChart() {
        //configure barChart appearance
        monthlyBarChart.getLegend().setEnabled(false);
        monthlyBarChart.getDescription().setEnabled(false);
        monthlyBarChart.getAxisRight().setDrawLabels(false);
        monthlyBarChart.getAxisRight().setDrawAxisLine(false);
        monthlyBarChart.getAxisRight().setDrawGridLines(false);
        LimitLine limitLine = new LimitLine(monthlyLimit, "");
        limitLine.setLineColor(Color.DKGRAY);
        limitLine.setLineWidth(4f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        limitLine.setTextSize(10f);
        monthlyBarChart.getAxisLeft().removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        monthlyBarChart.getAxisLeft().addLimitLine(limitLine);
        monthlyBarChart.getAxisLeft().setDrawAxisLine(false);
        monthlyBarChart.getAxisLeft().setGridColor(getResources().getColor(R.color.grid_color)); //color of left Y axis
        monthlyBarChart.getAxisLeft().setTextSize(12);
        monthlyBarChart.getAxisLeft().setXOffset(10);
        monthlyBarChart.getAxisLeft().setValueFormatter(new IntValueFormatter());
        XAxis xAxis = monthlyBarChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(getResources().getColor(R.color.grid_color));
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

        ArrayList<BarEntryHolder> holderOfBarEntries = new ArrayList<>();
        int numberOfDayInMonth = 0;
        float sumOfExpensesInMonth = 0;
        Calendar calendarToGetDayOfMonth = Calendar.getInstance();

        for (int i = 0; i < monthlyExpenses.size(); i++) {
            Date dateOfTransaction = monthlyExpenses.get(i).getDate();
            calendarToGetDayOfMonth.setTime(dateOfTransaction);
            int dayOfMonth = calendarToGetDayOfMonth.get(Calendar.DAY_OF_MONTH);
            int dayOfTransaction = dayOfMonth - 1; //because at getFormattedValue for xAxis +1 is added

            while (numberOfDayInMonth <= dayOfTransaction) {
                holderOfBarEntries.add(new BarEntryHolder(numberOfDayInMonth, sumOfExpensesInMonth));
                numberOfDayInMonth++;
            }

            numberOfDayInMonth = dayOfTransaction;
            float valueOfExpense = (float) Math.abs(monthlyExpenses.get(i).getAmount()); //abs because expense numbers are stored with '-' sign
            sumOfExpensesInMonth = sumOfExpensesInMonth + valueOfExpense;

            //checking if there is already a transaction with the same date, if so, actualize total sum
            int index = IntStream.range(0, holderOfBarEntries.size())
                    .filter(k -> holderOfBarEntries.get(k).getxVal() == dayOfTransaction)
                    .findFirst()
                    .orElse(-1);
            if (index != -1) {
                holderOfBarEntries.set(index, new BarEntryHolder(dayOfTransaction, sumOfExpensesInMonth));
            } else {
                holderOfBarEntries.add(new BarEntryHolder(dayOfTransaction, sumOfExpensesInMonth));
            }
        }
        //fil rest of months day
        for (int j = numberOfDayInMonth; j < numberOfDaysInMonth; j++) {
            holderOfBarEntries.add(new BarEntryHolder(j, sumOfExpensesInMonth));
        }

        monthlyBarChart.getAxisLeft().setAxisMinimum(-(sumOfExpensesInMonth * 0.075f)); //make additional space between bottom of the chart and labels
        if (sumOfExpensesInMonth < monthlyLimit) {
            monthlyBarChart.getAxisLeft().setAxisMaximum(monthlyLimit * 1.075f);
        } else {
            monthlyBarChart.getAxisLeft().setAxisMaximum(sumOfExpensesInMonth * 1.075f);
        }

        //copy data from barEntryHolder to barEntriesList
        ArrayList barEntriesOfCurrentCategoryInMonth = new ArrayList<>();
        for (int m = 0; m < holderOfBarEntries.size(); m++) {
            barEntriesOfCurrentCategoryInMonth.add(new BarEntry(holderOfBarEntries.get(m).getxVal(), holderOfBarEntries.get(m).getyVal()));
        }

        MyBarDataSet myBarDataSet = new MyBarDataSet(barEntriesOfCurrentCategoryInMonth, "");
        myBarDataSet.setColors(
                ContextCompat.getColor(getContext(), R.color.below_limit),
                ContextCompat.getColor(getContext(), R.color.over_limit));
        myBarDataSet.setDrawValues(false);

        BarData barData = new BarData(myBarDataSet);

        monthlyBarChart.setData(barData);
        monthlyBarChart.invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.setDailyLimit: {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (setDailyLimit.getRight() - setDailyLimit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        setDailyLimit.setText("");
                        v.performClick();
                        return false;
                    }
                }
                break;
            }
            case R.id.setMonthlyLimit: {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (setMonthlyLimit.getRight() - setMonthlyLimit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        setMonthlyLimit.setText("");
                        v.performClick();
                        return false;
                    }
                }
            }
            break;
        }
        return false;
    }
}