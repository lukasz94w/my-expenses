package com.example.myexpenses.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myexpenses.R;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.other.LimitsFragmentData;
import com.example.myexpenses.repository.TransactionRepository;
import com.example.myexpenses.viewDrawer.BarChartLimitDrawer;
import com.github.mikephil.charting.charts.BarChart;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class LimitsFragment extends Fragment {

    private TransactionRepository transactionRepository;
    private TextView expenseTotalSum;
    private TextView expenseMonthlyAverage;
    private BarChart monthlyBarChart;

    private int sumOfMonthlyExpenses;

    private int actualMonth;
    private int actualYear;
    private int currentChosenMonth;
    private int currentChosenYear;

    private int monthlyLimit;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transactionRepository = new TransactionRepository(getContext());
        int monthOffset = getArguments().getInt("monthOffset");
        actualMonth = getArguments().getInt("actualMonth");
        actualYear = getArguments().getInt("actualYear");
        monthlyLimit = getArguments().getInt("monthlyLimit");

        calculateCurrentMonthAndYear(monthOffset);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_limits, container, false);

        expenseMonthlyAverage = view.findViewById(R.id.expenseMonthlyAverage);
        expenseTotalSum = view.findViewById(R.id.expenseTotalSum);
        monthlyBarChart = view.findViewById(R.id.monthlyBarchart);

        updateData();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentChosenMonth);
        calendar.set(Calendar.YEAR, currentChosenYear);
        int numberOfDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<Transaction> monthlyExpenses = transactionRepository.findExpensesInMonth(currentChosenMonth, currentChosenYear);
        Collections.sort(monthlyExpenses); //dates must be in order

        //to bede zwracal
        sumOfMonthlyExpenses = monthlyExpenses.stream()
                .mapToInt(Transaction::getAmount)
                .map(Math::abs)
                .sum();

        expenseTotalSum.setText(String.format("-%.2f", getValueInCurrency(sumOfMonthlyExpenses)));
        expenseMonthlyAverage.setText(String.format("Average: %.2f / day", getValueInCurrency(sumOfMonthlyExpenses / numberOfDaysInMonth)));

        new BarChartLimitDrawer(monthlyBarChart, getContext(), monthlyExpenses, numberOfDaysInMonth, monthlyLimit);
    }

    public LimitsFragmentData getDataFromLimitsFragment() {
        return new LimitsFragmentData(currentChosenMonth, currentChosenYear, sumOfMonthlyExpenses);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void update(int newMonthlyLimit) {
        monthlyLimit = newMonthlyLimit;
        updateData();
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
}