package com.example.myexpenses.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.ListFragment;

import com.example.myexpenses.R;
import com.example.myexpenses.customAdapter.TransactionAdapter;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class LastTransactionsFragment extends ListFragment implements AdapterView.OnItemClickListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener {

    //onCreate
    private TransactionRepository transactionRepository;
    //onCreateView
    private View view;
    private ImageView previousMonthButton;
    private TextView actualChosenMonth;
    private ImageView nextMonthButton;
    private TextView monthlyTransactionSum;
    private TransactionAdapter transactionAdapter;
    private FloatingActionButton addTransactionActionButton;
    //popupWindow
    private View popupView;
    private PopupWindow popupWindow;
    private Toolbar toolbar;
    private ImageButton toolbarClose;
    private TextView toolbarText;
    private Button chooseTransactionDate;
    private Button chooseTodayTomorrow;
    private RadioGroup chooseTransactionType;
    private RadioButton chosenTransactionExpense;
    private RadioButton chosenTransactionIncome;
    private ImageView transactionCategoryImage;
    private Spinner chooseTransactionCategory;
    private EditText chooseTransactionAmount;
    private EditText chooseTransactionName;
    private Button deleteTransactionButton;
    private Button saveTransactionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionRepository = new TransactionRepository(getContext());
        setHasOptionsMenu(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_last_transactions, container, false);

        previousMonthButton = view.findViewById(R.id.previousMonthButton);
        previousMonthButton.setOnClickListener(this);

        actualChosenMonth = view.findViewById(R.id.actualChosenMonth);

        nextMonthButton = view.findViewById(R.id.nextMonthButton);
        nextMonthButton.setOnClickListener(this);

        monthlyTransactionSum = view.findViewById(R.id.monthlyTransactionSum);

        transactionAdapter = new TransactionAdapter(getActivity(), new ArrayList());
        setListAdapter(transactionAdapter);

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; //month index start at 0
        int currentYear = calendar.get(Calendar.YEAR);

        updateView(currentMonth, currentYear);

        addTransactionActionButton = view.findViewById(R.id.add_transaction_action_button);
        addTransactionActionButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previousMonthButton: {
                int currentChosenMonth = Integer.parseInt(this.actualChosenMonth.getText().toString().substring(0, 2));
                int currentChosenMonthYear = Integer.parseInt(this.actualChosenMonth.getText().toString().substring(3, 7));

                Integer[] previousMonthInTable = getPreviousMonth(currentChosenMonth, currentChosenMonthYear);
                int previousMonth = previousMonthInTable[0];
                int previousMonthYear = previousMonthInTable[1];

                updateView(previousMonth, previousMonthYear);

                break;
            }

            case R.id.nextMonthButton: {
                int currentChosenMonth = Integer.parseInt(this.actualChosenMonth.getText().toString().substring(0, 2));
                int currentChosenMonthYear = Integer.parseInt(this.actualChosenMonth.getText().toString().substring(3, 7));

                Integer[] nextMonthInTable = getNextMonth(currentChosenMonth, currentChosenMonthYear);
                int nextMonth = nextMonthInTable[0];
                int nextMonthYear = nextMonthInTable[1];

                updateView(nextMonth, nextMonthYear);

                break;
            }

            case R.id.add_transaction_action_button: {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                popupView = inflater.inflate(R.layout.popup_new_transaction, null);

                popupWindow = new PopupWindow(popupView, 565, 760, true);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                toolbar = popupView.findViewById(R.id.tool_bar);
                toolbarClose = popupView.findViewById(R.id.toolbarClose);
                toolbarClose.setOnClickListener(this);

                toolbarText = popupView.findViewById(R.id.toolbarText);
                toolbarText.setText("Add record");

                int currentDay = 1;
                int currentMonth = Integer.parseInt(actualChosenMonth.getText().toString().substring(0, 2)) - 1;
                int currentYear = Integer.parseInt(actualChosenMonth.getText().toString().substring(3, 7));
                Date date = new Date(currentYear, currentMonth, currentDay - 1);
                SimpleDateFormat simpledateformat = new SimpleDateFormat("EE");
                String currentDayOfWeek = simpledateformat.format(date);
                String currentDateAsString = convertDateToString(currentDay, currentMonth, currentYear, currentDayOfWeek);

                chooseTransactionDate = popupView.findViewById(R.id.chooseTransactionDate);
                chooseTransactionDate.setText(currentDateAsString);
                chooseTransactionDate.setOnClickListener(this);

                chooseTodayTomorrow = popupView.findViewById(R.id.chooseTodayTommorow);
                chooseTodayTomorrow.setOnClickListener(this);

                chooseTransactionType = popupView.findViewById(R.id.chooseTransactionType);
                chooseTransactionType.setOnCheckedChangeListener(this);
                chosenTransactionExpense = popupView.findViewById(R.id.chosenTransactionExpense);
                chosenTransactionIncome = popupView.findViewById(R.id.chosenTransactionIncome);

                transactionCategoryImage = popupView.findViewById(R.id.transactionCategoryImage);

                chooseTransactionCategory = popupView.findViewById(R.id.chooseTransactionCategory);
                ArrayAdapter<String> chooseCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.popup_spinner_layout, getResources().getStringArray(R.array.list_of_expenses));
                chooseTransactionCategory.setAdapter(chooseCategoryAdapter);
                chooseTransactionCategory.setOnItemSelectedListener(this);

                chooseTransactionAmount = popupView.findViewById(R.id.chooseTransactionAmount);
                chooseTransactionAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
                chooseTransactionAmount.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                chooseTransactionName = popupView.findViewById(R.id.chooseTransactionName);

                saveTransactionButton = popupView.findViewById(R.id.saveTransactionButton);
                saveTransactionButton.setOnClickListener(this);
                break;
            }

            case R.id.chooseTransactionDate: {
                int day = 1;
                int month = Integer.parseInt(actualChosenMonth.getText().toString().substring(0, 2)) - 1;
                int year = Integer.parseInt(actualChosenMonth.getText().toString().substring(3, 7));

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DialogTheme, (view, chosenYear, chosenMonth, chosenDayOfMonth) -> {
                    SimpleDateFormat simpledateformat = new SimpleDateFormat("EE");
                    Date date = new Date(chosenYear, chosenMonth, chosenDayOfMonth - 1);
                    String chosenDayOfWeek = simpledateformat.format(date); //get week name f.e.: Tue
                    String chosenDate = convertDateToString(chosenDayOfMonth, chosenMonth, chosenYear, chosenDayOfWeek);
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

            case R.id.chooseTodayTommorow: {
                if (chooseTodayTomorrow.getText().toString().equals("Today")) {
                    final Calendar calendarToday = Calendar.getInstance();
                    int yearToday = calendarToday.get(Calendar.YEAR);
                    int monthToday = calendarToday.get(Calendar.MONTH);
                    int dayToday = calendarToday.get(Calendar.DAY_OF_MONTH);

                    SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE");
                    Date date = new Date(yearToday, monthToday, dayToday - 1);
                    String dayOfWeekToday = simpledateformat.format(date);

                    String todayDate = convertDateToString(dayToday, monthToday, yearToday, dayOfWeekToday);
                    chooseTodayTomorrow.setText("Yesterday");
                    chooseTransactionDate.setText(todayDate);
                } else {
                    final Calendar calendarYesterday = Calendar.getInstance();
                    calendarYesterday.add(Calendar.DATE, -1);
                    int yearYesterday = calendarYesterday.get(Calendar.YEAR);
                    int monthYesterday = calendarYesterday.get(Calendar.MONTH);
                    int dayYesterday = calendarYesterday.get(Calendar.DAY_OF_MONTH);
                    SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE");
                    Date date = new Date(yearYesterday, monthYesterday, dayYesterday - 1);
                    String dayOfWeekYesterday = simpledateformat.format(date);

                    String yesterday = convertDateToString(dayYesterday, monthYesterday, yearYesterday, dayOfWeekYesterday);
                    chooseTodayTomorrow.setText("Today");
                    chooseTransactionDate.setText(yesterday);
                }
                break;
            }

            case R.id.deleteTransactionButton: {

                break;
            }

            case R.id.saveTransactionButton: {
                Transaction transactionToSave = new Transaction();

                //check if name field is empty, if so we set it as category name
                String transactionName = this.chooseTransactionName.getText().toString();

                //check if amount is empty if so we set it as zero
                Float transactionAmount;
                try {
                    transactionAmount = Float.valueOf(this.chooseTransactionAmount.getText().toString());
                } catch (NumberFormatException e) {
                    transactionAmount = 0f;
                }

                //check there typeOfTransaction: 0 - income, 1 expense,
                //also if its expense we need to add minus before amount
                RadioButton checkedRadioButton = popupView.findViewById(chooseTransactionType.getCheckedRadioButtonId());
                String categoryName = checkedRadioButton.getText().toString();
                Integer transactionType = 0;
                if (categoryName.equals("Expense")) {
                    transactionType = 1;
                    transactionAmount = -transactionAmount;
                }

                transactionToSave.setName(transactionName);
                transactionToSave.setAmount(transactionAmount);
                transactionToSave.setType(transactionType);
                transactionToSave.setCategory(chooseTransactionCategory.getSelectedItem().toString());
                transactionToSave.setDate(convertStringToDate(chooseTransactionDate.getText().toString()));
                transactionRepository.create(transactionToSave);

                //check if we need to actualise current seen list of transactions and total sum
                int chosenMonthFromListView = Integer.parseInt(actualChosenMonth.getText().toString().substring(0, 2));
                int chosenYearFromListView = Integer.parseInt(actualChosenMonth.getText().toString().substring(3, 7));
                int monthOfNewTransaction = Integer.parseInt(chooseTransactionDate.getText().toString().substring(3, 5));
                int yearOfNewTransaction = Integer.parseInt(chooseTransactionDate.getText().toString().substring(6, 10));

                if ((chosenMonthFromListView == monthOfNewTransaction) && (chosenYearFromListView == yearOfNewTransaction)) {
                    updateView(monthOfNewTransaction, yearOfNewTransaction);
                }

                Snackbar snackbar = Snackbar.make(getView(), "Record successfully added", Snackbar.LENGTH_LONG);
                snackbar.show();

                popupWindow.dismiss();
                break;
            }

            case R.id.toolbarClose: {
                popupWindow.dismiss();
                break;
            }

            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.chooseTransactionCategory: {
                String transactionTypeName = chooseTransactionCategory.getSelectedItem().toString().toLowerCase().replace(" ", "_");
                int idOfSuitableDrawableForTransactionTypeName = getContext().getResources().getIdentifier(transactionTypeName, "drawable", getContext().getPackageName());
                transactionCategoryImage.setBackgroundResource(idOfSuitableDrawableForTransactionTypeName);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        //get selected transaction from list
        Transaction selectedTransaction = (Transaction) (getListAdapter()).getItem(position);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_modify_transaction, null);

        popupWindow = new PopupWindow(popupView, 565, 851, true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        toolbar = popupView.findViewById(R.id.tool_bar);
        toolbarClose = popupView.findViewById(R.id.toolbarClose);
        toolbarClose.setOnClickListener(this);

        toolbarText = popupView.findViewById(R.id.toolbarText);
        toolbarText.setText("Modify record");

        Date date = selectedTransaction.getDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("EE");
        String currentDayOfWeek = simpledateformat.format(date);
        String currentDateAsString = convertDateToString(currentDay, currentMonth, currentYear, currentDayOfWeek);

        chooseTransactionDate = popupView.findViewById(R.id.chooseTransactionDate);
        chooseTransactionDate.setText(currentDateAsString);
        chooseTransactionDate.setOnClickListener(this);

        chooseTodayTomorrow = popupView.findViewById(R.id.chooseTodayTommorow);
        chooseTodayTomorrow.setOnClickListener(this);

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
        chooseTransactionAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7, 2)});
        double transactionAmount = selectedTransaction.getAmount();
        chooseTransactionAmount.setText(String.format("%.2f", Math.abs(transactionAmount)));
        chooseTransactionAmount.requestFocus();

        chooseTransactionName = popupView.findViewById(R.id.chooseTransactionName);
        chooseTransactionName.setText(selectedTransaction.getName());

        deleteTransactionButton = popupView.findViewById(R.id.deleteTransactionButton);
        deleteTransactionButton.setOnClickListener(this);

        saveTransactionButton = popupView.findViewById(R.id.saveTransactionButton);
        saveTransactionButton.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getId()) {
            case R.id.chooseTransactionType:
                int idOfCheckedRadiobutton = chooseTransactionType.getCheckedRadioButtonId();
                RadioButton checkedRadiobutton = popupView.findViewById(idOfCheckedRadiobutton);
                checkedRadiobutton.setChecked(true);
                String chosenTransactionTypeName = checkedRadiobutton.getText().toString();

                if (chosenTransactionTypeName.equals("Expense")) {
                    chosenTransactionExpense.setChecked(true);
                    chosenTransactionIncome.setChecked(false);
                    ArrayAdapter<String> chooseCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.popup_spinner_layout, getResources().getStringArray(R.array.list_of_expenses));
                    chooseTransactionCategory.setAdapter(chooseCategoryAdapter);
                } else {
                    chosenTransactionExpense.setChecked(false);
                    chosenTransactionIncome.setChecked(true);
                    ArrayAdapter<String> chooseCategoryAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.popup_spinner_layout, getResources().getStringArray(R.array.list_of_incomes));
                    chooseTransactionCategory.setAdapter(chooseCategoryAdapter);
                }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //TODO implement method
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            resetSearch();
            return false;
        }

        updateView(newText);
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateView(String newText) {
        ArrayList<Transaction> monthTransactions = transactionRepository.search(newText);
        double totalSum = monthTransactions.stream()
                .mapToDouble(o -> o.getAmount())
                .sum();
        ArrayList<Transaction> monthTransactionsListWithAddedHeaders = sortAndAddHeaders(monthTransactions);

        updateMonthlyTransactionSum(totalSum);
        updateMonthlyTransactionsList(monthTransactionsListWithAddedHeaders);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateView(int currentChosenMonth, int currentChosenMonthYear) {
        ArrayList<Transaction> monthTransactions = transactionRepository.findTransactionsInMonth(currentChosenMonth, currentChosenMonthYear);
        double totalSum = monthTransactions.stream()
                .mapToDouble(o -> o.getAmount())
                .sum();
        ArrayList<Transaction> monthTransactionsListWithAddedHeaders = sortAndAddHeaders(monthTransactions);

        actualChosenMonth.setText(convertMonthToString(currentChosenMonth, currentChosenMonthYear));
        updateMonthlyTransactionSum(totalSum);
        updateMonthlyTransactionsList(monthTransactionsListWithAddedHeaders);
    }

    private ArrayList sortAndAddHeaders(ArrayList<Transaction> transactionList) {
        ArrayList<Transaction> sortedListByDate = new ArrayList<>();
        Collections.sort(transactionList);

        Date dateHolder = new Date();
        //loops through the list and add a header signalising new day
        for (int i = 0; i < transactionList.size(); i++) {
            //if it is the start of a new day create a new header (as told higher)
            if (!(dateHolder.equals(transactionList.get(i).getDate()))) {
                Transaction headerNewDay = new Transaction(transactionList.get(i).getDate());
                headerNewDay.setSectionHeader(true);
                sortedListByDate.add(headerNewDay);
                dateHolder = transactionList.get(i).getDate();
            }
            sortedListByDate.add(transactionList.get(i));
        }

        return sortedListByDate;
    }

    private void updateMonthlyTransactionSum(double totalSum) {
        if (totalSum >= 0) {
            monthlyTransactionSum.setText(String.format("+%.2f", totalSum));
            monthlyTransactionSum.setTextColor(ContextCompat.getColor(getContext(), R.color.ColorPrimary));
        } else {
            monthlyTransactionSum.setText(String.format("%.2f", totalSum));
            monthlyTransactionSum.setTextColor(ContextCompat.getColor(getContext(), R.color.bacgroundColorPopup));
        }
    }

    private void updateMonthlyTransactionsList(ArrayList monthTransactionsListWithAddedHeaders) {
        transactionAdapter.clear();
        transactionAdapter.addAll(monthTransactionsListWithAddedHeaders);
        transactionAdapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resetSearch() {
        int currentChosenMonth = Integer.parseInt(this.actualChosenMonth.getText().toString().substring(0, 2));
        int currentChosenMonthYear = Integer.parseInt(this.actualChosenMonth.getText().toString().substring(3, 7));

        updateView(currentChosenMonth, currentChosenMonthYear);
    }

    private Integer[] getPreviousMonth(int actualChosenMonth, int actualChosenYear) {
        int previousMonth;
        int previousMonthYear;
        Integer[] previousMonthInTable = new Integer[2];

        if (actualChosenMonth == 1) {
            previousMonth = 12;
            previousMonthYear = actualChosenYear - 1;
        } else {
            previousMonth = actualChosenMonth - 1;
            previousMonthYear = actualChosenYear;
        }
        previousMonthInTable[0] = previousMonth;
        previousMonthInTable[1] = previousMonthYear;

        return previousMonthInTable;
    }

    private Integer[] getNextMonth(int actualChosenMonth, int actualChosenYear) {
        int nextMonth;
        int nextMonthYear;
        Integer[] previousMonthInTable = new Integer[2];

        if (actualChosenMonth == 12) {
            nextMonth = 1;
            nextMonthYear = actualChosenYear + 1;
        } else {
            nextMonth = actualChosenMonth + 1;
            nextMonthYear = actualChosenYear;
        }

        previousMonthInTable[0] = nextMonth;
        previousMonthInTable[1] = nextMonthYear;

        return previousMonthInTable;
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
        month = month + 1; //months are indexed starting at 0
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
        month = month; //months are indexed starting at 0
        String MM = "" + month;
        String yyyy = "" + year;

        if (month < 10) {
            MM = "0" + month;
        }

        return MM + "/" + yyyy;
    }

    //TEST METHODS
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
        final Date myDate = cal.getTime();
        return myDate;
    }

    public class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsBeforeZero,int digitsAfterZero) {
            mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Matcher matcher=mPattern.matcher(dest);
            if(!matcher.matches())
                return "";
            return null;
        }

    }

    public void createTestRecords() {
        transactionRepository.deleteAllRecords();

        //INCOMES
        transactionRepository.create(new Transaction(0, "Wypłata za październik", 1200, "Payment", returnDate("08.10.2020")));
        transactionRepository.create(new Transaction(0, "Wypłata za listopad", 1400, "Payment", returnDate("07.11.2020")));
        transactionRepository.create(new Transaction(0, "Wypłata za grudzień", 1300, "Payment", returnDate("07.12.2020")));
        transactionRepository.create(new Transaction(0, "Premia na święta", 500, "Payment", returnDate("14.12.2020")));
        transactionRepository.create(new Transaction(0, "Wypłata za styczeń", 1800, "Payment", returnDate("07.01.2021")));
        transactionRepository.create(new Transaction(0, "Wypłata za luty", 1700, "Payment", returnDate("06.02.2021")));
        transactionRepository.create(new Transaction(0, "Wypłata za marzec", 99999, "Payment", returnDate("09.03.2021")));
        transactionRepository.create(new Transaction(0, "Wygrana w lotka na początku marca", 10000, "Payment", returnDate("10.03.2021")));
        transactionRepository.create(new Transaction(0, "Testowy wyraz bardzo dlugi", 10000, "Payment", returnDate("10.03.2021")));

        //OCTOBER
        transactionRepository.create(new Transaction(1, "Pizza", -45, "Food", returnDate("15.10.2020")));
        transactionRepository.create(new Transaction(1, "Trankowanie", -100, "Car", returnDate("15.10.2020")));
        transactionRepository.create(new Transaction(1, "CD Action", -11, "Game", returnDate("17.10.2020")));
        transactionRepository.create(new Transaction(1, "Przegląd roweru", -150, "Sport", returnDate("19.10.2020")));
        transactionRepository.create(new Transaction(1, "Karta graficzna", -150, "Sport", returnDate("19.10.2020")));

        //NOVEMBER
        transactionRepository.create(new Transaction(1, "Wycieczka Wawka", -45, "Hobby", returnDate("02.11.2020")));
        transactionRepository.create(new Transaction(1, "Fifa 2021", -100, "Game", returnDate("07.11.2020")));
        transactionRepository.create(new Transaction(1, "CD Action", -11, "Game", returnDate("09.11.2020")));
        transactionRepository.create(new Transaction(1, "Delegacja na Słowację", -250, "Hobby", returnDate("21.11.2020")));

        //DECEMBER
        transactionRepository.create(new Transaction(1, "Prezent na Boże Narodziny mama", -155, "Other", returnDate("03.12.2020")));
        transactionRepository.create(new Transaction(1, "Prezent na Boże Narodziny ojciec", -135, "Other", returnDate("03.12.2020")));
        transactionRepository.create(new Transaction(1, "Mikołajki PLUM", -135, "Other", returnDate("06.12.2020")));
        transactionRepository.create(new Transaction(1, "Wymiana lusterka YARIS", -200, "Car", returnDate("12.12.2020")));

        //JANUARY
        transactionRepository.create(new Transaction(1, "Sylwester", -300, "Other", returnDate("01.01.2021")));
        transactionRepository.create(new Transaction(1, "Happy Meal w macu", -20, "Food", returnDate("09.01.2021")));
        transactionRepository.create(new Transaction(1, "Pizza", -35, "Food", returnDate("20.01.2021")));
        transactionRepository.create(new Transaction(1, "Tankowanie", -150, "Car", returnDate("21.01.2021")));

        //FEBRUARY
        transactionRepository.create(new Transaction(1, "Spodnie do biegania", -150, "Sport", returnDate("03.02.2021")));
        transactionRepository.create(new Transaction(1, "Koszula galowa", -70, "Other", returnDate("09.02.2021")));
        transactionRepository.create(new Transaction(1, "Pizza", -35, "Food", returnDate("12.02.2021")));
        transactionRepository.create(new Transaction(1, "Tankowanie", -90, "Car", returnDate("12.02.2021")));
        transactionRepository.create(new Transaction(1, "Zakupy w LIDLu", -78, "Food", returnDate("22.02.2021")));

        //MARCH
        transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567999999999", -5, "Game", returnDate("01.03.2021")));
        transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567890", -120, "Car", returnDate("02.03.2021")));
        transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", -99999, "Food", returnDate("02.03.2021")));
        transactionRepository.create(new Transaction(1, "Bardzo długi wpis 1234567", -9999, "Food", returnDate("03.03.2021")));
        transactionRepository.create(new Transaction(1, "Buty do biegania", -350, "Sport", returnDate("03.03.2021")));
        transactionRepository.create(new Transaction(1, "Opaska Xiaomi", -70, "Sport", returnDate("03.03.2021")));
        transactionRepository.create(new Transaction(1, "DOOM", -95, "Game", returnDate("03.03.2021")));
        transactionRepository.create(new Transaction(1, "Tankowanie", -120, "Car", returnDate("14.03.2021")));
        transactionRepository.create(new Transaction(1, "Zakupy w LIDLu", -95, "Food", returnDate("22.03.2021")));

        //MAY
        transactionRepository.create(new Transaction(1, "Prezent na komunię", -500, "Other", returnDate("22.05.2021")));
    }
}