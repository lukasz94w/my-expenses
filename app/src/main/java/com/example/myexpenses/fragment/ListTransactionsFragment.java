package com.example.myexpenses.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.ListFragment;

import com.example.myexpenses.R;
import com.example.myexpenses.customAdapter.ItemAdapter;
import com.example.myexpenses.model.Header;
import com.example.myexpenses.model.Item;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class ListTransactionsFragment extends ListFragment implements AdapterView.OnItemClickListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemLongClickListener, SearchView.OnQueryTextListener, View.OnTouchListener {

    //onCreate
    private TransactionRepository transactionRepository;
    private SharedPreferences sharedPreferences;
    private RelativeLayout filterBar;
    private ImageView sortTransactions;
    private RelativeLayout navigationBar;
    private TextView currentChosenMonthAndYear;
    private TextView monthlyTransactionSum;
    private ItemAdapter itemAdapter;
    private int actualDay;
    private int actualMonth;
    private int actualYear;
    private Date actualDate;
    private Float dailyLimit;
    private Float monthlyLimit;

    //search criteria
    private Integer currentChosenMonth;
    private Integer currentChosenYear;
    private int transactionsType;
    private String transactionsName;
    private Spinner transactionsCategory;
    private String transactionsOrder;

    //popupWindow
    private View popupView;
    private PopupWindow popupWindow;
    private Button chooseTransactionDate;
    private Button chooseTodayTomorrow;
    private RadioGroup chooseTransactionType;
    private RadioButton chosenTransactionExpense;
    private RadioButton chosenTransactionIncome;
    private ImageView transactionCategoryImage;
    private Spinner chooseTransactionCategory;
    private EditText chooseTransactionAmount;
    private EditText chooseTransactionName;
    private Button saveNewTransactionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionRepository = new TransactionRepository(getContext());
        setHasOptionsMenu(true);

        Calendar calendar = Calendar.getInstance();
        actualDay = calendar.get(Calendar.DAY_OF_MONTH);
        actualMonth = calendar.get(Calendar.MONTH); //month index start at 0
        actualYear = calendar.get(Calendar.YEAR);
        currentChosenMonth = actualMonth;
        currentChosenYear = actualYear;
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(currentChosenYear, currentChosenMonth, actualDay, 0, 0, 0);
        actualDate = new Date();
        actualDate.setTime(calendar.getTime().getTime());

        transactionsName = "";
        transactionsOrder = "";

        sharedPreferences = this.getActivity().getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        //default values if they not have been initialized yet
        dailyLimit = sharedPreferences.getFloat("Daily limit", 1000);
        monthlyLimit = sharedPreferences.getFloat("Monthly limit", 5000);
        transactionsType = sharedPreferences.getInt("Type of view", 2);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //onCreateView
        View view = inflater.inflate(R.layout.fragment_list_transactions, container, false);

        navigationBar = view.findViewById(R.id.navigationBar);

        ImageButton previousMonth = view.findViewById(R.id.previousMonth);
        previousMonth.setOnClickListener(this);

        currentChosenMonthAndYear = view.findViewById(R.id.currentChosenMonthAndYear);

        ImageButton nextMonth = view.findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);

        monthlyTransactionSum = view.findViewById(R.id.monthlyTransactionSum);

        filterBar = view.findViewById(R.id.filterBar);
        filterBar.setVisibility(View.GONE);

        transactionsCategory = view.findViewById(R.id.filterTransactionCategory);
        transactionsCategory.setOnItemSelectedListener(this);

        sortTransactions = view.findViewById(R.id.sortTransactions);
        sortTransactions.setOnClickListener(this);

        itemAdapter = new ItemAdapter(getActivity(), new ArrayList());
        setListAdapter(itemAdapter);

        updateView();

        FloatingActionButton addNewTransaction = view.findViewById(R.id.addNewTransaction);
        addNewTransaction.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_transactions_menu, menu);
        MenuItem menuActionSearch = menu.findItem(R.id.menuActionSearch);
        SearchView searchView = (SearchView) menuActionSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search");

        MenuItem menuTypeOfView = menu.findItem(R.id.menuTypeOfView);
        ArrayAdapter<String> filterCategoryAdapter;
        switch (transactionsType) {
            case 0:
                menuTypeOfView.setIcon(R.drawable.menu_show_incomes);
                filterCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_incomes));
                transactionsCategory.setAdapter(filterCategoryAdapter);
                break;
            case 1:
                menuTypeOfView.setIcon(R.drawable.menu_show_expenses);
                filterCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_expenses));
                transactionsCategory.setAdapter(filterCategoryAdapter);
                break;
            case 2:
                menuTypeOfView.setIcon(R.drawable.menu_show_transactions);
                filterCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_transactions));
                transactionsCategory.setAdapter(filterCategoryAdapter);
                break;
            default:
                break;
        }

        MenuItem menuFilter = menu.findItem(R.id.menuFilter);
        menuFilter.setIcon(R.drawable.menu_show_filter);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuTypeOfView: {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                ArrayAdapter<String> filterCategoryAdapter;
                transactionsType++;
                if (transactionsType > 2) {
                    transactionsType = 0;
                }
                editor.putInt("Type of view", transactionsType);
                editor.apply();

                switch (transactionsType) {
                    case 0:
                        item.setIcon(R.drawable.menu_show_incomes);
                        filterCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_incomes));
                        transactionsCategory.setAdapter(filterCategoryAdapter);
                        break;
                    case 1:
                        item.setIcon(R.drawable.menu_show_expenses);
                        filterCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_expenses));
                        transactionsCategory.setAdapter(filterCategoryAdapter);
                        break;
                    case 2:
                        item.setIcon(R.drawable.menu_show_transactions);
                        filterCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_filter_categories, getResources().getStringArray(R.array.filter_list_of_transactions));
                        transactionsCategory.setAdapter(filterCategoryAdapter);
                        break;
                    default:
                        break;
                }
                updateView();
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
                    transactionsCategory.setSelection(0);
                    transactionsOrder = "";
                    updateView();
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previousMonth: {
                if (currentChosenMonth == 0) { //months are indexed starting from zero
                    currentChosenMonth = 11;
                    currentChosenYear--;
                } else {
                    currentChosenMonth--;
                }
                updateView();
                break;
            }

            case R.id.nextMonth: {
                if (currentChosenMonth == 11) { //months are indexed starting from zero
                    currentChosenMonth = 0;
                    currentChosenYear++;
                } else {
                    currentChosenMonth++;
                }
                updateView();
                break;
            }

            case R.id.addNewTransaction: {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(LAYOUT_INFLATER_SERVICE);
                popupView = inflater.inflate(R.layout.popup_new_transaction, null);

                int popupWidth = 565;
                int popupHeight = 767;
                popupWindow = new PopupWindow(popupView, popupWidth, popupHeight, true);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                //if the touch is outside the bounds the touch is consumed and not passed to the popupWindow so it is not dismissed
                popupWindow.setTouchInterceptor((v12, event) -> {
                    if (event.getX() < 0 || event.getX() > popupWidth) return true;
                    if (event.getY() < 0 || event.getY() > popupHeight) return true;

                    return false;
                });

                //dim background when popup shows
                View container = (View) popupWindow.getContentView().getParent();
                WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) container.getLayoutParams();
                layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                layoutParams.dimAmount = 0.6f;
                windowManager.updateViewLayout(container, layoutParams);

                TextView toolbarText = popupView.findViewById(R.id.toolbarText);
                toolbarText.setText("Add record");

                SimpleDateFormat simpledateformat = new SimpleDateFormat("EE");
                String currentDayOfWeek = simpledateformat.format(actualDate);
                String currentDateAsString = convertDateToString(actualDay, currentChosenMonth + 1, currentChosenYear, currentDayOfWeek);

                chooseTransactionDate = popupView.findViewById(R.id.chooseTransactionDate);
                chooseTransactionDate.setText(currentDateAsString);
                chooseTransactionDate.setOnClickListener(this);

                chooseTodayTomorrow = popupView.findViewById(R.id.chooseTodayTomorrow);
                chooseTodayTomorrow.setOnClickListener(this);
                if (currentChosenMonth != actualMonth || currentChosenYear != actualYear) {
                    chooseTodayTomorrow.setText("Today");
                }

                chooseTransactionType = popupView.findViewById(R.id.chooseTransactionType);
                chooseTransactionType.setOnCheckedChangeListener(this);
                chosenTransactionExpense = popupView.findViewById(R.id.chosenTransactionExpense);
                chosenTransactionIncome = popupView.findViewById(R.id.chosenTransactionIncome);

                transactionCategoryImage = popupView.findViewById(R.id.transactionCategoryImage);

                chooseTransactionCategory = popupView.findViewById(R.id.chooseTransactionCategory);
                ArrayAdapter<String> chooseCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_categories, getResources().getStringArray(R.array.list_of_expenses));
                chooseTransactionCategory.setAdapter(chooseCategoryAdapter);
                chooseTransactionCategory.setOnItemSelectedListener(this);

                chooseTransactionAmount = popupView.findViewById(R.id.chooseTransactionAmount);
                chooseTransactionAmount.setOnTouchListener(this);
                chooseTransactionAmount.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                chooseTransactionName = popupView.findViewById(R.id.chooseTransactionName);
                chooseTransactionName.setOnTouchListener(this);

                saveNewTransactionButton = popupView.findViewById(R.id.saveNewTransactionButton);
                saveNewTransactionButton.setOnClickListener(this);

                ImageButton toolbarClose = popupView.findViewById(R.id.toolbarClose);
                toolbarClose.setOnClickListener(v1 -> {
                    if (!chooseTransactionAmount.getText().toString().equals("") || !chooseTransactionName.getText().toString().equals("")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(false);
                        builder.setMessage(R.string.unsaved_data_message);
                        builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> popupWindow.dismiss());
                        builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    } else {
                        popupWindow.dismiss();
                    }
                });
                break;
            }

            case R.id.chooseTransactionDate: {
                int day = actualDay;
                int month = currentChosenMonth;
                int year = currentChosenYear;

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DialogTheme, (view, chosenYear, chosenMonth, chosenDayOfMonth) -> {
                    SimpleDateFormat simpledateformat = new SimpleDateFormat("EE");
                    Date date = new Date(chosenYear, chosenMonth, chosenDayOfMonth - 1);
                    String chosenDayOfWeek = simpledateformat.format(date); //get week name f.e.: Tue
                    String chosenDate = convertDateToString(chosenDayOfMonth, chosenMonth + 1, chosenYear, chosenDayOfWeek); //months are indexed starting by zero
                    chooseTransactionDate.setText(chosenDate);

                    if (day == chosenDayOfMonth && month == chosenMonth && year == chosenYear) {
                        chooseTodayTomorrow.setText("Yesterday");
                    } else {
                        chooseTodayTomorrow.setText("Today");
                    }

                }, year, month, day);

                datePickerDialog.show();
                break;
            }

            case R.id.chooseTodayTomorrow: {
                if (chooseTodayTomorrow.getText().toString().equals("Today")) {
                    SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE");
                    Date date = new Date(actualYear, actualMonth, actualDay - 1);
                    String dayOfWeekToday = simpledateformat.format(date);

                    String todayDate = convertDateToString(actualDay, actualMonth + 1, actualYear, dayOfWeekToday);
                    chooseTodayTomorrow.setText("Yesterday");
                    chooseTransactionDate.setText(todayDate);
                } else {
                    Calendar calendarYesterday = Calendar.getInstance();
                    calendarYesterday.add(Calendar.DATE, -1);
                    int yearYesterday = calendarYesterday.get(Calendar.YEAR);
                    int monthYesterday = calendarYesterday.get(Calendar.MONTH);
                    int dayYesterday = calendarYesterday.get(Calendar.DAY_OF_MONTH);
                    SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE");
                    Date date = new Date(yearYesterday, monthYesterday, dayYesterday - 1);
                    String dayOfWeekYesterday = simpledateformat.format(date);

                    String yesterday = convertDateToString(dayYesterday, monthYesterday + 1, yearYesterday, dayOfWeekYesterday);
                    chooseTodayTomorrow.setText("Today");
                    chooseTransactionDate.setText(yesterday);
                }
                break;
            }

            case R.id.saveNewTransactionButton: {
                Transaction transactionToSave = new Transaction();

                //check if name field is empty, if so we set it as category name
                String transactionName = chooseTransactionName.getText().toString();
                if (transactionName.equals("")) {
                    transactionName = chooseTransactionCategory.getSelectedItem().toString();
                }

                //check if amount is empty if so we set it as zero
                float transactionAmount;
                try {
                    transactionAmount = Float.parseFloat(chooseTransactionAmount.getText().toString());
                } catch (NumberFormatException e) {
                    transactionAmount = 0f;
                }

                //check there typeOfTransaction: 0 - income, 1 expense,
                //also if its expense we need to add minus before amount
                RadioButton checkedRadioButton = popupView.findViewById(chooseTransactionType.getCheckedRadioButtonId());
                String categoryName = checkedRadioButton.getText().toString();
                int transactionType = 0;
                if (categoryName.equals("Expense")) {
                    transactionType = 1;
                    transactionAmount = -transactionAmount;
                }

                String transactionCategory = chooseTransactionCategory.getSelectedItem().toString();
                Date transactionDate = convertStringToDate(chooseTransactionDate.getText().toString());

                transactionToSave.setName(transactionName);
                transactionToSave.setAmount(transactionAmount);
                transactionToSave.setType(transactionType);
                transactionToSave.setCategory(transactionCategory);
                transactionToSave.setDate(transactionDate);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(transactionDate);
                int monthOfNewTransaction = calendar.get(Calendar.MONTH);
                int yearOfNewTransaction = calendar.get(Calendar.YEAR);

                if (transactionType == 1) {
                    boolean isDailyLimitExceeded = false;
                    if (transactionDate.equals(actualDate)) {
                        if (Math.abs(transactionRepository.getSumOfDailyExpenses(actualDay, monthOfNewTransaction, yearOfNewTransaction)) + Math.abs(transactionAmount) > dailyLimit)
                            isDailyLimitExceeded = true;
                    }
                    boolean isMonthlyLimitExceeded = false;
                    if (Math.abs(transactionRepository.getSumOfMonthlyExpenses(monthOfNewTransaction, yearOfNewTransaction)) + Math.abs(transactionAmount) > monthlyLimit) {
                        isMonthlyLimitExceeded = true;
                    }

                    if (isDailyLimitExceeded || isMonthlyLimitExceeded) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(false);
//                        builder.setTitle(R.string.limit_title);
                        if (isDailyLimitExceeded && isMonthlyLimitExceeded) {
                            builder.setMessage(R.string.limit_warning_daily_and_monthly_message);
                        } else if (isDailyLimitExceeded) {
                            builder.setMessage(R.string.limit_warning_daily_message);
                        } else {
                            builder.setMessage(R.string.limit_warning_monthly_message);
                        }
                        builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> {
                            transactionRepository.create(transactionToSave);
                            //check if we need to actualise current seen list of transactions and total sum
                            if ((monthOfNewTransaction == currentChosenMonth) && (yearOfNewTransaction == currentChosenYear)) {
                                updateView();
                            }
                            Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getView()), "Record successfully added", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            popupWindow.dismiss();
                        });

                        builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                            popupWindow.dismiss();
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        break;
                    }
                }

                transactionRepository.create(transactionToSave);
                //check if we need to actualise current seen list of transactions and total sum
                if ((monthOfNewTransaction == currentChosenMonth) && (yearOfNewTransaction == currentChosenYear)) {
                    updateView();
                }
                Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getView()), "Record successfully added", Snackbar.LENGTH_LONG);
                snackbar.show();
                popupWindow.dismiss();
                break;
            }

            case R.id.sortTransactions: {
                PopupMenu popupMenu = new PopupMenu(getContext(), sortTransactions);
                popupMenu.setGravity(Gravity.END);
                popupMenu.inflate(R.menu.popup_menu);

                popupMenu.setOnMenuItemClickListener(item -> {
                    transactionsOrder = (String) item.getTitle();
                    updateView();
                    return true;
                });
                popupMenu.show();
                break;
            }

            default:
                break;
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateView() {
        List<Transaction> foundedMonthTransactions = transactionRepository.findTransactionsInMonthByFilter(new String[]{
                String.valueOf(currentChosenMonth),
                String.valueOf(currentChosenYear),
                String.valueOf(transactionsType),
                transactionsName,
                (String) transactionsCategory.getSelectedItem(),
                transactionsOrder});

        double sumOfCurrentChosenMonthTransactions = foundedMonthTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        double sumOfCurrentChosenMonthExpenses = foundedMonthTransactions.stream()
                .filter(o -> o.getType() == 1)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double sumOfActualDayExpenses = 0;
        if (currentChosenMonth == actualMonth && currentChosenYear == actualYear) {
            sumOfActualDayExpenses = Math.abs(transactionRepository.getSumOfDailyExpenses(actualDay, actualMonth, actualYear));
        }

        List<Item> monthTransactionsListWithAddedHeaders = sortAndAddHeaders(foundedMonthTransactions);
        currentChosenMonthAndYear.setText(convertMonthToString(currentChosenMonth + 1, currentChosenYear)); //months are indexed starting from zero

        Spannable sumOfMonthlyTransactions;
        if (sumOfCurrentChosenMonthTransactions >= 0) {
            sumOfMonthlyTransactions = new SpannableString(String.format("+%.2f", sumOfCurrentChosenMonthTransactions));
            sumOfMonthlyTransactions.setSpan(new ForegroundColorSpan(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.sum_greater_than_zero)), 0, sumOfMonthlyTransactions.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            sumOfMonthlyTransactions = new SpannableString(String.format("%.2f", sumOfCurrentChosenMonthTransactions));
            sumOfMonthlyTransactions.setSpan(new ForegroundColorSpan(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.sum_lesser_than_zero)), 0, sumOfMonthlyTransactions.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        monthlyTransactionSum.setText(sumOfMonthlyTransactions);

        if ((currentChosenMonth == actualMonth && currentChosenYear == actualYear) && sumOfActualDayExpenses > dailyLimit && sumOfCurrentChosenMonthExpenses > monthlyLimit) {
            Spannable limitExceeded = new SpannableString(" (D!M!)");
            limitExceeded.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.limit_reached)), 0, limitExceeded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            monthlyTransactionSum.append(limitExceeded);
        } else if ((currentChosenMonth == actualMonth && currentChosenYear == actualYear) && sumOfActualDayExpenses > dailyLimit) {
            Spannable limitExceeded = new SpannableString(" (D!)");
            limitExceeded.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.limit_reached)), 0, limitExceeded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            monthlyTransactionSum.append(limitExceeded);
        } else if (sumOfCurrentChosenMonthExpenses > monthlyLimit) {
            Spannable limitExceeded = new SpannableString(" (M!)");
            limitExceeded.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.limit_reached)), 0, limitExceeded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            monthlyTransactionSum.append(limitExceeded);
        }

        itemAdapter.clear();
        itemAdapter.addAll(monthTransactionsListWithAddedHeaders);
        itemAdapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.chooseTransactionCategory: {
                String transactionTypeName = chooseTransactionCategory.getSelectedItem().toString().toLowerCase().replace(" ", "_");
                int idOfSuitableDrawableForTransactionTypeName = Objects.requireNonNull(getContext()).getResources().getIdentifier(transactionTypeName, "drawable", getContext().getPackageName());
                transactionCategoryImage.setBackgroundResource(idOfSuitableDrawableForTransactionTypeName);
                break;
            }
            case R.id.filterTransactionCategory: {
                updateView();
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        //get selected transaction from list
        Transaction selectedTransaction = (Transaction) (Objects.requireNonNull(getListAdapter())).getItem(position);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_modify_transaction, null);

        int popupWidth = 565;
        int popupHeight = 858;
        popupWindow = new PopupWindow(popupView, popupWidth, popupHeight, true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        //if the touch is outside the bounds the touch is consumed and not passed to the popupWindow so it is not dismissed
        popupWindow.setTouchInterceptor((v12, event) -> {
            if (event.getX() < 0 || event.getX() > popupWidth) return true;
            if (event.getY() < 0 || event.getY() > popupHeight) return true;

            return false;
        });

        //dim background when popup shows
        View container = (View) popupWindow.getContentView().getParent();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) container.getLayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.6f;
        windowManager.updateViewLayout(container, layoutParams);

        TextView toolbarText = popupView.findViewById(R.id.toolbarText);
        toolbarText.setText("Modify record");

        Date date = selectedTransaction.getDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int transactionDay = calendar.get(Calendar.DAY_OF_MONTH);
        int transactionMonth = calendar.get(Calendar.MONTH);
        int transactionYear = calendar.get(Calendar.YEAR);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("EE");
        String currentDayOfWeek = simpledateformat.format(date);
        String currentDateAsString = convertDateToString(transactionDay, transactionMonth + 1, transactionYear, currentDayOfWeek); //months are indexed starting from zero

        chooseTransactionDate = popupView.findViewById(R.id.chooseTransactionDate);
        chooseTransactionDate.setText(currentDateAsString);
        chooseTransactionDate.setOnClickListener(this);

        chooseTodayTomorrow = popupView.findViewById(R.id.chooseTodayTomorrow);
        chooseTodayTomorrow.setOnClickListener(this);
        chooseTodayTomorrow.setText("Today");

        chooseTransactionCategory = popupView.findViewById(R.id.chooseTransactionCategory);
        chooseTransactionCategory.setOnItemSelectedListener(this);

        chooseTransactionType = popupView.findViewById(R.id.chooseTransactionType);
        chooseTransactionType.setOnCheckedChangeListener(this);
        chosenTransactionExpense = popupView.findViewById(R.id.chosenTransactionExpense);
        chosenTransactionIncome = popupView.findViewById(R.id.chosenTransactionIncome);

        String[] spinnerValues;
        if (selectedTransaction.getType() == 1) { //expense
            chosenTransactionExpense.setChecked(true);
            spinnerValues = getResources().getStringArray(R.array.list_of_expenses);
            chooseTransactionCategory.setSelection(Arrays.asList(spinnerValues).indexOf(selectedTransaction.getCategory())); //automatically the image is selected (onItemSelected is called)
        } else { //income
            chosenTransactionIncome.setChecked(true);
            spinnerValues = getResources().getStringArray(R.array.list_of_incomes);
            chooseTransactionCategory.setSelection(Arrays.asList(spinnerValues).indexOf(selectedTransaction.getCategory())); //automatically the image is selected (onItemSelected is called)
        }

        transactionCategoryImage = popupView.findViewById(R.id.transactionCategoryImage);

        chooseTransactionAmount = popupView.findViewById(R.id.chooseTransactionAmount);
        double primaryValueOfUpdatedTransaction = selectedTransaction.getAmount();
        chooseTransactionAmount.setText(String.format("%.2f", Math.abs(primaryValueOfUpdatedTransaction)));
        chooseTransactionAmount.setOnTouchListener(this);
        chooseTransactionAmount.requestFocus();

        chooseTransactionName = popupView.findViewById(R.id.chooseTransactionName);
        chooseTransactionName.setText(selectedTransaction.getName());
        chooseTransactionName.setOnTouchListener(this);

        Button deleteTransactionButton = popupView.findViewById(R.id.deleteTransactionButton);
        deleteTransactionButton.setOnClickListener(v1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(false);
            builder.setMessage(R.string.delete_transaction_message);
            builder.setPositiveButton(R.string.delete_transaction_yes_button, (dialog, which) -> {
                transactionRepository.deleteTransaction(selectedTransaction.getId());
                updateView();
                Snackbar snackbar = Snackbar.make(getView(), "Record successfully deleted", Snackbar.LENGTH_LONG);
                snackbar.show();
                popupWindow.dismiss();
            });
            builder.setNegativeButton(R.string.delete_transaction_no_button, (dialog, which) -> {
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        Button saveTransactionButton = popupView.findViewById(R.id.saveTransactionButton);
        saveTransactionButton.setOnClickListener(v12 -> {
            Transaction transactionToSave = new Transaction();

            int transactionId = selectedTransaction.getId();

            //check if name field is empty, if so we set it as category name
            String transactionName = chooseTransactionName.getText().toString();
            if (transactionName.equals("")) {
                transactionName = chooseTransactionCategory.getSelectedItem().toString();
            }

            //check if amount is empty if so we set it as zero
            float transactionAmount;
            try {
                transactionAmount = Float.parseFloat(chooseTransactionAmount.getText().toString());
            } catch (NumberFormatException e) {
                transactionAmount = 0f;
            }

            //check there typeOfTransaction: 0 - income, 1 expense,
            //also if its expense we need to add minus before amount
            RadioButton checkedRadioButton = popupView.findViewById(chooseTransactionType.getCheckedRadioButtonId());
            String categoryName = checkedRadioButton.getText().toString();
            int transactionType = 0;
            if (categoryName.equals("Expense")) {
                transactionType = 1;
                transactionAmount = -transactionAmount;
            }

            String transactionCategory = chooseTransactionCategory.getSelectedItem().toString();
            Date transactionDate = convertStringToDate(chooseTransactionDate.getText().toString());

            transactionToSave.setId(transactionId);
            transactionToSave.setName(transactionName);
            transactionToSave.setAmount(transactionAmount);
            transactionToSave.setType(transactionType);
            transactionToSave.setCategory(transactionCategory);
            transactionToSave.setDate(transactionDate);

            calendar.setTime(transactionDate);
            int monthOfUpdatedTransaction = calendar.get(Calendar.MONTH);
            int yearOfUpdatedTransaction = calendar.get(Calendar.YEAR);

            if (transactionType == 1) {
                boolean isDailyLimitExceeded = false;
                if (transactionDate.equals(actualDate)) {
                    if (Math.abs(transactionRepository.getSumOfDailyExpenses(actualDay, monthOfUpdatedTransaction, yearOfUpdatedTransaction)) - Math.abs(primaryValueOfUpdatedTransaction) + Math.abs(transactionAmount) > dailyLimit)
                        isDailyLimitExceeded = true;
                }
                boolean isMonthlyLimitExceeded = false;
                if (Math.abs(transactionRepository.getSumOfMonthlyExpenses(monthOfUpdatedTransaction, yearOfUpdatedTransaction)) - Math.abs(primaryValueOfUpdatedTransaction) + Math.abs(transactionAmount) > monthlyLimit) {
                    isMonthlyLimitExceeded = true;
                }

                if (isDailyLimitExceeded || isMonthlyLimitExceeded) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(false);
//                    builder.setTitle(R.string.limit_title);
                    if (isDailyLimitExceeded && isMonthlyLimitExceeded) {
                        builder.setMessage(R.string.limit_warning_daily_and_monthly_message);
                    } else if (isDailyLimitExceeded) {
                        builder.setMessage(R.string.limit_warning_daily_message);
                    } else {
                        builder.setMessage(R.string.limit_warning_monthly_message);
                    }
                    builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> {
                        transactionRepository.updateTransaction(transactionToSave);
                        updateView();
                        Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getView()), "Record successfully added", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        popupWindow.dismiss();
                    });

                    builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                        popupWindow.dismiss();
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return;
                }
            }

            transactionRepository.updateTransaction(transactionToSave);
            updateView();
            Snackbar snackbar = Snackbar.make(getView(), "Record successfully updated", Snackbar.LENGTH_LONG);
            snackbar.show();
            popupWindow.dismiss();
        });

        saveNewTransactionButton = popupView.findViewById(R.id.saveNewTransactionButton);
        saveNewTransactionButton.setOnClickListener(this);

        ImageButton toolbarClose = popupView.findViewById(R.id.toolbarClose);
        toolbarClose.setOnClickListener(v13 -> {
            int transactionId = selectedTransaction.getId();
            String transactionName = chooseTransactionName.getText().toString();
            RadioButton checkedRadioButton = popupView.findViewById(chooseTransactionType.getCheckedRadioButtonId());
            String categoryName = checkedRadioButton.getText().toString();
            float transactionAmount;
            try {
                transactionAmount = Float.parseFloat(chooseTransactionAmount.getText().toString());
            } catch (NumberFormatException e) {
                transactionAmount = 0f;
            }
            int transactionType = 0;
            if (categoryName.equals("Expense")) {
                transactionType = 1;
                if (!(transactionAmount == 0)) {
                    transactionAmount = -transactionAmount;
                }
            }
            String transactionCategory = chooseTransactionCategory.getSelectedItem().toString();
            Date transactionDate = convertStringToDate(chooseTransactionDate.getText().toString());
            Transaction transactionAtMomentOfClosingPopup = new Transaction(transactionId, transactionType, transactionName, transactionAmount, transactionCategory, transactionDate);

            if (!selectedTransaction.equals(transactionAtMomentOfClosingPopup)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false);
                builder.setMessage(R.string.unsaved_data_message);
                builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> popupWindow.dismiss());
                builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.chooseTransactionType) {
            int idOfCheckedRadiobutton = chooseTransactionType.getCheckedRadioButtonId();
            RadioButton checkedRadiobutton = popupView.findViewById(idOfCheckedRadiobutton);
            checkedRadiobutton.setChecked(true);
            String chosenTransactionTypeName = checkedRadiobutton.getText().toString();

            if (chosenTransactionTypeName.equals("Expense")) {
                chosenTransactionExpense.setChecked(true);
                chosenTransactionIncome.setChecked(false);
                ArrayAdapter<String> chooseCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_categories, getResources().getStringArray(R.array.list_of_expenses));
                chooseTransactionCategory.setAdapter(chooseCategoryAdapter);
            } else {
                chosenTransactionExpense.setChecked(false);
                chosenTransactionIncome.setChecked(true);
                ArrayAdapter<String> chooseCategoryAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_categories, getResources().getStringArray(R.array.list_of_incomes));
                chooseTransactionCategory.setAdapter(chooseCategoryAdapter);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setSelector(R.drawable.level_list_selector);
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deleteTransactions:

                        SparseBooleanArray checkedItemPositions = getListView().getCheckedItemPositions();
                        String[] transactionsIds = new String[checkedItemPositions.size()];
                        for (int i = 0; i < checkedItemPositions.size(); i++) {
                            if (checkedItemPositions.valueAt(i)) {
                                Transaction transaction = (Transaction) getListView().getAdapter().getItem(checkedItemPositions.keyAt(i));
                                int transactionId = transaction.getId();
                                transactionsIds[i] = "" + transactionId + "";
                            }
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(false);
                        if (transactionsIds.length == 1) {
                            builder.setMessage(R.string.delete_transaction_message);
                        } else {
                            builder.setMessage(R.string.delete_transactions_message);
                        }
                        builder.setPositiveButton(R.string.delete_transaction_yes_button, (dialog, which) -> {
                            mode.finish();
                            transactionRepository.deleteTransactions(transactionsIds);
                            updateView();
                            Snackbar snackbar = Snackbar.make(getView(), "Records successfully deleted", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        });
                        builder.setNegativeButton(R.string.delete_transaction_no_button, (dialog, which) -> {
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        break;
                    default:
                        break;
                }
                return true;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
                navigationBar.setVisibility(View.GONE);
                getActivity().getMenuInflater().inflate(R.menu.multi_list_selection, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                navigationBar.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean arg3) {

                int checkedItems = getListView().getCheckedItemCount();

                double sumOfSelectedTransactions = 0;
                SparseBooleanArray checkedItemPositions = getListView().getCheckedItemPositions();
                for (int i = 0; i < checkedItemPositions.size(); i++) {
                    if (checkedItemPositions.valueAt(i)) {
                        Transaction transaction = (Transaction) getListView().getAdapter().getItem(checkedItemPositions.keyAt(i));
                        sumOfSelectedTransactions = sumOfSelectedTransactions + transaction.getAmount();
                    }
                }

                if (checkedItems == 1) {
                    mode.setTitle(checkedItems + " Transaction = " + sumOfSelectedTransactions);
                } else {
                    mode.setTitle(checkedItems + " Transactions = " + sumOfSelectedTransactions);
                }
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //TODO implement method
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
        updateView();
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resetSearch() {
        transactionsName = "";
        updateView();
    }

    private List<Item> sortAndAddHeaders(List<Transaction> transactionList) {
        List<Item> sortedList = new LinkedList<>();

        Date dateHolder = new Date();
        //loops through the list and add a header signalising new day
        for (int i = 0; i < transactionList.size(); i++) {
            //if it is the start of a new day create a new header (as told higher)
            if (!(dateHolder.equals(transactionList.get(i).getDate()))) {
                //if it's date header make new and add it
                sortedList.add(new Header(transactionList.get(i).getDate()));
                dateHolder = transactionList.get(i).getDate();
            }
            //if it's transaction just add it
            sortedList.add(transactionList.get(i));
        }

        return sortedList;
    }

    public Date convertStringToDate(String dateAsString) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date date = new Date();
        try {
            date = format.parse(dateAsString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertDateToString(int dayOfMonth, int month, int year, String dayOfWeek) {
        String yyyy = "" + year;
        String MM = "" + month;
        String dd = "" + dayOfMonth;

        if (month < 10) {
            MM = "0" + month;
        }
        if (dayOfMonth < 10) {
            dd = "0" + dayOfMonth;
        }

        return dd + "." + MM + "." + yyyy + ", " + dayOfWeek;
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.chooseTransactionName: {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (chooseTransactionName.getRight() - chooseTransactionName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        chooseTransactionName.setText("");
                        v.performClick();
                        return false;
                    }
                }
                break;
            }
            case R.id.chooseTransactionAmount: {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (chooseTransactionName.getRight() - chooseTransactionName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        chooseTransactionAmount.setText("");
                        v.performClick();
                        return false;
                    }
                }
            }
            break;
        }
        return false;
    }

    //TEST METHODS
    public void createTestRecords() {

        for (int i = 0; i < 15; i++) {
            //INCOMES
            transactionRepository.create(new Transaction(0, "Wypłata za październik", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(0, "Wypłata za listopad", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
            transactionRepository.create(new Transaction(0, "Wypłata za grudzień", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(0, "Premia na święta", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(0, "Wypłata za styczeń", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(0, "Wypłata za luty", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".02.2020")));
            transactionRepository.create(new Transaction(0, "Wypłata za marzec", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(0, "Wygrana w lotka na początku marca", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(0, "Testowy wyraz bardzo dlugi", (float) (new Random().nextInt(5000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));

            //OCTOBER
            transactionRepository.create(new Transaction(1, "Pizza", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "Trankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "CD Action", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "Przegląd roweru", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
            transactionRepository.create(new Transaction(1, "Karta graficzna", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));

            //NOVEMBER
            transactionRepository.create(new Transaction(1, "Wycieczka Wawka", (float) new Random().nextInt(250) - 250, "Hobby", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
            transactionRepository.create(new Transaction(1, "Fifa 2021", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
            transactionRepository.create(new Transaction(1, "CD Action", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
            transactionRepository.create(new Transaction(1, "Delegacja na Słowację", (float) new Random().nextInt(250) - 250, "Hobby", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));

            //DECEMBER
            transactionRepository.create(new Transaction(1, "Prezent na Boże Narodziny mama", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Prezent na Boże Narodziny ojciec", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Mikołajki PLUM", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
            transactionRepository.create(new Transaction(1, "Wymiana lusterka YARIS", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));

            //JANUARY
            transactionRepository.create(new Transaction(1, "Sylwester", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Happy Meal w macu", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Pizza", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));

            //FEBRUARY
            transactionRepository.create(new Transaction(1, "Spodnie do biegania", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Koszula galowa", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Pizza", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
            transactionRepository.create(new Transaction(1, "Zakupy w LIDLu", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));

            //MARCH
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567999999999", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567890", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Buty do biegania", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Opaska Xiaomi", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "DOOM", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
            transactionRepository.create(new Transaction(1, "Zakupy w LIDLu", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));

            //APRIL
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567999999999", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567890", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Buty do biegania", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Opaska Xiaomi", (float) new Random().nextInt(250) - 250, "Sport", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "DOOM", (float) new Random().nextInt(250) - 250, "Game", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Tankowanie", (float) new Random().nextInt(250) - 250, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
            transactionRepository.create(new Transaction(1, "Zakupy w LIDLu", (float) new Random().nextInt(250) - 250, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));

            //MAY
            transactionRepository.create(new Transaction(1, "Prezent na komunię", (float) new Random().nextInt(250) - 250, "Other", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        }
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Toast toast = Toast.makeText(getContext(), "Hello Javatpoint", Toast.LENGTH_SHORT);
        toast.show();
        return false;
    }
}