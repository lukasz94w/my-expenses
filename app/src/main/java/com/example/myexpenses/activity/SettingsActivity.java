package com.example.myexpenses.activity;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import com.example.myexpenses.R;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;
import com.example.myexpenses.other.TransactionsProvider;

import java.util.List;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private CheckBox activatePresentingIncomes;
    private CheckBox activatePieChartAnimation;
    private CheckBox activatePresentingTotalValues;
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
        boolean sharedPrefShouldShowIncomesBarCharts = sharedPreferences.getBoolean("Should show incomes bar charts", true);
        boolean sharedPrefShouldShowPieChartAnimation = sharedPreferences.getBoolean("Should show pie chart animation", true);
        boolean sharedPrefShouldPresentTotalValues = sharedPreferences.getBoolean("Should present total values on pie chart", false);

        activatePresentingIncomes = findViewById(R.id.activatePresentingIncomes);
        activatePresentingIncomes.setChecked(sharedPrefShouldShowIncomesBarCharts);
        activatePresentingIncomes.setOnClickListener(this);

        activatePieChartAnimation = findViewById(R.id.activatePieChartAnimation);
        activatePieChartAnimation.setChecked(sharedPrefShouldShowPieChartAnimation);
        activatePieChartAnimation.setOnClickListener(this);

        activatePresentingTotalValues = findViewById(R.id.activatePresentingTotalValues);
        activatePresentingTotalValues.setChecked(sharedPrefShouldPresentTotalValues);
        activatePresentingTotalValues.setOnClickListener(this);

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
            case R.id.activatePieChartAnimation: {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                boolean checkboxState = activatePieChartAnimation.isChecked();
                editor.putBoolean("Should show pie chart animation", checkboxState);
                editor.apply();
                break;
            }
            case R.id.activatePresentingTotalValues: {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                boolean checkboxState = activatePresentingTotalValues.isChecked();
                editor.putBoolean("Should present total values on pie chart", checkboxState);
                editor.apply();
                break;
            }
            case R.id.generateRandomData: {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setCancelable(false);
                builder.setMessage(R.string.random_data_message);
                builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> {
                    new CreateTestTransactionsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    new DeleteAllTransactionsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    public class DeleteAllTransactionsTask extends AsyncTask<String, Void, Boolean> {

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

    public class CreateTestTransactionsTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);
        private TransactionRepository transactionRepository;
        private List<Transaction> transactionList;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Creating in progress...");
            this.dialog.show();
            transactionList = TransactionsProvider.getListOfTestTransactions();
            transactionRepository = new TransactionRepository(SettingsActivity.this);
        }

        protected Boolean doInBackground(final String... args) {
            try {
                for (Transaction transaction : transactionList) {
                    transactionRepository.create(transaction);
                }
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
    }

    //prevent left checked icon on navigation drawer and also clear filters f.e.
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }
}