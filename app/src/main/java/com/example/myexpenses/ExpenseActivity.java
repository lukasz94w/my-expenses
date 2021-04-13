package com.example.myexpenses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.myexpenses.model.Expense;
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
    SharedPreferences sharedPreferences;

    int LAUNCH_CATEGORY_ACTIVITY = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

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

        sharedPreferences = getSharedPreferences("Limits", MODE_PRIVATE);

        //initialize current date on date picker
        Calendar calendar = Calendar.getInstance();
        String currentDateAsString = convertDateToString(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        editDateExpense.setText(currentDateAsString);
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
                    String chosenDate = convertDateToString(returnedDayOfMonth, returnedMonth, returnedYear);
                    editDateExpense.setText(chosenDate);
                }, year, month, day);
                datePickerDialog.show();
                break;
            case R.id.editCategoryExpenseButton:
                Intent intent = new Intent(ExpenseActivity.this, CategoryActivity.class);
                startActivityForResult(intent, LAUNCH_CATEGORY_ACTIVITY);
                break;
            case R.id.acceptExpenseButton:
                //default values if they haven't been initialized yet
                int dailyLimit = sharedPreferences.getInt("Daily limit", 1000);
                int monthlyLimit = sharedPreferences.getInt("Monthly limit", 5000);

                int sumOfDailyExpenses = returnSumOfDailyExpenses(editDateExpense.getText().toString());
                int sumOfMonthlyExpenses = returnSumOfMonthlyExpenses(editDateExpense.getText().toString());

                ExpenseRepository expenseRepository = new ExpenseRepository(this);
                expenseRepository.create(new Expense(editNameExpense.getText().toString(), Float.valueOf(editAmountExpense.getText().toString()), editCategoryExpense.getText().toString(), convertStringToDate(editDateExpense.getText().toString())));

                if (sumOfDailyExpenses >= dailyLimit) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(
                            R.string.limit_title).setMessage(getResources().getString(R.string.limit_monthly_message_part_1) + " " +
                            sumOfMonthlyExpenses + " " + getResources().getString(R.string.limit_monthly_message_part_2) + " " + monthlyLimit
                    );
                    builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> {
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                if (sumOfMonthlyExpenses >= monthlyLimit) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(
                            R.string.limit_title).setMessage(getResources().getString(R.string.limit_daily_message_part_1) + " " +
                            sumOfDailyExpenses + " " + getResources().getString(R.string.limit_daily_message_part_2) + " " + dailyLimit
                    );
                    builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> {
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
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

    public int returnSumOfDailyExpenses(String chosenDate) {
        String[] dateParts = chosenDate.split("/");

        Calendar startOfChosenDay = Calendar.getInstance();
        Calendar endOfChosenDay = Calendar.getInstance();

        startOfChosenDay.set(Calendar.MILLISECOND, 0);
        startOfChosenDay.set(Calendar.SECOND, 0);
        startOfChosenDay.set(Calendar.MINUTE, 0);
        startOfChosenDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfChosenDay.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
        startOfChosenDay.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
        startOfChosenDay.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));

        endOfChosenDay.set(Calendar.MILLISECOND, 999);
        endOfChosenDay.set(Calendar.SECOND, 59);
        endOfChosenDay.set(Calendar.MINUTE, 59);
        endOfChosenDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfChosenDay.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
        endOfChosenDay.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
        endOfChosenDay.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));

        Long startOfChosenDayAsLong = startOfChosenDay.getTime().getTime();
        Long endOfChosenDayAsLong = endOfChosenDay.getTime().getTime();

        ExpenseRepository expenseRepository = new ExpenseRepository(this);
        int sumOfDailyExpenses = expenseRepository.returnSumOfExpenses(startOfChosenDayAsLong, endOfChosenDayAsLong);

        return sumOfDailyExpenses;
    }

    public int returnSumOfMonthlyExpenses(String chosenDate) {
        String[] dateParts = chosenDate.split("/");

        Calendar startOfChosenMonth = Calendar.getInstance();
        Calendar endOfChosenMonth = Calendar.getInstance();

        startOfChosenMonth.set(Calendar.MILLISECOND, 0);
        startOfChosenMonth.set(Calendar.SECOND, 0);
        startOfChosenMonth.set(Calendar.MINUTE, 0);
        startOfChosenMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfChosenMonth.set(Calendar.DAY_OF_MONTH, startOfChosenMonth.getActualMinimum(Calendar.DAY_OF_MONTH));
        startOfChosenMonth.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
        startOfChosenMonth.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));

        endOfChosenMonth.set(Calendar.MILLISECOND, 999);
        endOfChosenMonth.set(Calendar.SECOND, 59);
        endOfChosenMonth.set(Calendar.MINUTE, 59);
        endOfChosenMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfChosenMonth.set(Calendar.DAY_OF_MONTH, endOfChosenMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        endOfChosenMonth.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
        endOfChosenMonth.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));

        Long startOfChosenMonthAsLong = startOfChosenMonth.getTime().getTime();
        Long endOfChosenMonthAsLong = endOfChosenMonth.getTime().getTime();

        ExpenseRepository expenseRepository = new ExpenseRepository(this);
        int sumOfMonthlyExpenses = expenseRepository.returnSumOfExpenses(startOfChosenMonthAsLong, endOfChosenMonthAsLong);

        return sumOfMonthlyExpenses;
    }

    public Date convertStringToDate(String dateAsString) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        try {
            date = format.parse(dateAsString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertDateToString(int dayOfMonth, int month, int year) {
        month = month + 1; //months are indexed starting at 0
        String yyyy = "" + year;
        String MM = "" + month;
        String dd = "" + dayOfMonth;
        if (month < 10) {
            MM = "0" + month;
        }
        if (dayOfMonth < 10) {
            dd = "0" + dayOfMonth;
        }

        return dd + "/" + MM + "/" + yyyy;
    }
}