package com.example.myexpenses.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.myexpenses.R;
import com.example.myexpenses.fragment.ChartsFragment;
import com.example.myexpenses.other.CurrentMonthData;
import com.example.myexpenses.viewPager.CustomViewPager;

import java.util.Calendar;
import java.util.HashMap;

public class ChartsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int INITIAL_PAGE = 120;
    private static final int NUM_PAGES = INITIAL_PAGE * 2; //10 years (120 months) before and after current month

    private int actualMonth;
    private int actualYear;
    private int currentChosenMonth;
    private int currentChosenYear;

    private TextView currentChosenMonthAndYear;
    private TextView monthlyTransactionSum;

    private boolean sharedPrefShouldShowIncomesBarCharts;
    private boolean sharedPrefShouldShowPieChartAnimation;
    private boolean sharedPrefShouldPresentTotalValues;

    private CustomViewPager viewPager;
    private ChartsPagerAdapter chartsPagerAdapter;
    private ChartsFragment currentSeenChartsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.graphic_summary);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Calendar calendar = Calendar.getInstance();
        actualMonth = calendar.get(Calendar.MONTH);
        actualYear = calendar.get(Calendar.YEAR);
        currentChosenMonth = actualMonth;
        currentChosenYear = actualYear;

        //navigation bar
        ImageButton previousMonth = findViewById(R.id.previousMonth);
        previousMonth.setOnClickListener(this);
        currentChosenMonthAndYear = findViewById(R.id.currentChosenMonthAndYear);
        ImageButton nextMonth = findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);
        monthlyTransactionSum = findViewById(R.id.monthlyTransactionSum);

        SharedPreferences sharedPreferences = this.getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        //default value if it not have been initialized yet
        sharedPrefShouldShowIncomesBarCharts = sharedPreferences.getBoolean("sharedPrefShouldShowIncomesBarCharts", true);
        sharedPrefShouldShowPieChartAnimation = sharedPreferences.getBoolean("sharedPrefShouldShowPieChartAnimation", true);
        sharedPrefShouldPresentTotalValues = sharedPreferences.getBoolean("sharedPrefShouldPresentTotalValues", false);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setSwipingEnabled(false);
        chartsPagerAdapter = new ChartsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(chartsPagerAdapter);
        viewPager.setCurrentItem(INITIAL_PAGE, false); //120 - half between 0 and 240, false - no sliding animation

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                currentSeenChartsFragment = chartsPagerAdapter.getFragment(INITIAL_PAGE);
                currentSeenChartsFragment.animateChart();
                CurrentMonthData currentMonthData = currentSeenChartsFragment.getDataFromChartsFragment();
                currentChosenMonth = currentMonthData.getCurrentChosenMonth();
                currentChosenYear = currentMonthData.getCurrentChosenYear();
                currentChosenMonthAndYear.setText(convertMonthToString(currentChosenMonth + 1, currentChosenYear));
                monthlyTransactionSum.setText(currentMonthData.getFormattedTotalSum());
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //read current chosen month, year and total sum of transactions
                currentSeenChartsFragment = chartsPagerAdapter.getFragment(position);
                CurrentMonthData currentMonthData = currentSeenChartsFragment.getDataFromChartsFragment();
                currentChosenMonth = currentMonthData.getCurrentChosenMonth();
                currentChosenYear = currentMonthData.getCurrentChosenYear();
                currentChosenMonthAndYear.setText(convertMonthToString(currentChosenMonth + 1, currentChosenYear));
                monthlyTransactionSum.setText(currentMonthData.getFormattedTotalSum());
                currentSeenChartsFragment.animateChart();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

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
            default:
                break;
        }
    }

    private class ChartsPagerAdapter extends FragmentStatePagerAdapter {

        //holding there reference to currently existing (3) fragments
        private HashMap<Integer, ChartsFragment> pageReferenceMap = new HashMap<>();

        public ChartsPagerAdapter(FragmentManager fa) {
            super(fa, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            ChartsFragment chartsFragment = new ChartsFragment();
            Bundle data = new Bundle();

            data.putInt("monthOffset", position - INITIAL_PAGE);
            data.putInt("actualMonth", actualMonth);
            data.putInt("actualYear", actualYear);
            data.putBoolean("sharedPrefShouldShowIncomesBarCharts", sharedPrefShouldShowIncomesBarCharts);
            data.putBoolean("sharedPrefShouldShowPieChartAnimation", sharedPrefShouldShowPieChartAnimation);
            data.putBoolean("sharedPrefShouldPresentTotalValues", sharedPrefShouldPresentTotalValues);
            chartsFragment.setArguments(data);

            //add fragment to reference map
            pageReferenceMap.put(position, chartsFragment);

            return chartsFragment;
        }

        public ChartsFragment getFragment(int key) {
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
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