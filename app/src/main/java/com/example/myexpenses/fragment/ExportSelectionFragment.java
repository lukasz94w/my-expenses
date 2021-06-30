package com.example.myexpenses.fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myexpenses.R;
import com.example.myexpenses.repository.TransactionRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class ExportSelectionFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener {

    protected int dayDateFrom, monthDateFrom, yearDateFrom;
    protected int dayDateTo, monthDateTo, yearDateTo;
    private Button chooseTransactionDateFrom, chooseTransactionDateTo;
    private Spinner chooseTransactionCategoryToBeSaved;
    private ImageView transactionCategoryImageToBeSaved;
    private RadioGroup chooseTransactionTypeToBeSaved;

    private final int REQUEST_CODE_CREATE_FILE = 100;

    public ExportSelectionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize ranges
        Calendar calendar = Calendar.getInstance();
        //1st day of actual year
        dayDateFrom = 1;
        monthDateFrom = 0; //months are indexed from 0
        yearDateFrom = calendar.get(Calendar.YEAR);
        //today
        dayDateTo = calendar.get(Calendar.DAY_OF_MONTH);
        monthDateTo = calendar.get(Calendar.MONTH); //months are indexed from 0
        yearDateTo = calendar.get(Calendar.YEAR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export_selection, container, false);

        chooseTransactionDateFrom = view.findViewById(R.id.chooseTransactionDateFrom);
        chooseTransactionDateFrom.setOnClickListener(this);
        chooseTransactionDateTo = view.findViewById(R.id.chooseTransactionDateTo);
        chooseTransactionDateTo.setOnClickListener(this);

        chooseTransactionDateFrom.setText(convertDateToString(dayDateFrom, monthDateFrom + 1, yearDateFrom));
        chooseTransactionDateTo.setText(convertDateToString(dayDateTo, monthDateTo + 1, yearDateTo));

        chooseTransactionTypeToBeSaved = view.findViewById(R.id.chooseTransactionTypeToBeSaved);
        chooseTransactionTypeToBeSaved.setOnCheckedChangeListener(this);

        transactionCategoryImageToBeSaved = view.findViewById(R.id.transactionCategoryImageToBeSaved);
        chooseTransactionCategoryToBeSaved = view.findViewById(R.id.chooseTransactionsCategoryToBeSaved);
        ArrayAdapter<String> chooseCategoryAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_in_export_activity, getResources().getStringArray(R.array.list_of_expenses));
        chooseTransactionCategoryToBeSaved.setAdapter(chooseCategoryAdapter);
        chooseTransactionCategoryToBeSaved.setOnItemSelectedListener(this);

        Button saveToFile = view.findViewById(R.id.saveToFile);
        saveToFile.setOnClickListener(this);
        return view;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveToFile: {

                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                String fileName = getFileName();
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                startActivityForResult(intent, REQUEST_CODE_CREATE_FILE);

                break;
            }

            case R.id.chooseTransactionDateFrom: {

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DialogTheme, (view, chosenYear, chosenMonth, chosenDayOfMonth) -> {
                    yearDateFrom = chosenYear;
                    monthDateFrom = chosenMonth;
                    dayDateFrom = chosenDayOfMonth;
                    chooseTransactionDateFrom.setText(convertDateToString(dayDateFrom, monthDateFrom + 1, yearDateFrom));

                }, yearDateFrom, monthDateFrom, dayDateFrom);

                datePickerDialog.show();
                break;
            }
            case R.id.chooseTransactionDateTo: {

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DialogTheme, (view, chosenYear, chosenMonth, chosenDayOfMonth) -> {
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CREATE_FILE: {
                if (resultCode == RESULT_OK) {

                    if(data != null) {
                        //prepare setup data
                        Uri uri = data.getData();
                        int transactionType = getTransactionType();
                        String transactionCategory = getTransactionCategory();
                        DateRange dateRange = getDateRange();

                        new ExportSelectedTransactionsCSVTask().execute(new Setup(uri, transactionType, transactionCategory, dateRange));
                    }
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.chooseTransactionsCategoryToBeSaved: {
                String transactionTypeName = chooseTransactionCategoryToBeSaved.getSelectedItem().toString().toLowerCase().replace(" ", "_");
                int idOfSuitableDrawableForTransactionTypeName = getActivity().getResources().getIdentifier(transactionTypeName, "drawable", getActivity().getPackageName());
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
            RadioButton checkedRadiobutton = getView().findViewById(idOfCheckedRadiobutton);
            String chosenTransactionTypeName = checkedRadiobutton.getText().toString();

            ArrayAdapter<String> chooseCategoryAdapter;
            if (chosenTransactionTypeName.equals("Expenses")) {
                chooseCategoryAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_in_export_activity, getResources().getStringArray(R.array.list_of_expenses));
            } else {
                chooseCategoryAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_in_export_activity, getResources().getStringArray(R.array.list_of_incomes));
            }
            chooseTransactionCategoryToBeSaved.setAdapter(chooseCategoryAdapter);
        }
    }

    private class ExportSelectedTransactionsCSVTask extends AsyncTask<Setup, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(getContext());
        private TransactionRepository transactionRepository;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Saving file...");
            this.dialog.show();
            transactionRepository = new TransactionRepository(getContext());
        }

        protected Boolean doInBackground(final Setup... setups) {
            try {
                OutputStream outputStream = getActivity().getContentResolver().openOutputStream(setups[0].uri);

                Cursor curCSV = transactionRepository.rawWithFilter(
                        setups[0].dateRange.dayDateFrom,
                        setups[0].dateRange.monthDateFrom,
                        setups[0].dateRange.yearDateFrom,
                        setups[0].dateRange.dayDateTo,
                        setups[0].dateRange.monthDateTo,
                        setups[0].dateRange.yearDateTo,
                        setups[0].transactionType,
                        setups[0].transactionCategory);

                //write column names
                String[] columnNames = curCSV.getColumnNames();
                String COLUMN_SEPARATOR = ";";
                for (int i = 0; i < columnNames.length; i++) {
                    outputStream.write((columnNames[i] + COLUMN_SEPARATOR).getBytes());
                }

                //write column data
                int lineNumber = 0;
                while (curCSV.moveToNext()) {
                    lineNumber++;
                    String LINE_SEPARATOR = "\n";
                    outputStream.write(LINE_SEPARATOR.getBytes());
                    for (int i = 0; i < curCSV.getColumnNames().length; i++) {
                        //transaction id
                        if (i == 0) {
                            outputStream.write((lineNumber + COLUMN_SEPARATOR).getBytes());
                        }
                        //transaction type, convert from 0/1 to Expense/Income
                        if (i == 1) {
                            int transactionType = Integer.parseInt(curCSV.getString(i));
                            if (transactionType == 1) {
                                outputStream.write(("Expense" + COLUMN_SEPARATOR).getBytes());
                            } else {
                                outputStream.write(("Income" + COLUMN_SEPARATOR).getBytes());
                            }
                        }
                        //transaction name
                        if (i == 2) {
                            outputStream.write((curCSV.getString(i) + COLUMN_SEPARATOR).getBytes());
                        }
                        //transaction amount, convert from int (subunit) to float (currency)
                        if (i == 3) {
                            int transactionAmount = Integer.parseInt(curCSV.getString(i));
                            outputStream.write(((getValueInCurrency(transactionAmount) + COLUMN_SEPARATOR).getBytes()));
                        }
                        //transaction category
                        if (i == 4) {
                            outputStream.write((curCSV.getString(i) + COLUMN_SEPARATOR).getBytes());
                        }
                        //transaction date, convert from long (unix) to dd.MM.yyyy format
                        if (i == 5) {
                            Date dateOfTransaction = new Date(Long.parseLong(curCSV.getString(i)));
                            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                            String dateAsString = formatter.format(dateOfTransaction);
                            outputStream.write((dateAsString + COLUMN_SEPARATOR).getBytes());
                        }
                    }
                }
                outputStream.close();
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
                Toast.makeText(getContext(), "File saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error during saving file", Toast.LENGTH_SHORT).show();
            }
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

    private String getFileName() {

        CheckBox activateDateSelection = getView().findViewById(R.id.activateDateSelection);

        if (activateDateSelection.isChecked()) {
            String dayDateFromAsString = String.valueOf(dayDateFrom);
            String monthDateFromAsString = String.valueOf(monthDateFrom + 1);
            String dayDateToAsString = String.valueOf(dayDateTo);
            String monthDateToAsString = String.valueOf(monthDateTo + 1);
            String yearDateFromAsString = String.valueOf(yearDateFrom);
            String yearDateToAsString = String.valueOf(yearDateTo);
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
            return "MyFinances_" + dayDateFromAsString + "." + monthDateFromAsString + "." + yearDateFromAsString + "-" + dayDateToAsString + "." + monthDateToAsString + "." + yearDateToAsString + ".csv";
        } else
            return "MyFinances.csv";
    }

    private int getTransactionType() {

        CheckBox activateTransactionTypeSelection = getView().findViewById(R.id.activateTransactionTypeSelection);
        int transactionType = 2;

        if (activateTransactionTypeSelection.isChecked()) {
            int idOfCheckedRadiobutton = chooseTransactionTypeToBeSaved.getCheckedRadioButtonId();
            RadioButton checkedRadiobutton = getView().findViewById(idOfCheckedRadiobutton);
            String transactionTypeName = checkedRadiobutton.getText().toString();
            if (transactionTypeName.equals("Expenses")) {
                transactionType = 1;
            } else {
                transactionType = 0;
            }
        }

        return transactionType;
    }

    private String getTransactionCategory() {

        CheckBox activateTransactionCategorySelection = getView().findViewById(R.id.activateTransactionCategorySelection);

        String transactionCategory = "";

        if (activateTransactionCategorySelection.isChecked()) {
            transactionCategory = chooseTransactionCategoryToBeSaved.getSelectedItem().toString();
        }

        return transactionCategory;
    }

    private DateRange getDateRange() {
        CheckBox activateDateSelection = getView().findViewById(R.id.activateDateSelection);

        DateRange dateRange;

        if (activateDateSelection.isChecked()) {
            dateRange = new DateRange(dayDateFrom, monthDateFrom, yearDateFrom, dayDateTo, monthDateTo, yearDateTo);
        }
        else {
            dateRange = new DateRange(1, 1, 1900, 31, 12, 2100);
        }

        return dateRange;
    }

    private static class Setup {
        private final Uri uri;
        private final int transactionType;
        private final String transactionCategory;
        private final DateRange dateRange;

        public Setup(Uri uri, int transactionType, String transactionCategory, DateRange dateRange) {
            this.uri = uri;
            this.transactionType = transactionType;
            this.transactionCategory = transactionCategory;
            this.dateRange = dateRange;
        }
    }

    private static class DateRange {
        private final int dayDateFrom;
        private final int monthDateFrom;
        private final int yearDateFrom;
        private final int dayDateTo;
        private final int monthDateTo;
        private final int yearDateTo;

        private DateRange(int dayDateFrom, int monthDateFrom, int yearDateFrom, int dayDateTo, int monthDateTo, int yearDateTo) {
            this.dayDateFrom = dayDateFrom;
            this.monthDateFrom = monthDateFrom;
            this.yearDateFrom = yearDateFrom;
            this.dayDateTo = dayDateTo;
            this.monthDateTo = monthDateTo;
            this.yearDateTo = yearDateTo;
        }
    }
}
