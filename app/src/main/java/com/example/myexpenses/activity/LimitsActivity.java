package com.example.myexpenses.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.myexpenses.R;
import com.example.myexpenses.dialogFragment.EditLimitsDialog;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;
import com.example.myexpenses.viewDrawer.BarChartLimitDrawer;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class LimitsActivity extends AppCompatActivity implements View.OnClickListener, EditLimitsDialog.EditLimitsDialogCommunicator {

    private RelativeLayout relativeLayout;
    private TextView currentChosenMonthAndYear;
    private TextView dailyLimitSetAmount, dailyLimitLeftAmount;
    private TextView monthlyLimitSetAmount, monthlyLimitLeftAmount;
    private TextView expenseTotalSum;
    private TextView expenseMonthlyAverage;
    private BarChart monthlyBarChart;

    private TransactionRepository transactionRepository;
    private SharedPreferences sharedPreferences;
    private Calendar calendar;
    private int currentChosenMonth;
    private int currentChosenYear;
    private int dailyLimit;
    private int monthlyLimit;
    private int sumOfDailyExpenses;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_limits);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.set_limits));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        transactionRepository = new TransactionRepository(this);
        sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);

        calendar = Calendar.getInstance();
        int actualDay = calendar.get(Calendar.DAY_OF_MONTH);
        currentChosenMonth = calendar.get(Calendar.MONTH);
        currentChosenYear = calendar.get(Calendar.YEAR);

        //read it only once
        sumOfDailyExpenses = Math.abs(transactionRepository.getSumOfDailyExpenses(actualDay, currentChosenMonth, currentChosenYear));

        //default values if they not have been initialized yet
        dailyLimit = sharedPreferences.getInt("dailyLimit", 1000 * 100);
        monthlyLimit = sharedPreferences.getInt("monthlyLimit", 5000 * 100);

        relativeLayout = findViewById(R.id.relativeLayoutLimitsActivity);

        ImageButton previousMonth = findViewById(R.id.previousMonth);
        previousMonth.setOnClickListener(this);
        currentChosenMonthAndYear = findViewById(R.id.currentChosenMonthAndYear);
        ImageButton nextMonth = findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);

        dailyLimitSetAmount = findViewById(R.id.dailyLimitSetAmount);
        dailyLimitLeftAmount = findViewById(R.id.dailyLimitLeftAmount);
        monthlyLimitSetAmount = findViewById(R.id.monthlyLimitSetAmount);
        monthlyLimitLeftAmount = findViewById(R.id.monthlyLimitLeftAmount);
        Button setDailyAndMonthlyLimit = findViewById(R.id.setDailyAndMonthlyLimit);
        setDailyAndMonthlyLimit.setOnClickListener(this);

        expenseMonthlyAverage = findViewById(R.id.expenseMonthlyAverage);
        expenseTotalSum = findViewById(R.id.expenseTotalSum);
        monthlyBarChart = findViewById(R.id.monthlyBarchart);

        updateView();
    }

    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
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
                updateView();
                break;
            }

            case R.id.nextMonth: {
                if (currentChosenMonth == 11) { //months are indexed starting from zero
                    currentChosenMonth = 0;
                    currentChosenYear++;
                } else {
                    currentChosenMonth++;
                }
                updateView();
                break;
            }
            case R.id.setDailyAndMonthlyLimit: {

                FragmentManager fragmentManager = getSupportFragmentManager();
                EditLimitsDialog editLimitsDialog = new EditLimitsDialog();
                //passing data to dialog
                Bundle data = new Bundle();
                data.putInt("dailyLimit", dailyLimit);
                data.putInt("monthlyLimit", monthlyLimit);
                editLimitsDialog.setArguments(data);

                editLimitsDialog.show(fragmentManager, "edit limits fragment");
                break;
            }

            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void retrieveDataFromEditLimitsDialog(Bundle data) {
        int dailyLimitFromDialog = data.getInt("dailyLimit");
        int monthlyLimitFromDialog = data.getInt("monthlyLimit");

        if (dailyLimitFromDialog != dailyLimit || monthlyLimitFromDialog != monthlyLimit) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (dailyLimitFromDialog != dailyLimit) {
                dailyLimit = dailyLimitFromDialog;
                editor.putInt("dailyLimit", dailyLimit);
            }
            if (monthlyLimitFromDialog != monthlyLimit) {
                monthlyLimit = monthlyLimitFromDialog;
                editor.putInt("monthlyLimit", monthlyLimit);
            }
            editor.apply();
            updateView();
            Snackbar snackbar = Snackbar.make(relativeLayout, "New limits set", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        else {
            Snackbar snackbar = Snackbar.make(relativeLayout, "Limits hasn't changed", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateView() {
        currentChosenMonthAndYear.setText(convertMonthToString(currentChosenMonth + 1, currentChosenYear)); //months are indexed starting from zero

        calendar.set(Calendar.MONTH, currentChosenMonth);
        calendar.set(Calendar.YEAR, currentChosenYear);
        int numberOfDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<Transaction> monthlyExpenses = transactionRepository.findExpensesInMonth(currentChosenMonth, currentChosenYear);
        Collections.sort(monthlyExpenses); //dates must be in order

        int sumOfMonthlyExpenses = monthlyExpenses.stream()
                .mapToInt(Transaction::getAmount)
                .map(Math::abs)
                .sum();

        dailyLimitSetAmount.setText(String.format("%.2f", getValueInCurrency(dailyLimit)));
        monthlyLimitSetAmount.setText(String.format("%.2f", getValueInCurrency(monthlyLimit)));

        int limitExceededAmount = dailyLimit - sumOfDailyExpenses;
        if (limitExceededAmount < 0) {
            dailyLimitLeftAmount.setText(String.format("%.2f", getValueInCurrency(limitExceededAmount)));
            dailyLimitLeftAmount.setTextColor(getLimitReachedColor());
        } else {
            dailyLimitLeftAmount.setText(String.format("+%.2f", getValueInCurrency(limitExceededAmount)));
            dailyLimitLeftAmount.setTextColor(getLimitNotReachedColor());
        }

        if (monthlyLimit - sumOfMonthlyExpenses < 0) {
            monthlyLimitLeftAmount.setText(String.format("%.2f", getValueInCurrency(monthlyLimit - sumOfMonthlyExpenses)));
            monthlyLimitLeftAmount.setTextColor(getLimitReachedColor());
        } else {
            monthlyLimitLeftAmount.setText(String.format("+%.2f", getValueInCurrency(monthlyLimit - sumOfMonthlyExpenses)));
            monthlyLimitLeftAmount.setTextColor(getLimitNotReachedColor());
        }

        expenseTotalSum.setText(String.format("-%.2f", getValueInCurrency(sumOfMonthlyExpenses)));
        expenseMonthlyAverage.setText(String.format("Average: %.2f / day", getValueInCurrency(sumOfMonthlyExpenses / numberOfDaysInMonth)));

        BarChartLimitDrawer barChartLimitDrawer = new BarChartLimitDrawer(monthlyBarChart, getApplicationContext(), monthlyExpenses, numberOfDaysInMonth, monthlyLimit);
    }

    private int getLimitNotReachedColor() {
        return ContextCompat.getColor(this, R.color.sum_greater_than_zero);
    }

    private int getLimitReachedColor() {
        return ContextCompat.getColor(this, R.color.limit_reached);
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

    //prevent left checked icon on navigation drawer and also clear filters f.e.
    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}