package com.example.myexpenses.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.myexpenses.R;
import com.example.myexpenses.dialogFragment.EditLimitsDialog;
import com.example.myexpenses.fragment.LimitsFragment;
import com.example.myexpenses.other.LimitsFragmentData;
import com.example.myexpenses.repository.TransactionRepository;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.HashMap;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class LimitsActivity extends AppCompatActivity implements View.OnClickListener, EditLimitsDialog.EditLimitsDialogCommunicator {

    private RelativeLayout relativeLayout;
    private TextView currentChosenMonthAndYear;
    private TextView dailyLimitSetAmount, dailyLimitLeftAmount;
    private TextView monthlyLimitSetAmount, monthlyLimitLeftAmount;

    private TransactionRepository transactionRepository;
    private SharedPreferences sharedPreferences;
    private int currentChosenMonth;
    private int currentChosenYear;
    private int dailyLimit;
    private int monthlyLimit;
    private int sumOfDailyExpenses;

    private int actualDay;
    private int actualMonth;
    private int actualYear;

    private ViewPager viewPager;

    private static final int INITIAL_PAGE = 120;
    private static final int NUM_PAGES = INITIAL_PAGE * 2; //10 years (120 months) before and after current month

    private LimitsPagerAdapter limitsPagerAdapter;

    private LimitsFragment currentSeenLimitsFragment;

    private int sumOfMonthlyExpenses;

    private boolean areNewLimitsSet = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_limits);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.set_limits));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        transactionRepository = new TransactionRepository(this);
        sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);

        Calendar calendar = Calendar.getInstance();
        actualDay = calendar.get(Calendar.DAY_OF_MONTH);
        actualMonth = calendar.get(Calendar.MONTH);
        actualYear = calendar.get(Calendar.YEAR);
        currentChosenMonth = actualMonth;
        currentChosenYear = actualYear;

        //read it only once
        sumOfDailyExpenses = Math.abs(transactionRepository.getSumOfDailyExpenses(actualDay, currentChosenMonth, currentChosenYear));

        //default values if they not have been initialized yet
        dailyLimit = sharedPreferences.getInt("dailyLimit", 1000 * 100);
        monthlyLimit = sharedPreferences.getInt("monthlyLimit", 5000 * 100);

        relativeLayout = findViewById(R.id.relativeLayoutLimitsActivity);

        ImageButton previousMonth = findViewById(R.id.previousMonth);
        previousMonth.setOnClickListener(this);
        currentChosenMonthAndYear = findViewById(R.id.currentChosenMonthAndYear);
        ImageButton nextMonth = findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);

        dailyLimitSetAmount = findViewById(R.id.dailyLimitSetAmount);
        dailyLimitLeftAmount = findViewById(R.id.dailyLimitLeftAmount);
        monthlyLimitSetAmount = findViewById(R.id.monthlyLimitSetAmount);
        monthlyLimitLeftAmount = findViewById(R.id.monthlyLimitLeftAmount);
        Button setDailyAndMonthlyLimit = findViewById(R.id.setDailyAndMonthlyLimit);
        setDailyAndMonthlyLimit.setOnClickListener(this);

        viewPager = findViewById(R.id.viewPager);
        limitsPagerAdapter = new LimitsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(limitsPagerAdapter);
        viewPager.setCurrentItem(INITIAL_PAGE, false);

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                currentSeenLimitsFragment = limitsPagerAdapter.getFragment(INITIAL_PAGE);
                LimitsFragmentData limitsFragmentData = currentSeenLimitsFragment.getDataFromLimitsFragment();
                currentChosenMonth = limitsFragmentData.getCurrentChosenMonth();
                currentChosenYear = limitsFragmentData.getCurrentChosenYear();
                currentChosenMonthAndYear.setText(convertMonthToString(currentChosenMonth + 1, currentChosenYear));
                sumOfMonthlyExpenses = limitsFragmentData.getSumOfMonthlyExpenses();
                updateView();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //read current chosen month, year and total sum of transactions
                currentSeenLimitsFragment = limitsPagerAdapter.getFragment(position);
                LimitsFragmentData limitsFragmentData = currentSeenLimitsFragment.getDataFromLimitsFragment();
                currentChosenMonth = limitsFragmentData.getCurrentChosenMonth();
                currentChosenYear = limitsFragmentData.getCurrentChosenYear();
                currentChosenMonthAndYear.setText(convertMonthToString(currentChosenMonth + 1, currentChosenYear));
                sumOfMonthlyExpenses = limitsFragmentData.getSumOfMonthlyExpenses();
                updateView();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previousMonth: {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                break;
            }
            case R.id.nextMonth: {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                break;
            }

            case R.id.setDailyAndMonthlyLimit: {

                FragmentManager fragmentManager = getSupportFragmentManager();
                EditLimitsDialog editLimitsDialog = new EditLimitsDialog();
                //passing data to dialog
                Bundle data = new Bundle();
                data.putInt("dailyLimit", dailyLimit);
                data.putInt("monthlyLimit", monthlyLimit);
                editLimitsDialog.setArguments(data);

                editLimitsDialog.show(fragmentManager, "edit limits fragment");
                break;
            }

            default:
                break;
        }
    }

    private class LimitsPagerAdapter extends FragmentStatePagerAdapter {

        //holding there reference to currently existing (3) fragments
        private HashMap<Integer, LimitsFragment> pageReferenceMap = new HashMap<>();

        public LimitsPagerAdapter(FragmentManager fa) {
            super(fa, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            LimitsFragment limitsFragment = new LimitsFragment();
            Bundle data = new Bundle();
            data.putInt("monthOffset", position - INITIAL_PAGE);
            data.putInt("actualMonth", actualMonth);
            data.putInt("actualYear", actualYear);
            data.putInt("monthlyLimit", monthlyLimit);
            limitsFragment.setArguments(data);

            //add fragment to reference map
            pageReferenceMap.put(position, limitsFragment);

            return limitsFragment;
        }

        public LimitsFragment getFragment(int key) {
            return pageReferenceMap.get(key);
        }

        //when fragment is destroyed, we remove it from reference map
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
            pageReferenceMap.remove(position);
        }


        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public int getItemPosition(Object object) {
            if (object instanceof LimitsFragment) {
                ((LimitsFragment) object).update(monthlyLimit);
            }
            return super.getItemPosition(object);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void retrieveDataFromEditLimitsDialog(Bundle data) {
        int dailyLimitFromDialog = data.getInt("dailyLimit");
        int monthlyLimitFromDialog = data.getInt("monthlyLimit");

        if (dailyLimitFromDialog != dailyLimit || monthlyLimitFromDialog != monthlyLimit) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (dailyLimitFromDialog != dailyLimit) {
                dailyLimit = dailyLimitFromDialog;
                editor.putInt("dailyLimit", dailyLimit);
            }
            if (monthlyLimitFromDialog != monthlyLimit) {
                monthlyLimit = monthlyLimitFromDialog;
                editor.putInt("monthlyLimit", monthlyLimit);
            }
            areNewLimitsSet = true;
            editor.apply();
            limitsPagerAdapter.notifyDataSetChanged(); //refresh barcharts
            updateView(); //refresh limits
            Snackbar snackbar = Snackbar.make(relativeLayout, "New limits set", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            Snackbar snackbar = Snackbar.make(relativeLayout, "Limits hasn't changed", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateView() {

        dailyLimitSetAmount.setText(String.format("%.2f", getValueInCurrency(dailyLimit)));
        monthlyLimitSetAmount.setText(String.format("%.2f", getValueInCurrency(monthlyLimit)));

        int limitExceededAmount = dailyLimit - sumOfDailyExpenses;
        if (limitExceededAmount < 0) {
            dailyLimitLeftAmount.setText(String.format("%.2f", getValueInCurrency(limitExceededAmount)));
            dailyLimitLeftAmount.setTextColor(getLimitReachedColor());
        } else {
            dailyLimitLeftAmount.setText(String.format("+%.2f", getValueInCurrency(limitExceededAmount)));
            dailyLimitLeftAmount.setTextColor(getLimitNotReachedColor());
        }

        if (monthlyLimit - sumOfMonthlyExpenses < 0) {
            monthlyLimitLeftAmount.setText(String.format("%.2f", getValueInCurrency(monthlyLimit - sumOfMonthlyExpenses)));
            monthlyLimitLeftAmount.setTextColor(getLimitReachedColor());
        } else {
            monthlyLimitLeftAmount.setText(String.format("+%.2f", getValueInCurrency(monthlyLimit - sumOfMonthlyExpenses)));
            monthlyLimitLeftAmount.setTextColor(getLimitNotReachedColor());
        }
    }

    private int getLimitNotReachedColor() {
        return ContextCompat.getColor(this, R.color.sum_greater_than_zero);
    }

    private int getLimitReachedColor() {
        return ContextCompat.getColor(this, R.color.limit_reached);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        if (areNewLimitsSet) {
            returnIntent.putExtra("areNewLimitsSet", true);
            returnIntent.putExtra("dailyLimit", dailyLimit);
            returnIntent.putExtra("monthlyLimit", monthlyLimit);
        } else {
            returnIntent.putExtra("areNewLimitsSet", false);
        }
        setResult(RESULT_OK, returnIntent);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String convertMonthToString(int month, int year) {
        //months are indexed starting at 0
        String MM = "" + month;
        String yyyy = "" + year;

        if (month < 10) {
            MM = "0" + month;
        }

        return MM + "/" + yyyy;
    }
}