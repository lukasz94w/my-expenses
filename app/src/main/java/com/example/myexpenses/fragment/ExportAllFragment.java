package com.example.myexpenses.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myexpenses.R;
import com.example.myexpenses.repository.TransactionRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class ExportAllFragment extends Fragment implements View.OnClickListener {

    private final int REQUEST_CODE_CREATE_FILE = 100;

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

                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                String fileName = "MyFinances.csv";
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                startActivityForResult(intent, REQUEST_CODE_CREATE_FILE);

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

                    if (data != null) {
                        //prepare setup data
                        Uri uri = data.getData();

                        new ExportAllTransactionsToCSVTask().execute(uri);
                    }
                }
            }
        }
    }

    private class ExportAllTransactionsToCSVTask extends AsyncTask<Uri, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(getContext());
        private TransactionRepository transactionRepository;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Saving file...");
            this.dialog.show();
            transactionRepository = new TransactionRepository(getContext());
        }

        protected Boolean doInBackground(final Uri... uris) {

            try {
                OutputStream outputStream = getActivity().getContentResolver().openOutputStream(uris[0]);

                transactionRepository = new TransactionRepository(getContext());

                Cursor curCSV = transactionRepository.raw();

                //write column names
                String[] columnNames = curCSV.getColumnNames();
                String COLUMN_SEPARATOR = ";";
                for (int i = 0; i < columnNames.length; i++) {
                    outputStream.write((columnNames[i] + COLUMN_SEPARATOR).getBytes());
                }

                //write data
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
}