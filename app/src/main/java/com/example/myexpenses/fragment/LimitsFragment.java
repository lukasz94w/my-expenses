package com.example.myexpenses.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.myexpenses.R;
import com.example.myexpenses.repository.ExpenseRepository;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class LimitsFragment extends Fragment implements View.OnClickListener {

    private Button acceptLimitButton;
    private SharedPreferences sharedPreferences;
    private EditText dailyLimitSetAmount, monthlyLimitSetAmount;
    private TextView dailyLimitLeftAmount, monthlyLimitLeftAmount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getActivity().getSharedPreferences("Limits", MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_limits, container, false);

        dailyLimitSetAmount = view.findViewById(R.id.dailyLimitSetAmount);
        monthlyLimitSetAmount = view.findViewById(R.id.monthlyLimitSetAmount);
        dailyLimitLeftAmount = view.findViewById(R.id.dailyLimitLeftAmount);
        monthlyLimitLeftAmount = view.findViewById(R.id.monthlyLimitLeftAmount);

        acceptLimitButton = view.findViewById(R.id.acceptLimitButton);
        acceptLimitButton.setOnClickListener(this);

        //default values if they not have been initialized yet
        int dailyLimit = sharedPreferences.getInt("Daily limit", 1000);
        int monthlyLimit = sharedPreferences.getInt("Monthly limit", 5000);

        int sumOfDailyExpensesCurrentDay = returnSumOfDailyExpensesCurrentDay();
        int sumOfMonthlyExpensesCurrentMonth = returnSumOfMonthlyExpensesCurrentMonth();

        dailyLimitSetAmount.setText(String.valueOf(dailyLimit));
        monthlyLimitSetAmount.setText(String.valueOf(monthlyLimit));
        dailyLimitLeftAmount.setText(String.valueOf(dailyLimit - sumOfDailyExpensesCurrentDay));
        monthlyLimitLeftAmount.setText(String.valueOf(monthlyLimit - sumOfMonthlyExpensesCurrentMonth));

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.acceptLimitButton:
                SharedPreferences.Editor editor = sharedPreferences.edit();

                int newDailyLimit = Integer.parseInt(dailyLimitSetAmount.getText().toString());
                int newMonthlyLimit = Integer.parseInt(monthlyLimitSetAmount.getText().toString());

                editor.putInt("Daily limit", newDailyLimit);
                editor.putInt("Monthly limit", newMonthlyLimit);
                editor.commit();

                int newDailyLimitLeft = returnSumOfDailyExpensesCurrentDay();
                int newMonthlyLimitLeft = returnSumOfMonthlyExpensesCurrentMonth();

                dailyLimitLeftAmount.setText(String.valueOf(newDailyLimit - newDailyLimitLeft));
                monthlyLimitLeftAmount.setText(String.valueOf(newMonthlyLimit - newMonthlyLimitLeft));
        }
    }

    public int returnSumOfDailyExpensesCurrentDay() {
        Calendar startOfChosenDay = Calendar.getInstance();
        Calendar endOfChosenDay = Calendar.getInstance();

        startOfChosenDay.set(Calendar.MILLISECOND, 0);
        startOfChosenDay.set(startOfChosenDay.get(Calendar.YEAR), startOfChosenDay.get(Calendar.MONTH), startOfChosenDay.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        endOfChosenDay.set(Calendar.MILLISECOND, 999);
        endOfChosenDay.set(endOfChosenDay.get(Calendar.YEAR), endOfChosenDay.get(Calendar.MONTH), endOfChosenDay.get(Calendar.DAY_OF_MONTH), 23, 59, 59);

        Long startOfChosenDayAsLong = startOfChosenDay.getTime().getTime();
        Long endOfChosenDayAsLong = endOfChosenDay.getTime().getTime();

        ExpenseRepository expenseRepository = new ExpenseRepository(this.getActivity());
        int sumOfDailyExpenses = expenseRepository.returnSumOfExpenses(startOfChosenDayAsLong, endOfChosenDayAsLong);

        return sumOfDailyExpenses;
    }

    public int returnSumOfMonthlyExpensesCurrentMonth() {
        Calendar startOfChosenMonth = Calendar.getInstance();
        Calendar endOfChosenMonth = Calendar.getInstance();

        startOfChosenMonth.set(Calendar.MILLISECOND, 0);
        startOfChosenMonth.set(startOfChosenMonth.get(Calendar.YEAR), startOfChosenMonth.get(Calendar.MONTH), startOfChosenMonth.getActualMinimum(Calendar.DAY_OF_MONTH), 0, 0, 0);
        endOfChosenMonth.set(Calendar.MILLISECOND, 999);
        endOfChosenMonth.set(endOfChosenMonth.get(Calendar.YEAR), endOfChosenMonth.get(Calendar.MONTH), endOfChosenMonth.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

        Long startOfChosenMonthAsLong = startOfChosenMonth.getTime().getTime();
        Long endOfChosenMonthAsLong = endOfChosenMonth.getTime().getTime();

        ExpenseRepository expenseRepository = new ExpenseRepository(this.getActivity());
        int sumOfMonthlyExpenses = expenseRepository.returnSumOfExpenses(startOfChosenMonthAsLong, endOfChosenMonthAsLong);

        return sumOfMonthlyExpenses;
    }
}