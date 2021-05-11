package com.example.myexpenses;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.myexpenses.repository.TransactionRepository;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class ExportActivity extends AppCompatActivity {

    private Button testButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.set_limits));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(v -> {

            //first check if we have permission to save into the download directory
            if (!checkIfPermissionIsGranted()) {
                Toast.makeText(getApplicationContext(), "Grant permission first", Toast.LENGTH_SHORT).show();
                return;
            }

            //if we have permission then create .csv file
            new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        });
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

    private String getFileName() {
        Calendar calendar = Calendar.getInstance();
        int actualDay = calendar.get(Calendar.DAY_OF_MONTH);
        int actualMonth = calendar.get(Calendar.MONTH) + 1; //month index start at 0
        int actualYear = calendar.get(Calendar.YEAR);

        String yyyy = "" + actualYear;
        String MM = "" + actualMonth;
        String dd = "" + actualDay;

        if (actualMonth < 10) {
            MM = "0" + actualMonth;
        }
        if (actualDay < 10) {
            dd = "0" + actualDay;
        }
        //TODO file name should contains date from and date to
        return yyyy + "-" + MM + "-" + dd + ".csv";
    }

    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(ExportActivity.this);
        TransactionRepository transactionRepository;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
            transactionRepository = new TransactionRepository(ExportActivity.this);
        }

        protected Boolean doInBackground(final String... args) {

            File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, getFileName());
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file), ';', '\0', '\0', "\n");
                Cursor curCSV = transactionRepository.raw();
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
                Toast.makeText(ExportActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ExportActivity.this, "Export failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}