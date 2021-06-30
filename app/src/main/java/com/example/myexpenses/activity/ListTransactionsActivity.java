package com.example.myexpenses.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.myexpenses.R;
import com.example.myexpenses.dialogFragment.AddNewTransactionDialog;
import com.example.myexpenses.dialogFragment.ModifyTransactionDialog;
import com.example.myexpenses.fragment.ListTransactionFragment;
import com.example.myexpenses.other.CurrentMonthData;
import com.example.myexpenses.other.ModifyTransactionDialogCallbackData;
import com.example.myexpenses.other.SearchFilter;
import com.example.myexpenses.viewPager.CustomViewPager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.HashMap;

public class ListTransactionsActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener, ModifyTransactionDialog.ModifyTransactionDialogCallback, ListTransactionFragment.ListTransactionsFragmentCallback, AdapterView.OnItemSelectedListener, AddNewTransactionDialog.AddNewTransactionDialogCallback {

    private static final int INITIAL_PAGE = 120;
    private static final int NUM_PAGES = INITIAL_PAGE * 2; //10 years (120 months) before and after current month

    private CustomViewPager viewPager;
    private TransactionsPagerAdapter transactionsPagerAdapter;
    //search criteria
    private int transactionsType;
    private String transactionsName;
    private Spinner transactionsCategorySpinner;
    private String transactionsCategory;
    private ImageView sortTransactions;
    private int transactionsOrder;
    private SearchFilter searchFilter;
    private TextView currentChosenMonthAndYear;
    private TextView monthlyTransactionSum;
    private RelativeLayout filterBar;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    private SearchView searchView;

    private int actualDay;
    private int actualMonth;
    private int actualYear;
    private int currentChosenMonth;
    private int currentChosenYear;

    private int dailyLimit;
    private int monthlyLimit;

    private FloatingActionButton FABaddNewTransaction;

    private SharedPreferences sharedPreferences;

    private ListTransactionFragment currentSeenListTransactionFragment;

    private Menu menu;

