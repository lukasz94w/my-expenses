package com.example.myexpenses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ExpenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        TextView headerTextView = (TextView) findViewById(R.id.headerTextView);
        TextView amount = (TextView) findViewById(R.id.amount);
        EditText editAmount = (EditText) findViewById(R.id.editAmount);

        TextView category = (TextView) findViewById(R.id.category);

        Button buttonAcceptExpense = (Button) findViewById(R.id.buttonAcceptExpense);
    }
}