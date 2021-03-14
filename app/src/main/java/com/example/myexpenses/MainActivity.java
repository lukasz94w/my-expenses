package com.example.myexpenses;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.myexpenses.model.Expense;
import com.example.myexpenses.model.Income;
import com.example.myexpenses.repository.ExpenseRepository;
import com.example.myexpenses.repository.IncomeRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private CardView expense, income, limits, summary;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expense = findViewById(R.id.expense);
        expense.setOnClickListener(this);

        income = findViewById(R.id.income);
        income.setOnClickListener(this);

        limits = findViewById(R.id.limits);
        limits.setOnClickListener(this);

        summary = findViewById(R.id.summary);
        summary.setOnClickListener(this);

        IncomeRepository incomeRepository = new IncomeRepository(this);
        ExpenseRepository expenseRepository = new ExpenseRepository(this);
        Date currentTime = Calendar.getInstance().getTime();
//        incomeRepository.deleteAllRecords();
//        expenseRepository.deleteAllRecords();

//        incomeRepository.create(new Income("Przychod nr 1", 1, currentTime));
//        incomeRepository.create(new Income("Przychod nr 2", 2, currentTime));
//
//        expenseRepository.create(new Expense("Wydatek nr 1", 1, "Samochody", currentTime));
//        expenseRepository.create(new Expense("Wydatek nr 2", 2, "Ubrania", currentTime));

        List<Income> incomeList = incomeRepository.findAll();
        for (Income income : incomeList) {
            String log = "Id: " + income.getId() + " || Name: " + income.getName() + " || Kwota: " + income.getAmount() + " || Date: " + income.getDate();
            Log.d("INCOME: ", log);
        }

        List<Expense> expenseList = expenseRepository.findAll();
        for (Expense expense : expenseList) {
            String log = "Id: " + expense.getId() + " || Name: " + expense.getName() + " || Kwota: " + expense.getAmount() + " || Category: " + expense.getCategory() + " || Date: " + expense.getDate();
            Log.d("EXPENSE: ", log);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.expense:
                intent = new Intent(MainActivity.this, ExpenseActivity.class);
                startActivity(intent);
                break;
            case R.id.income:
                intent = new Intent(MainActivity.this, IncomeActivity.class);
                startActivity(intent);
                break;
            case R.id.limits:
                //TODO dodac przejscie do aktywnosci limity
                break;
            case R.id.summary:
                intent = new Intent(MainActivity.this, SummaryActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}