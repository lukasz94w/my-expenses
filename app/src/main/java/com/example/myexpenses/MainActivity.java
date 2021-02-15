package com.example.myexpenses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView headerTextView = (TextView) findViewById(R.id.headerTextView);
        Button butonEnterExpense = (Button) findViewById(R.id.buttonEnterExpense);
        Button buttonEnterIncome = (Button) findViewById(R.id.buttonEnterIncome);

    }
}