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
import java.util.Calendar;

public class IncomeActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener{

    private Toolbar toolbar;
    EditText editNameIncome, editAmountIncome, editDateIncome;
    ImageButton editDateIncomeButton;
    Button acceptIncomeButton;

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
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch(v.getId()){
            case R.id.editNameIncome:
                if (hasFocus) {
                    editNameIncome.setHint("");
                } else {
                    editNameIncome.setHint("Nazwa");
                }
                break;
            case R.id.editAmountIncome:
                if(hasFocus){
                    editAmountIncome.setHint("");
                }
                else{
                    editAmountIncome.setHint("Kwota");
                }
                break;
            case R.id.editDateIncome:
                if(hasFocus){
                    editDateIncome.setHint("");
                }
                else{
                    editDateIncome.setHint("Data");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.editDateIncomeButton:
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, returnedYear, returnedMonth, returnedDayOfMonth) -> {
                    editDateIncome.setText(returnedDayOfMonth + "/" + returnedMonth + "/" + returnedYear);
                }, year, month, day);
                datePickerDialog.show();
                break;
            case R.id.acceptIncomeButton:
                //TODO implenement logic button
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
}