    private final int LIMITS_ACTIVITY_REQUEST_CODE = 1;
    private final int SETTINGS_ACTIVITY_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transactions);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        filterBar = findViewById(R.id.filterBar);
        filterBar.setVisibility(View.GONE);
        FABaddNewTransaction = findViewById(R.id.FABaddNewTransaction);
        FABaddNewTransaction.setOnClickListener(this);
        //navigation drawer
        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        RelativeLayout viewContainer = headerView.findViewById(R.id.view_container);
        viewContainer.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        actualDay = calendar.get(Calendar.DAY_OF_MONTH);
        actualMonth = calendar.get(Calendar.MONTH);
        actualYear = calendar.get(Calendar.YEAR);
        currentChosenMonth = actualMonth;
        currentChosenYear = actualYear;

        sharedPreferences = this.getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        //default values if they not have been initialized yet
        dailyLimit = sharedPreferences.getInt("dailyLimit", 1000 * 100);
        monthlyLimit = sharedPreferences.getInt("monthlyLimit", 5000 * 100);
        //initialize search filters
        transactionsType = sharedPreferences.getInt("transactionsType", 2);
        transactionsName = "";
        transactionsCategorySpinner = findViewById(R.id.transactionCategory);
        transactionsCategorySpinner.setOnItemSelectedListener(this);
        transactionsCategory = (String) transactionsCategorySpinner.getSelectedItem();
        sortTransactions = findViewById(R.id.sortTransactions);
        sortTransactions.setOnClickListener(this);
        transactionsOrder = 0;
        searchFilter = new SearchFilter(transactionsType, transactionsName, transactionsCategory, transactionsOrder);

        //navigation bar
        ImageButton previousMonth = findViewById(R.id.previousMonth);
        previousMonth.setOnClickListener(this);
        currentChosenMonthAndYear = findViewById(R.id.currentChosenMonthAndYear);
        ImageButton nextMonth = findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);
        monthlyTransactionSum = findViewById(R.id.monthlyTransactionSum);

        currentSeenListTransactionFragment = new ListTransactionFragment();

        viewPager = findViewById(R.id.viewPager);
        transactionsPagerAdapter = new TransactionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(transactionsPagerAdapter);
        viewPager.setCurrentItem(INITIAL_PAGE, false); //120 - half betweend 0 and 240, false - no sliding animation

        //https://stackoverflow.com/questions/22997205/viewpager-fragments-are-not-initiated-in-oncreate
        //https://stackoverflow.com/questions/16074058/onpageselected-doesnt-work-for-first-page/16074152
        //https://stackoverflow.com/questions/11794269/onpageselected-isnt-triggered-when-calling-setcurrentitem0/20292064#20292064
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                currentSeenListTransactionFragment = transactionsPagerAdapter.getFragment(INITIAL_PAGE);
                CurrentMonthData currentMonthData = currentSeenListTransactionFragment.getDataFromListTransactionsFragment();
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
                currentSeenListTransactionFragment = transactionsPagerAdapter.getFragment(position);
                CurrentMonthData currentMonthData = currentSeenListTransactionFragment.getDataFromListTransactionsFragment();
                currentChosenMonth = currentMonthData.getCurrentChosenMonth();
                currentChosenYear = currentMonthData.getCurrentChosenYear();
                currentChosenMonthAndYear.setText(convertMonthToString(currentChosenMonth + 1, currentChosenYear));
                monthlyTransactionSum.setText(currentMonthData.getFormattedTotalSum());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        setUpNavigationView();
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
            case R.id.FABaddNewTransaction: {

                FragmentManager fragmentManager = getSupportFragmentManager();
                AddNewTransactionDialog addNewTransactionDialog = new AddNewTransactionDialog();

                //passing data to dialog
                Bundle data = new Bundle();
                data.putInt("actualDay", actualDay);
                data.putInt("actualMonth", actualMonth);
                data.putInt("actualYear", actualYear);
                data.putInt("currentChosenMonth", currentChosenMonth);
                data.putInt("currentChosenYear", currentChosenYear);
                data.putInt("dailyLimit", dailyLimit);
                data.putInt("monthlyLimit", monthlyLimit);
                addNewTransactionDialog.setArguments(data);

                addNewTransactionDialog.show(fragmentManager, "add new transaction fragment");
                break;
            }
            case R.id.sortTransactions: {
                PopupMenu popupMenu = new PopupMenu(this, sortTransactions);
                popupMenu.setGravity(Gravity.END);
                popupMenu.inflate(R.menu.popup_menu);

                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.dateDesc:
                            transactionsOrder = 0;
                            searchFilter.setTransactionsOrder(transactionsOrder);
                            transactionsPagerAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.dateAsc:
                            transactionsOrder = 1;
                            searchFilter.setTransactionsOrder(transactionsOrder);
                            transactionsPagerAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.valueDesc:
                            transactionsOrder = 2;
                            searchFilter.setTransactionsOrder(transactionsOrder);
                            transactionsPagerAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.valueAsc:
                            transactionsOrder = 3;
                            searchFilter.setTransactionsOrder(transactionsOrder);
                            transactionsPagerAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.categoryDesc:
                            transactionsOrder = 4;
                            searchFilter.setTransactionsOrder(transactionsOrder);
                            transactionsPagerAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.categoryAsc:
                            transactionsOrder = 5;
                            searchFilter.setTransactionsOrder(transactionsOrder);
                            transactionsPagerAdapter.notifyDataSetChanged();
                            return true;
                        default:
                            return false;
                    }
                });
                popupMenu.show();
                break;
            }
            case R.id.view_container: {
                drawer.closeDrawers();
                break;
            }
            default:
                break;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_transactions_menu, menu);

        this.menu = menu;

        MenuItem menuActionSearch = menu.findItem(R.id.menuActionSearch);
        searchView = (SearchView) menuActionSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search");

        MenuItem menuTypeOfView = menu.findItem(R.id.menuTypeOfView);
        ArrayAdapter<String> filterCategoryAdapter;
        switch (transactionsType) {
            case 0:
                menuTypeOfView.setIcon(R.drawable.menu_show_incomes);
                filterCategoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_incomes));
                transactionsCategorySpinner.setAdapter(filterCategoryAdapter);
                break;
            case 1:
                menuTypeOfView.setIcon(R.drawable.menu_show_expenses);
                filterCategoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_expenses));
                transactionsCategorySpinner.setAdapter(filterCategoryAdapter);
                break;
            case 2:
                menuTypeOfView.setIcon(R.drawable.menu_show_transactions);
                filterCategoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_transactions));
                transactionsCategorySpinner.setAdapter(filterCategoryAdapter);
                break;
            default:
                break;
        }

        MenuItem menuFilter = menu.findItem(R.id.menuFilter);
        menuFilter.setIcon(R.drawable.menu_show_filter);

        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuTypeOfView: {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                ArrayAdapter<String> filterCategoryAdapter;
                transactionsType++;
                if (transactionsType > 2) {
                    transactionsType = 0;
                }
                editor.putInt("transactionsType", transactionsType);
                editor.apply();

                switch (transactionsType) {
                    case 0:
                        item.setIcon(R.drawable.menu_show_incomes);
                        filterCategoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_incomes));
                        transactionsCategorySpinner.setAdapter(filterCategoryAdapter);
                        break;
                    case 1:
                        item.setIcon(R.drawable.menu_show_expenses);
                        filterCategoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_expenses));
                        transactionsCategorySpinner.setAdapter(filterCategoryAdapter);
                        break;
                    case 2:
                        item.setIcon(R.drawable.menu_show_transactions);
                        filterCategoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_transactions));
                        transactionsCategorySpinner.setAdapter(filterCategoryAdapter);
                        break;
                    default:
                        break;
                }
                searchFilter.setTransactionsType(transactionsType);
                transactionsPagerAdapter.notifyDataSetChanged();
                monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
                return true;
            }

            case R.id.menuFilter: {
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    item.setIcon(R.drawable.menu_hide_filter);
                    filterBar.setVisibility(View.VISIBLE);
                } else {
                    item.setIcon(R.drawable.menu_show_filter);
                    filterBar.setVisibility(View.GONE);

                    transactionsCategorySpinner.setSelection(0);
                    transactionsCategory = (String) transactionsCategorySpinner.getSelectedItem();
                    searchFilter.setTransactionsCategory(transactionsCategory);
                    transactionsOrder = 0;
                    searchFilter.setTransactionsOrder(transactionsOrder);
                    transactionsPagerAdapter.notifyDataSetChanged();
                    monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
                }
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onQueryTextChange(String writtenText) {
        if (writtenText == null || writtenText.trim().isEmpty()) {
            resetSearch();
            return false;
        }

        transactionsName = writtenText;
        searchFilter.setTransactionsName(transactionsName);
        transactionsPagerAdapter.notifyDataSetChanged();
        monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resetSearch() {
        transactionsName = "";
        searchFilter.setTransactionsName(transactionsName);
        transactionsPagerAdapter.notifyDataSetChanged();
        monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.transactionCategory: {
                transactionsCategory = (String) transactionsCategorySpinner.getSelectedItem();
                searchFilter.setTransactionsCategory(transactionsCategory);
                transactionsPagerAdapter.notifyDataSetChanged();
                monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LIMITS_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                boolean areNewLimitsSet = data.getExtras().getBoolean("areNewLimitsSet");

                if (areNewLimitsSet) {
                    dailyLimit = data.getExtras().getInt("dailyLimit");
                    monthlyLimit = data.getExtras().getInt("monthlyLimit");
                    transactionsPagerAdapter.notifyDataSetChanged();
                    monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
                }
            }
        } else if (requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                boolean shouldUpdateListOfTransactions = data.getExtras().getBoolean("shouldUpdateListOfTransactions");

                if (shouldUpdateListOfTransactions) {
                    transactionsPagerAdapter.notifyDataSetChanged();
                    monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
                }
            }
        }
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.list_transactions:
                    break;
                case R.id.charts:
                    startActivity(new Intent(ListTransactionsActivity.this, ChartsActivity.class));
                    break;
                case R.id.limits:
                    startActivityForResult(new Intent(ListTransactionsActivity.this, LimitsActivity.class), LIMITS_ACTIVITY_REQUEST_CODE);
                    return true;
                case R.id.export:
                    startActivity(new Intent(ListTransactionsActivity.this, ExportActivity.class));
                    return true;
                case R.id.settings:
                    startActivityForResult(new Intent(ListTransactionsActivity.this, SettingsActivity.class), SETTINGS_ACTIVITY_REQUEST_CODE);
                    return true;
                case R.id.about:
                    startActivity(new Intent(ListTransactionsActivity.this, AboutActivity.class));
                    return true;
            }
            menuItem.setChecked(!menuItem.isChecked());
            menuItem.setChecked(true);

            invalidateOptionsMenu();
            return true;
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawer.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        //if filter bar is open
        if (filterBar.getVisibility() == View.VISIBLE) {
            MenuItem item = menu.findItem(R.id.menuFilter);
            item.setChecked(!item.isChecked());
            item.setIcon(R.drawable.menu_show_filter);
            filterBar.setVisibility(View.GONE);

            transactionsCategorySpinner.setSelection(0);
            transactionsCategory = (String) transactionsCategorySpinner.getSelectedItem();
            searchFilter.setTransactionsCategory(transactionsCategory);
            transactionsOrder = 0;
            searchFilter.setTransactionsOrder(transactionsOrder);
            transactionsPagerAdapter.notifyDataSetChanged();
            monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
        }
        //if search window is open
        else if (!searchView.isIconified()) {
            searchView.setIconified(true);

            transactionsName = "";
            searchFilter.setTransactionsName(transactionsName);
            transactionsPagerAdapter.notifyDataSetChanged();
            monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
            //if navigationDrawer is open
        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void addNewTransactionDialogCallback(int monthOffset) {

        int index = viewPager.getCurrentItem() - monthOffset;

        //check if we need to update one of the 3 loaded fragments (-1, 0 or 1)
        if ((monthOffset >= -1 && monthOffset <= 1)) {
            if (monthOffset == 0) {
                //update current seen fragment (0)
                currentSeenListTransactionFragment.updateFragment();
                monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
            } else if (monthOffset == -1 || monthOffset == 1) {
                //update one of the side fragments (-1 or 1)
                ListTransactionFragment sideFragment = transactionsPagerAdapter.getFragment(index - monthOffset);
                sideFragment.updateFragment();
            }
        }

        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Record successfully added", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void modifyTransactionDialogCallback(ModifyTransactionDialogCallbackData modifyTransactionDialogCallbackData) {
        int monthOffset = modifyTransactionDialogCallbackData.getMonthOffset();
        String typeOfOperation = modifyTransactionDialogCallbackData.getTypeOfOperation();

        int index = viewPager.getCurrentItem();
        Snackbar snackbar;

        if (typeOfOperation.equals("TRANSACTION_ADDED")) {
            if ((monthOffset >= -1 && monthOffset <= 1)) {
                if (monthOffset == 0) {
                    //update current seen fragment (0)
                    currentSeenListTransactionFragment.updateFragment();
                    monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
                } else if (monthOffset == -1 || monthOffset == 1) {
                    //update one of the side fragments (-1 or 1)
                    ListTransactionFragment sideFragment = transactionsPagerAdapter.getFragment(index - monthOffset);
                    sideFragment.updateFragment();
                }
            }

            snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Record successfully added", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        if (typeOfOperation.equals("TRANSACTION_DELETED")) {
            //update current seen fragment (0)
            currentSeenListTransactionFragment.updateFragment();
            monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());

            snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Record successfully deleted", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        if (typeOfOperation.equals("TRANSACTION_UPDATED")) {
            //update current seen fragment (0)
            currentSeenListTransactionFragment.updateFragment();
            monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());

            //check if we need to update also one of side fragments
            if (monthOffset == -1 || monthOffset == 1) {
                //update side fragment
                ListTransactionFragment sideFragment = transactionsPagerAdapter.getFragment(index - monthOffset);
                sideFragment.updateFragment();
            }

            snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Record successfully updated", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void listTransactionsFragmentCallback(int stateOfMultiChoiceOperation) {

        //entered multichoice mode
        if (stateOfMultiChoiceOperation == 1) {
            //disable navigation drawer
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            //hide FAB
            FABaddNewTransaction.hide();
            //close filterBar
            if (filterBar.getVisibility() == View.VISIBLE) {
                filterBar.setVisibility(View.GONE);
            }
            //set default menu icon
            MenuItem item = menu.findItem(R.id.menuFilter);
            item.setIcon(R.drawable.menu_show_filter);
            //disable swiping
            viewPager.setSwipingEnabled(false);

            //exit multichoice mode with: 2, 3 - transaction(s) deleted, 4 - no transaction was deleted
        } else if (stateOfMultiChoiceOperation == 2 || stateOfMultiChoiceOperation == 3 || stateOfMultiChoiceOperation == 4) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            FABaddNewTransaction.show();
            viewPager.setSwipingEnabled(true);

            //if transaction was deleted we need to update totalSum of monthly transactions
            if (stateOfMultiChoiceOperation == 2) {
                monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Record successfully deleted", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else if (stateOfMultiChoiceOperation == 3) {
                monthlyTransactionSum.setText(currentSeenListTransactionFragment.getFormattedTotalSum());
                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Records successfully deleted", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    private class TransactionsPagerAdapter extends FragmentStatePagerAdapter {

        //holding there reference to currently existing (3) fragments
        private HashMap<Integer, ListTransactionFragment> pageReferenceMap = new HashMap<>();

        public TransactionsPagerAdapter(FragmentManager fa) {
            super(fa, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            ListTransactionFragment listTransactionFragment = new ListTransactionFragment();
            Bundle data = new Bundle();

            data.putInt("monthOffset", position - INITIAL_PAGE);
            data.putInt("actualDay", actualDay);
            data.putInt("actualMonth", actualMonth);
            data.putInt("actualYear", actualYear);
            data.putInt("dailyLimit", dailyLimit);
            data.putInt("monthlyLimit", monthlyLimit);
            //search criteria
            data.putInt("transactionsType", transactionsType);
            data.putString("transactionsName", transactionsName);
            data.putString("transactionsCategory", transactionsCategory);
            data.putInt("transactionsOrder", transactionsOrder);
            listTransactionFragment.setArguments(data);

            //add fragment to reference map
            pageReferenceMap.put(position, listTransactionFragment);

            return listTransactionFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public int getItemPosition(Object object) {
            if (object instanceof ListTransactionFragment) {
                ((ListTransactionFragment) object).setLimits(dailyLimit, monthlyLimit);
                ((ListTransactionFragment) object).setSearchFilters(searchFilter);
            }
            return super.getItemPosition(object);
        }

        public ListTransactionFragment getFragment(int key) {
            return pageReferenceMap.get(key);
        }

//        @RequiresApi(api = Build.VERSION_CODES.N)
//        public void updateFragment(int position) {
//            ListTransactionsFragment fragment = (ListTransactionsFragment) getSupportFragmentManager().getFragments().get(viewPager.getCurrentItem());
//            fragment.updateFragment();
//        }

        //when fragment is destroyed, we remove it from reference map
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
            pageReferenceMap.remove(position);
        }
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
