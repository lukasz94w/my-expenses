package com.example.myexpenses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.myexpenses.model.Income;
import com.example.myexpenses.repository.IncomeRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class IncomeActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private Toolbar toolbar;
    EditText editNameIncome, editAmountIncome, editDateIncome;
    ImageButton editDateIncomeButton;
    Button acceptIncomeButton;
    IncomeRepository incomeRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Wprowadź przychód");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editNameIncome = findViewById(R.id.editNameIncome);
        editNameIncome.setOnFocusChangeListener(this);

        editAmountIncome = findViewById(R.id.editAmountIncome);
        editAmountIncome.setOnFocusChangeListener(this);

        editDateIncome = findViewById(R.id.editDateIncome);
        editDateIncome.setOnFocusChangeListener(this);
        editDateIncomeButton = findViewById(R.id.editDateIncomeButton);
        editDateIncomeButton.setOnClickListener(this);

        acceptIncomeButton = findViewById(R.id.acceptIncomeButton);
        acceptIncomeButton.setOnClickListener(this);

        incomeRepository = new IncomeRepository(this);

        //initialize current date on date picker
        Calendar calendar = Calendar.getInstance();
        String currentDateAsString = convertDateToString(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        editDateIncome.setText(currentDateAsString);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.editNameIncome:
                if (hasFocus) {
                    editNameIncome.setHint("");
                } else {
                    editNameIncome.setHint("Nazwa");
                }
                break;
            case R.id.editAmountIncome:
                if (hasFocus) {
                    editAmountIncome.setHint("");
                } else {
                    editAmountIncome.setHint("Kwota");
                }
                break;
            case R.id.editDateIncome:
                if (hasFocus) {
                    editDateIncome.setHint("");
                } else {
                    editDateIncome.setHint("Data");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editDateIncomeButton:
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, returnedYear, returnedMonth, returnedDayOfMonth) -> {
                    String chosenDate = convertDateToString(returnedDayOfMonth, returnedMonth, returnedYear);
                    editDateIncome.setText(chosenDate);
                }, year, month, day);
                datePickerDialog.show();
                break;
            case R.id.acceptIncomeButton:
                incomeRepository.create(new Income(editNameIncome.getText().toString(), Float.valueOf(editAmountIncome.getText().toString()), convertStringToDate(editDateIncome.getText().toString())));
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