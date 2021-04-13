package com.example.myexpenses;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TransactionActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editType, editName, editAmount, editDate, editCategory;
    ImageButton editDateButton, editCategoryButton;
    Button acceptButton;
    SharedPreferences sharedPreferences;

    Button editDatee;
    Button editTodayTomorrow;

    int LAUNCH_CATEGORY_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_new_transaction);


        //initialize current date on date picker
        Calendar calendar = Calendar.getInstance();
        String currentDateAsString = convertDateToString(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));

        editDatee = findViewById(R.id.chooseTransactionDate);
        editDatee.setText(currentDateAsString);
        editDatee.setOnClickListener(this);

        editTodayTomorrow = findViewById(R.id.chooseTodayTommorow);
        editTodayTomorrow.setOnClickListener(this);


//        editName = findViewById(R.id.editName);
//        editAmount = findViewById(R.id.editAmount);
//        editType = findViewById(R.id.editType);
//        editDate = findViewById(R.id.editDate);
//        editDateButton = findViewById(R.id.editDateButton);
//        editDateButton.setOnClickListener(this);
//        editCategory = findViewById(R.id.editCategory);
//        editCategoryButton = findViewById(R.id.editCategoryButton);
//        editCategoryButton.setOnClickListener(this);
        acceptButton = findViewById(R.id.saveTransactionButton);
        acceptButton.setOnClickListener(this);
        sharedPreferences = getSharedPreferences("Limits", MODE_PRIVATE);

//        editDate.setText(currentDateAsString);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chooseTransactionDate:
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, returnedYear, returnedMonth, returnedDayOfMonth) -> {
                    String chosenDate = convertDateToString(returnedDayOfMonth, returnedMonth, returnedYear);
                    editDatee.setText(chosenDate);
                    if(day == returnedDayOfMonth && month == returnedMonth && year == returnedYear) {
                        editTodayTomorrow.setText("Yesterday");
                    }
                    else {
                        editTodayTomorrow.setText("Today");
                    }

                }, year, month, day);
                datePickerDialog.show();
                break;
            case R.id.chooseTodayTommorow:
                if (editTodayTomorrow.getText().toString().equals("Today")) {
                    final Calendar calendarYesterday = Calendar.getInstance();
                    int yearYesterday = calendarYesterday.get(Calendar.YEAR);
                    int monthYesterday = calendarYesterday.get(Calendar.MONTH);
                    int dayYesterday = calendarYesterday.get(Calendar.DAY_OF_MONTH);
                    String yesterday = convertDateToString(dayYesterday, monthYesterday, yearYesterday);
                    editTodayTomorrow.setText("Yesterday");
                    editDatee.setText(yesterday);
                } else {
                    final Calendar calendarYesterday = Calendar.getInstance();
                    int yearYesterday = calendarYesterday.get(Calendar.YEAR);
                    calendarYesterday.add(Calendar.DATE, -1);
                    int monthYesterday = calendarYesterday.get(Calendar.MONTH);
                    int dayYesterday = calendarYesterday.get(Calendar.DAY_OF_MONTH);
                    String yesterday = convertDateToString(dayYesterday, monthYesterday, yearYesterday);
                    editTodayTomorrow.setText("Today");
                    editDatee.setText(yesterday);
                }
                break;


//            case R.id.editDateButton:
//                final Calendar calendar = Calendar.getInstance();
//                int year = calendar.get(Calendar.YEAR);
//                int month = calendar.get(Calendar.MONTH);
//                int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//                DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, returnedYear, returnedMonth, returnedDayOfMonth) -> {
//                    String chosenDate = convertDateToString(returnedDayOfMonth, returnedMonth, returnedYear);
//                    editDate.setText(chosenDate);
//                }, year, month, day);
//                datePickerDialog.show();
//                break;
//            case R.id.editCategoryButton:
//                Intent intent = new Intent(TransactionActivity.this, CategoryActivity.class);
//                startActivityForResult(intent, LAUNCH_CATEGORY_ACTIVITY);
//                break;
            case R.id.saveTransactionButton:
                //default values if they haven't been initialized yet
                int dailyLimit = sharedPreferences.getInt("Daily limit", 1000);
                int monthlyLimit = sharedPreferences.getInt("Monthly limit", 5000);

//                int sumOfDailyExpenses = returnSumOfDailyExpenses(editDateExpense.getText().toString());
//                int sumOfMonthlyExpenses = returnSumOfMonthlyExpenses(editDateExpense.getText().toString());

                int sumOfDailyExpenses = 0;
                int sumOfMonthlyExpenses = 0;

                TransactionRepository transactionRepository = new TransactionRepository(this);
                transactionRepository.create(new Transaction(Integer.parseInt(editType.getText().toString()), editName.getText().toString(), Float.valueOf(editAmount.getText().toString()), editCategory.getText().toString(), convertStringToDate(editDate.getText().toString())));
                int test = 0;


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

        return dd + "." + MM + "." + yyyy;
    }
}