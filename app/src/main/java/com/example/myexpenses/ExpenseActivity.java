package com.example.myexpenses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.myexpenses.model.Expense;
import com.example.myexpenses.model.Income;
import com.example.myexpenses.repository.ExpenseRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExpenseActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private Toolbar toolbar;
    EditText editNameExpense, editAmountExpense, editDateExpense, editCategoryExpense;
    ImageButton editDateExpenseButton, editCategoryExpenseButton;
    Button acceptExpenseButton;
    ExpenseRepository expenseRepository;

    int LAUNCH_CATEGORY_ACTIVITY = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Wprowadź wydatek");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editNameExpense = findViewById(R.id.editNameExpense);
        editNameExpense.setOnFocusChangeListener(this);

        editAmountExpense = findViewById(R.id.editAmountExpense);
        editAmountExpense.setOnFocusChangeListener(this);

        editDateExpense = findViewById(R.id.editDateExpense);
        editDateExpense.setOnFocusChangeListener(this);
        editDateExpenseButton = findViewById(R.id.editDateExpenseButton);
        editDateExpenseButton.setOnClickListener(this);

        editCategoryExpense = findViewById(R.id.editCategoryExpense);
        editCategoryExpense.setOnFocusChangeListener(this);

        editCategoryExpenseButton = findViewById(R.id.editCategoryExpenseButton);
        editCategoryExpenseButton.setOnClickListener(this);

        acceptExpenseButton = findViewById(R.id.acceptExpenseButton);
        acceptExpenseButton.setOnClickListener(this);

        expenseRepository = new ExpenseRepository(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.editNameExpense:
                if (hasFocus) {
                    editNameExpense.setHint("");
                } else {
                    editNameExpense.setHint("Nazwa");
                }
                break;
            case R.id.editAmountExpense:
                if (hasFocus) {
                    editAmountExpense.setHint("");
                } else {
                    editAmountExpense.setHint("Kwota");
                }
                break;
            case R.id.editDateExpense:
                if (hasFocus) {
                    editDateExpense.setHint("");
                } else {
                    editDateExpense.setHint("Data");
                }
                break;
            case R.id.editCategoryExpense:
                if (hasFocus) {
                    editCategoryExpense.setHint("");
                } else {
                    editCategoryExpense.setHint("Kategoria");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editDateExpenseButton:
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, returnedYear, returnedMonth, returnedDayOfMonth) -> {
                    returnedMonth = returnedMonth + 1; //months are indexed starting at 0
                    String yyyy = ""  + returnedYear;
                    String MM = "" + returnedMonth;
                    String dd = "" + returnedDayOfMonth;
                    if (returnedMonth < 10) {
                        MM = "0" + returnedMonth;
                    }
                    if (returnedDayOfMonth < 10) {
                        dd = "0" + returnedDayOfMonth;
                    }
                    editDateExpense.setText(yyyy + "/" + MM + "/" + dd);
                }, year, month, day);
                datePickerDialog.show();
                break;
            case R.id.editCategoryExpenseButton:
                Intent intent = new Intent(ExpenseActivity.this, CategoryActivity.class);
                startActivityForResult(intent, LAUNCH_CATEGORY_ACTIVITY);
                break;
            case R.id.acceptExpenseButton:
                expenseRepository.create(new Expense(editNameExpense.getText().toString(), Float.valueOf(editAmountExpense.getText().toString()), editCategoryExpense.getText().toString(),  convertStringToDate(editDateExpense.getText().toString())));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:{
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_CATEGORY_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                editCategoryExpense.setText(data.getStringExtra("CHOSEN_CATEGORY"));
            }
            if (requestCode == RESULT_CANCELED) {
                ;
            }
        }
    }

    public Date convertStringToDate(String dateAsString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        try {
            date = format.parse(dateAsString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}