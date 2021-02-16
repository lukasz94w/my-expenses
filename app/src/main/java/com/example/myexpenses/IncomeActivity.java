package com.example.myexpenses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class IncomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        TextView headerIncome = (TextView) findViewById(R.id.headerIncome);
        TextView name = (TextView) findViewById(R.id.name);
        EditText editName = (EditText) findViewById(R.id.editName);

        TextView amount = (TextView) findViewById(R.id.amount);


    }
}