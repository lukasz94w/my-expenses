package com.example.myexpenses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private CheckBox activatePresentingIncomes;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = this.getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        //default value if it not have been initialized yet
        boolean shouldShowIncomesBarCharts = sharedPreferences.getBoolean("Should show incomes bar charts", true);

        activatePresentingIncomes = findViewById(R.id.activatePresentingIncomes);
        activatePresentingIncomes.setChecked(shouldShowIncomesBarCharts);
        activatePresentingIncomes.setOnClickListener(this);

        Button generateRandomData = findViewById(R.id.generateRandomData);
        generateRandomData.setOnClickListener(this);

        Button deleteAllData = findViewById(R.id.deleteAllData);
        deleteAllData.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activatePresentingIncomes: {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                boolean checkboxState = activatePresentingIncomes.isChecked();
                editor.putBoolean("Should show incomes bar charts", checkboxState);
                editor.apply();
                break;
            }
            case R.id.generateRandomData: {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setCancelable(false);
                builder.setMessage(R.string.random_data_message);
                builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> {
                    new CreateRandomRecordsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                });
                builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
            }
            case R.id.deleteAllData: {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setCancelable(false);
                builder.setMessage(R.string.delete_all_data_warning);
                builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> {
                    new TruncateDatabaseTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                });
                builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
            }
            default:
                break;
        }
    }

    public class TruncateDatabaseTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);
        private TransactionRepository transactionRepository;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Deleting transactions...");
            this.dialog.show();
            transactionRepository = new TransactionRepository(SettingsActivity.this);
        }

        protected Boolean doInBackground(final String... args) {

            try {
                transactionRepository.deleteAllTransactions();
                return true;
            } catch (SQLException e) {
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                Toast.makeText(SettingsActivity.this, "Data successfully removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Error during deleting", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public class CreateRandomRecordsTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);
        private TransactionRepository transactionRepository;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Creating in progress...");
            this.dialog.show();
            transactionRepository = new TransactionRepository(SettingsActivity.this);
        }

        protected Boolean doInBackground(final String... args) {
            try {
                createTestRecords();
                return true;
            } catch (SQLException e) {
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                Toast.makeText(SettingsActivity.this, "Random transactions created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Error during creation", Toast.LENGTH_SHORT).show();
            }

        }

        public void createTestRecords() {
            //INCOMES
            transactionRepository.create(new Transaction(0, "Wypłata za październik", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(0, "Wypłata za listopad", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
            transactionRepository.create(new Transaction(0, "Wypłata za grudzień", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(0, "Premia na święta", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(0, "Wypłata za styczeń", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(0, "Wypłata za luty", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".02.2020")));
            transactionRepository.create(new Transaction(0, "Wypłata za marzec", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(0, "Wypłata za kwiecień", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(0, "Wypłata za maj", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));

            //OCTOBER
            transactionRepository.create(new Transaction(1, "Pizza", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "Trankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "CD Action", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "Przegląd roweru", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "Karta graficzna", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));

            //NOVEMBER
            transactionRepository.create(new Transaction(1, "Wycieczka Wawka", (float) new Random().nextInt(250) - 250, "Hobby", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
            transactionRepository.create(new Transaction(1, "Fifa 2021", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
            transactionRepository.create(new Transaction(1, "CD Action", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
            transactionRepository.create(new Transaction(1, "Delegacja na Słowację", (float) new Random().nextInt(250) - 250, "Hobby", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));

            //DECEMBER
            transactionRepository.create(new Transaction(1, "Prezent na Boże Narodziny mama", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Prezent na Boże Narodziny ojciec", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));

            transactionRepository.create(new Transaction(1, "Mikołajki PLUM", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Wymiana lusterka YARIS", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));

            //JANUARY
            transactionRepository.create(new Transaction(1, "Sylwester", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Happy Meal w macu", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Pizza", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));

            //FEBRUARY
            transactionRepository.create(new Transaction(1, "Spodnie do biegania", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Koszula galowa", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Pizza", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));

            //MARCH
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567999999999", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567890", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Buty do biegania", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Opaska Xiaomi", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "DOOM", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));

            //APRIL
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567999999999", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567890", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Buty do biegania", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Opaska Xiaomi", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "DOOM", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));

            //MAY
            transactionRepository.create(new Transaction(1, "Impreza urodzinowa prezent", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567890", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Pizza", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Imprezka", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Buty do biegania", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Opaska Xiaomi", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "DOOM", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Jedzenie", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
            transactionRepository.create(new Transaction(1, "Prezent na komunię", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        }

        public Date returnDate(String date) {
            final String userInput = date;
            final String[] timeParts = userInput.split("\\.");
            Calendar cal = Calendar.getInstance();

            //zero
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);

            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeParts[0]));
            cal.set(Calendar.MONTH, Integer.parseInt(timeParts[1]) - 1);
            cal.set(Calendar.YEAR, Integer.parseInt(timeParts[2]));
            return cal.getTime();
        }
    }

    //prevent left checked icon on navigation drawer and also clear filters f.e.
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }
}