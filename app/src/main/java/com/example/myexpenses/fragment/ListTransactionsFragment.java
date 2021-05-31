package com.example.myexpenses.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;

import com.example.myexpenses.R;
import com.example.myexpenses.customAdapter.ItemAdapter;
import com.example.myexpenses.dialogFragment.AddNewTransactionDialog;
import com.example.myexpenses.dialogFragment.ModifyTransactionDialog;
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

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class ListTransactionsFragment extends ListFragment implements AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener, AddNewTransactionDialog.AddNewTransactionDialogCommunicator, ModifyTransactionDialog.ModifyTransactionDialogCommunicator {

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

    private SearchView searchView;
    private Menu menu;

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

        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
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

        updateView("NO_ANIMATION");

        FloatingActionButton addNewTransaction = view.findViewById(R.id.addNewTransaction);
        addNewTransaction.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
                updateView("NO_ANIMATION");
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
                    updateView("NO_ANIMATION");
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
                updateView("LEFT_TO_RIGHT_ANIMATION");
                break;
            }

            case R.id.nextMonth: {
                if (currentChosenMonth == 11) { //months are indexed starting from zero
                    currentChosenMonth = 0;
                    currentChosenYear++;
                } else {
                    currentChosenMonth++;
                }
                updateView("RIGHT_TO_LEFT_ANIMATION");
                break;
            }

            case R.id.addNewTransaction: {

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                AddNewTransactionDialog addNewTransactionDialog = new AddNewTransactionDialog();
                addNewTransactionDialog.setTargetFragment(this, 1);

                //passing data to dialog
                Bundle data = new Bundle();
                data.putInt("actualDay", actualDay);
                data.putInt("actualMonth", actualMonth);
                data.putInt("actualYear", actualYear);
                data.putInt("currentChosenMonth", currentChosenMonth);
                data.putInt("currentChosenYear", currentChosenYear);
                data.putFloat("dailyLimit", dailyLimit);
                data.putFloat("monthlyLimit", monthlyLimit);
                data.putLong("actualDate", actualDate.getTime());
                addNewTransactionDialog.setArguments(data);

                addNewTransactionDialog.show(fragmentManager, "add new transaction fragment");
                break;
            }

            case R.id.sortTransactions: {
                PopupMenu popupMenu = new PopupMenu(getContext(), sortTransactions);
                popupMenu.setGravity(Gravity.END);
                popupMenu.inflate(R.menu.popup_menu);

                popupMenu.setOnMenuItemClickListener(item -> {
                    transactionsOrder = (String) item.getTitle();
                    updateView("NO_ANIMATION");
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
    private void updateView(String typeOfAnimation) {
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
                .mapToDouble(o -> Math.abs(o.getAmount()))
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
            sumOfMonthlyTransactions.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.sum_greater_than_zero)), 0, sumOfMonthlyTransactions.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            sumOfMonthlyTransactions = new SpannableString(String.format("%.2f", sumOfCurrentChosenMonthTransactions));
            sumOfMonthlyTransactions.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.sum_lesser_than_zero)), 0, sumOfMonthlyTransactions.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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

        switch (typeOfAnimation) {
            case "NO_ANIMATION": {
                break;
            }
//            case "RIGHT_TO_LEFT_ANIMATION": {
//                listView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.right_to_left));
//                break;
//            }
//            case "LEFT_TO_RIGHT_ANIMATION": {
//                listView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.left_to_right));
//                break;
//            }
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.filterTransactionCategory: {
                updateView("NO_ANIMATION");
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

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        ModifyTransactionDialog modifyTransactionDialog = new ModifyTransactionDialog();
        modifyTransactionDialog.setTargetFragment(this, 1);

        //passing data to dialog
        Bundle data = new Bundle();
        data.putSerializable("selectedTransaction", selectedTransaction);
        data.putInt("actualDay", actualDay);
        data.putInt("actualMonth", actualMonth);
        data.putInt("actualYear", actualYear);
        data.putInt("currentChosenMonth", currentChosenMonth);
        data.putInt("currentChosenYear", currentChosenYear);
        data.putFloat("dailyLimit", dailyLimit);
        data.putFloat("monthlyLimit", monthlyLimit);
        data.putLong("actualDate", actualDate.getTime());
        modifyTransactionDialog.setArguments(data);

        modifyTransactionDialog.show(fragmentManager, "add new transaction fragment");
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void handleOnBackPressed() {
            // Handle the back button event

            if (filterBar.getVisibility() == View.VISIBLE) {
                MenuItem item = menu.findItem(R.id.menuFilter);
                item.setChecked(!item.isChecked());
                item.setIcon(R.drawable.menu_show_filter);
                filterBar.setVisibility(View.GONE);
                transactionsCategory.setSelection(0);
                transactionsOrder = "";
                updateView("NO_ANIMATION");
            } else if (!searchView.isIconified()) {
                searchView.setIconified(true);
            } else {
                this.setEnabled(false);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        }
    };

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
                            updateView("NO_ANIMATION");
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

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
//                navigationBar.setVisibility(View.GONE); //now navigationBar goes below menu
                getActivity().getMenuInflater().inflate(R.menu.multi_list_selection, menu);

                //close navigation bar and DON'T clear search filters
                if (filterBar.getVisibility() == View.VISIBLE) {
                    filterBar.setVisibility(View.GONE);
                }

                return true;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                navigationBar.setVisibility(View.VISIBLE);
                //set menu show filter icon
                MenuItem item = menu.findItem(R.id.menuFilter);
                item.setIcon(R.drawable.menu_show_filter);
                //clear filters
                transactionsCategory.setSelection(0);
                transactionsOrder = "";
                updateView("NO_ANIMATION");
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
        updateView("NO_ANIMATION");
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resetSearch() {
        transactionsName = "";
        updateView("NO_ANIMATION");
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

    public String convertMonthToString(int month, int year) {
        //months are indexed starting at 0
        String MM = "" + month;
        String yyyy = "" + year;

        if (month < 10) {
            MM = "0" + month;
        }

        return MM + "/" + yyyy;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void AddNewTransactionCallback(boolean shouldUpdateListView) {
        Snackbar snackbar = Snackbar.make(getView(), "Record successfully added", Snackbar.LENGTH_LONG);
        snackbar.show();

        if (shouldUpdateListView) {
            updateView("NO ANIMATION");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void ModifyTransactionCallback(int typeOfOperation) {
        Snackbar snackbar;
        switch (typeOfOperation) {
            case 1:
                snackbar = Snackbar.make(getView(), "Record successfully deleted", Snackbar.LENGTH_LONG);
                snackbar.show();
                updateView("NO_ANIMATION");
                break;
            case 2:
                snackbar = Snackbar.make(getView(), "Record successfully updated", Snackbar.LENGTH_LONG);
                snackbar.show();
                updateView("NO_ANIMATION");
                break;
            case 3:
                snackbar = Snackbar.make(getView(), "Record successfully added", Snackbar.LENGTH_LONG);
                snackbar.show();
                updateView("NO_ANIMATION");
                break;
            case 4:
                snackbar = Snackbar.make(getView(), "Record successfully added", Snackbar.LENGTH_LONG);
                snackbar.show();
                break;
            default:
                break;
        }
    }
}