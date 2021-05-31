package com.example.myexpenses;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;

import com.example.myexpenses.repository.TransactionRepository;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

public class ExportActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener {

    protected int dayDateFrom, monthDateFrom, yearDateFrom;
    protected int dayDateTo, monthDateTo, yearDateTo;
    private Button chooseTransactionDateFrom, chooseTransactionDateTo;
    private Spinner chooseTransactionCategoryToBeSaved;
    private ImageView transactionCategoryImageToBeSaved;
    private RadioGroup chooseTransactionTypeToBeSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.export));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button saveToFile = findViewById(R.id.saveToFile);
        saveToFile.setOnClickListener(this);

        //initialize ranges
        Calendar calendar = Calendar.getInstance();
        dayDateFrom = 1;
        monthDateFrom = 0; //months are indexed from 0
        yearDateFrom = calendar.get(Calendar.YEAR);
        dayDateTo = calendar.get(Calendar.DAY_OF_MONTH);
        monthDateTo = calendar.get(Calendar.MONTH); //months are indexed from 0
        yearDateTo = calendar.get(Calendar.YEAR);

        chooseTransactionDateFrom = findViewById(R.id.chooseTransactionDateFrom);
        chooseTransactionDateFrom.setOnClickListener(this);
        chooseTransactionDateTo = findViewById(R.id.chooseTransactionDateTo);
        chooseTransactionDateTo.setOnClickListener(this);

        chooseTransactionDateFrom.setText(convertDateToString(dayDateFrom, monthDateFrom + 1, yearDateFrom));
        chooseTransactionDateTo.setText(convertDateToString(dayDateTo, monthDateTo + 1, yearDateTo));

        chooseTransactionTypeToBeSaved = findViewById(R.id.chooseTransactionTypeToBeSaved);
        chooseTransactionTypeToBeSaved.setOnCheckedChangeListener(this);

        transactionCategoryImageToBeSaved = findViewById(R.id.transactionCategoryImageToBeSaved);
        chooseTransactionCategoryToBeSaved = findViewById(R.id.chooseTransactionsCategoryToBeSaved);
        ArrayAdapter<String> chooseCategoryAdapter = new ArrayAdapter<>(ExportActivity.this, R.layout.spinner_in_export_activity, getResources().getStringArray(R.array.list_of_expenses));
        chooseTransactionCategoryToBeSaved.setAdapter(chooseCategoryAdapter);
        chooseTransactionCategoryToBeSaved.setOnItemSelectedListener(this);
    }

    private boolean checkIfPermissionIsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    public String convertDateToString(int dayOfMonth, int month, int year) {
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

    private String getFileName(boolean activateDateSelection, int dayDateFrom, int monthDateFrom, int yearDateFrom, int dayDateTo, int monthDateTo, int yearDateTo) {
        if (activateDateSelection) {
            String dayDateFromAsString = String.valueOf(dayDateFrom);
            String monthDateFromAsString = String.valueOf(monthDateFrom + 1);
            String dayDateToAsString = String.valueOf(dayDateTo);
            String monthDateToAsString = String.valueOf(monthDateTo + 1);
            if (dayDateFrom < 10) {
                dayDateFromAsString = "0" + dayDateFromAsString;
            }
            if (monthDateFrom < 9) {
                monthDateFromAsString = "0" + monthDateFromAsString;
            }
            if (dayDateTo < 10) {
                dayDateToAsString = "0" + dayDateToAsString;
            }
            if (monthDateTo < 9) {
                monthDateToAsString = "0" + monthDateToAsString;
            }
            return "MyFinances_" + dayDateFromAsString + "." + monthDateFromAsString + "." + yearDateFrom + "-" + dayDateToAsString + "." + monthDateToAsString + "." + yearDateTo + ".csv";
        } else
            return "MyFinances.csv";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveToFile: {

                //first check if we have permission to save into the download directory
                if (!checkIfPermissionIsGranted()) {
                    Toast.makeText(getApplicationContext(), "Grant permission first", Toast.LENGTH_SHORT).show();
                    break;
                }
                //if we have permission then create .csv file
                new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case R.id.chooseTransactionDateFrom: {

                DatePickerDialog datePickerDialog = new DatePickerDialog(ExportActivity.this, R.style.DialogTheme, (view, chosenYear, chosenMonth, chosenDayOfMonth) -> {
                    yearDateFrom = chosenYear;
                    monthDateFrom = chosenMonth;
                    dayDateFrom = chosenDayOfMonth;
                    chooseTransactionDateFrom.setText(convertDateToString(dayDateFrom, monthDateFrom + 1, yearDateFrom));

                }, yearDateFrom, monthDateFrom, dayDateFrom);

                datePickerDialog.show();
                break;
            }
            case R.id.chooseTransactionDateTo: {

                DatePickerDialog datePickerDialog = new DatePickerDialog(ExportActivity.this, R.style.DialogTheme, (view, chosenYear, chosenMonth, chosenDayOfMonth) -> {
                    yearDateTo = chosenYear;
                    monthDateTo = chosenMonth;
                    dayDateTo = chosenDayOfMonth;
                    chooseTransactionDateTo.setText(convertDateToString(dayDateTo, monthDateTo + 1, yearDateTo));

                }, yearDateTo, monthDateTo, dayDateTo);

                datePickerDialog.show();
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.chooseTransactionsCategoryToBeSaved: {
                String transactionTypeName = chooseTransactionCategoryToBeSaved.getSelectedItem().toString().toLowerCase().replace(" ", "_");
                int idOfSuitableDrawableForTransactionTypeName = Objects.requireNonNull(getApplicationContext()).getResources().getIdentifier(transactionTypeName, "drawable", getApplicationContext().getPackageName());
                transactionCategoryImageToBeSaved.setBackgroundResource(idOfSuitableDrawableForTransactionTypeName);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.chooseTransactionTypeToBeSaved) {
            int idOfCheckedRadiobutton = chooseTransactionTypeToBeSaved.getCheckedRadioButtonId();
            RadioButton checkedRadiobutton = findViewById(idOfCheckedRadiobutton);
            String chosenTransactionTypeName = checkedRadiobutton.getText().toString();

            ArrayAdapter<String> chooseCategoryAdapter;
            if (chosenTransactionTypeName.equals("Expenses")) {
                chooseCategoryAdapter = new ArrayAdapter<>(ExportActivity.this, R.layout.spinner_in_export_activity, getResources().getStringArray(R.array.list_of_expenses));
            } else {
                chooseCategoryAdapter = new ArrayAdapter<String>(ExportActivity.this, R.layout.spinner_in_export_activity, getResources().getStringArray(R.array.list_of_incomes));
            }
            chooseTransactionCategoryToBeSaved.setAdapter(chooseCategoryAdapter);
        }
    }

    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(ExportActivity.this);
        private TransactionRepository transactionRepository;
        private int dayDateFrom, monthDateFrom, yearDateFrom, dayDateTo, monthDateTo, yearDateTo;
        private int transactionType;
        private String transactionCategory;
        private String fileName;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Saving file...");
            this.dialog.show();
            transactionRepository = new TransactionRepository(ExportActivity.this);
            CheckBox activateDateSelection = findViewById(R.id.activateDateSelection);
            CheckBox activateTransactionTypeSelection = findViewById(R.id.activateTransactionTypeSelection);
            CheckBox activateTransactionCategorySelection = findViewById(R.id.activateTransactionCategorySelection);

            if (activateDateSelection.isChecked()) {
                this.dayDateFrom = ExportActivity.this.dayDateFrom;
                this.monthDateFrom = ExportActivity.this.monthDateFrom;
                this.yearDateFrom = ExportActivity.this.yearDateFrom;
                this.dayDateTo = ExportActivity.this.dayDateTo;
                this.monthDateTo = ExportActivity.this.monthDateTo;
                this.yearDateTo = ExportActivity.this.yearDateTo;
            }

            transactionType = 2;
            transactionCategory = "";
            fileName = getFileName(activateDateSelection.isChecked(), dayDateFrom, monthDateFrom, yearDateFrom, dayDateTo, monthDateTo, yearDateTo);

            if (activateTransactionTypeSelection.isChecked()) {
                int idOfCheckedRadiobutton = chooseTransactionTypeToBeSaved.getCheckedRadioButtonId();
                RadioButton checkedRadiobutton = findViewById(idOfCheckedRadiobutton);
                String transactionTypeName = checkedRadiobutton.getText().toString();
                if (transactionTypeName.equals("Expenses")) {
                    transactionType = 1;
                } else {
                    transactionType = 0;
                }
            }

            if (activateTransactionCategorySelection.isChecked()) {
                transactionCategory = chooseTransactionCategoryToBeSaved.getSelectedItem().toString();
            }
        }

        protected Boolean doInBackground(final String... args) {

            File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, fileName);
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file), ';', '\0', '\0', "\n");
                Cursor curCSV = transactionRepository.raw(
                        dayDateFrom,
                        monthDateFrom,
                        yearDateFrom,
                        dayDateTo,
                        monthDateTo,
                        yearDateTo,
                        transactionType,
                        transactionCategory);

                csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    //export all columns from db
                    for (int i = 0; i < curCSV.getColumnNames().length; i++) {
                        mySecondStringArray[i] = curCSV.getString(i);
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }

                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                Toast.makeText(ExportActivity.this, "File saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ExportActivity.this, "Error during saving file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //prevent left checked icon on navigation drawer and also clear filters f.e.
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }
}
