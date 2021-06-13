package com.example.myexpenses.fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myexpenses.R;
import com.example.myexpenses.repository.TransactionRepository;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class ExportSelectionFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener {

    protected int dayDateFrom, monthDateFrom, yearDateFrom;
    protected int dayDateTo, monthDateTo, yearDateTo;
    private Button chooseTransactionDateFrom, chooseTransactionDateTo;
    private Spinner chooseTransactionCategoryToBeSaved;
    private ImageView transactionCategoryImageToBeSaved;
    private RadioGroup chooseTransactionTypeToBeSaved;

    private final int REQUEST_CODE_FOR_PERMISSION_ANDROID_BELOW_11_VERSION = 10;
    private final int REQUEST_CODE_FOR_PERMISSION_ANDROID_11_VERSION = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize ranges
        Calendar calendar = Calendar.getInstance();
        dayDateFrom = 1;
        monthDateFrom = 0; //months are indexed from 0
        yearDateFrom = calendar.get(Calendar.YEAR);
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

                if (checkPermission()) {
                    new ExportSelectedTransactionsCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    requestPermission();
                }
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

    private boolean checkPermission() {
        //android == 11
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        //android < 11
        else {
            int resultForReadExternalStorage = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
            int resultForWriteExternalStorage = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
            return resultForReadExternalStorage == PackageManager.PERMISSION_GRANTED && resultForWriteExternalStorage == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        //android == 11
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getActivity().getPackageName())));
                startActivityForResult(intent, REQUEST_CODE_FOR_PERMISSION_ANDROID_11_VERSION);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_CODE_FOR_PERMISSION_ANDROID_11_VERSION);
            }
        } else {
            //android < 11
            ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_FOR_PERMISSION_ANDROID_BELOW_11_VERSION);
        }
    }

    //handle permission result on android < 11
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_FOR_PERMISSION_ANDROID_BELOW_11_VERSION) {
            if (grantResults.length > 0) {

                boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    //permission granted
                    new ExportSelectedTransactionsCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Toast.makeText(getContext(), "Allow permission for storage access", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //handle permission result on android == 11
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_PERMISSION_ANDROID_11_VERSION) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    //permission granted
                    new ExportSelectedTransactionsCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Toast.makeText(getContext(), "Allow permission for storage access", Toast.LENGTH_SHORT).show();
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

    private class ExportSelectedTransactionsCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(getContext());
        private TransactionRepository transactionRepository;
        private int dayDateFrom, monthDateFrom, yearDateFrom, dayDateTo, monthDateTo, yearDateTo;
        private int transactionType;
        private String transactionCategory;
        private String fileName;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Saving file...");
            this.dialog.show();
            transactionRepository = new TransactionRepository(getContext());
            CheckBox activateDateSelection = getView().findViewById(R.id.activateDateSelection);
            CheckBox activateTransactionTypeSelection = getView().findViewById(R.id.activateTransactionTypeSelection);
            CheckBox activateTransactionCategorySelection = getView().findViewById(R.id.activateTransactionCategorySelection);

            if (activateDateSelection.isChecked()) {
                this.dayDateFrom = ExportSelectionFragment.this.dayDateFrom;
                this.monthDateFrom = ExportSelectionFragment.this.monthDateFrom;
                this.yearDateFrom = ExportSelectionFragment.this.yearDateFrom;
                this.dayDateTo = ExportSelectionFragment.this.dayDateTo;
                this.monthDateTo = ExportSelectionFragment.this.monthDateTo;
                this.yearDateTo = ExportSelectionFragment.this.yearDateTo;
            }

            transactionType = 2;
            transactionCategory = "";
            fileName = getFileName(activateDateSelection.isChecked(), dayDateFrom, monthDateFrom, yearDateFrom, dayDateTo, monthDateTo, yearDateTo);

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
                Cursor curCSV = transactionRepository.rawWithFilter(
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

                        //type, convert from 0/1 to Expense/Income
                        if (i == 1) {
                            int transactionType = Integer.parseInt(curCSV.getString(i));
                            if (transactionType == 1) {
                                mySecondStringArray[i] = "Expense";
                            } else {
                                mySecondStringArray[i] = "Income";
                            }
                        }
                        //amount, convert from int (subunit) to float (currency)
                        if (i == 3) {
                            int transactionAmount = Integer.parseInt(curCSV.getString(i));
                            mySecondStringArray[i] = String.valueOf(getValueInCurrency(transactionAmount));
                        }
                        //date, convert from long (unix) to dd.MM.yyyy format
                        if (i == 5) {
                            Date dateOfTransaction = new Date(Long.parseLong(curCSV.getString(i)));
                            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                            String dateAsString = formatter.format(dateOfTransaction);
                            mySecondStringArray[i] = dateAsString;

                        } else if (i == 0 || i == 2 || i == 4) {
                            mySecondStringArray[i] = curCSV.getString(i);
                        }
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

}
