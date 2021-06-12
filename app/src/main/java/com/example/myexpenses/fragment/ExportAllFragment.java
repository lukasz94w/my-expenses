package com.example.myexpenses.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.myexpenses.R;
import com.example.myexpenses.other.CurrencyConverter;
import com.example.myexpenses.repository.TransactionRepository;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class ExportAllFragment extends Fragment implements View.OnClickListener {

    private final int REQUEST_CODE_FOR_PERMISSION_ANDROID_BELOW_11_VERSION = 10;
    private final int REQUEST_CODE_FOR_PERMISSION_ANDROID_11_VERSION = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export_all, container, false);

        Button saveToFile = view.findViewById(R.id.saveToFile);
        saveToFile.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveToFile: {

                if (checkPermission()) {
                    new ExportAllTransactionsToCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    requestPermission();
                }
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
                    new ExportAllTransactionsToCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    new ExportAllTransactionsToCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Toast.makeText(getContext(), "Allow permission for storage access", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class ExportAllTransactionsToCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(getContext());
        private TransactionRepository transactionRepository;
        private String fileName;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Saving file...");
            this.dialog.show();
            transactionRepository = new TransactionRepository(getContext());
            fileName = "MyFinances.csv";
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
                Cursor curCSV = transactionRepository.raw();

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
                        //amount, convert from int to float and divide by 100
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
}