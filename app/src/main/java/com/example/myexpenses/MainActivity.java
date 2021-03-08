package com.example.myexpenses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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


//        Button buttonEnterIncome = findViewById(R.id.enterIncome);
//        Button buttonSummary = findViewById(R.id.enterSummary);
//
//        expense.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
//            startActivity(intent);
//        });
//
//        buttonEnterIncome.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, IncomeActivity.class);
//            startActivity(intent);
//        });
//
//        buttonSummary.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
//            startActivity(intent);
//        });
//
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