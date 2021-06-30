package com.example.myexpenses.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;

import com.example.myexpenses.R;
import com.example.myexpenses.arrayAdapter.ItemAdapter;
import com.example.myexpenses.dialogFragment.ModifyTransactionDialog;
import com.example.myexpenses.model.Header;
import com.example.myexpenses.model.Item;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.other.CurrentMonthData;
import com.example.myexpenses.other.SearchFilter;
import com.example.myexpenses.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class ListTransactionFragment extends ListFragment implements AdapterView.OnItemClickListener {

    //onCreate
    private TransactionRepository transactionRepository;
    private ItemAdapter itemAdapter;
    private int actualDay;
    private int actualMonth;
    private int actualYear;
    private Date actualDate;
    private int dailyLimit;
    private int monthlyLimit;

    //search criteria
    private Integer currentChosenMonth;
    private Integer currentChosenYear;
    private int transactionsType;
    private String transactionsName;
    private String transactionsCategory;
    private int transactionsOrder;

    private ListTransactionsFragmentCallback listTransactionsFragmentCallback;
    private SpannableStringBuilder formattedTotalSum;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            listTransactionsFragmentCallback = (ListTransactionsFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement ListTransactionsFragment.ListTransactionsFragmentCallback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transactionRepository = new TransactionRepository(getContext());

        int monthOffset = getArguments().getInt("monthOffset");
        actualDay = getArguments().getInt("actualDay");
        actualMonth = getArguments().getInt("actualMonth");
        actualYear = getArguments().getInt("actualYear");
        dailyLimit = getArguments().getInt("dailyLimit");
        monthlyLimit = getArguments().getInt("monthlyLimit");
        calculateCurrentMonthAndYear(monthOffset);
        //search criteria
        transactionsType = getArguments().getInt("transactionsType");
        transactionsName = getArguments().getString("transactionsName");
        transactionsCategory = getArguments().getString("transactionsCategory");
        transactionsOrder = getArguments().getInt("transactionsOrder");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(currentChosenYear, currentChosenMonth, actualDay, 0, 0, 0);
        actualDate = new Date();
        actualDate.setTime(calendar.getTime().getTime());
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //onCreateView
        View view = inflater.inflate(R.layout.fragment_list_transactions, container, false);
        itemAdapter = new ItemAdapter(getActivity(), new ArrayList<>());
        setListAdapter(itemAdapter);
        updateListData();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateListData() {
        List<Transaction> foundedMonthTransactions = transactionRepository.findTransactionsInMonthByFilter(new String[]{
                String.valueOf(currentChosenMonth),
                String.valueOf(currentChosenYear),
                String.valueOf(transactionsType),
                transactionsName,
                transactionsCategory,
                String.valueOf(transactionsOrder)});

        int sumOfCurrentChosenMonthTransactions = foundedMonthTransactions.stream()
                .mapToInt(Transaction::getAmount)
                .sum();

        int sumOfActualDayExpenses = 0;
        if (currentChosenMonth == actualMonth && currentChosenYear == actualYear) {
            sumOfActualDayExpenses = Math.abs(transactionRepository.getSumOfDailyExpenses(actualDay, actualMonth, actualYear));
        }

        int sumOfCurrentChosenMonthExpenses = Math.abs(transactionRepository.getSumOfMonthlyExpenses(currentChosenMonth, currentChosenYear));

        List<Item> monthTransactionsListWithAddedHeaders = sortAndAddHeaders(foundedMonthTransactions);

        formattedTotalSum = new SpannableStringBuilder();
        Spannable sumOfMonthlyTransactions;
        if (sumOfCurrentChosenMonthTransactions >= 0) {
            sumOfMonthlyTransactions = new SpannableString(String.format("+%.2f", getValueInCurrency(sumOfCurrentChosenMonthTransactions)));
            sumOfMonthlyTransactions.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.sum_greater_than_zero)), 0, sumOfMonthlyTransactions.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            sumOfMonthlyTransactions = new SpannableString(String.format("%.2f", getValueInCurrency(sumOfCurrentChosenMonthTransactions)));
            sumOfMonthlyTransactions.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.sum_lesser_than_zero)), 0, sumOfMonthlyTransactions.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        formattedTotalSum.append(sumOfMonthlyTransactions);

        if ((currentChosenMonth == actualMonth && currentChosenYear == actualYear) && sumOfActualDayExpenses > dailyLimit && sumOfCurrentChosenMonthExpenses > monthlyLimit) {
            Spannable limitExceeded = new SpannableString(" (D!M!)");
            limitExceeded.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.limit_reached)), 0, limitExceeded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            formattedTotalSum.append(limitExceeded);
        } else if ((currentChosenMonth == actualMonth && currentChosenYear == actualYear) && sumOfActualDayExpenses > dailyLimit) {
            Spannable limitExceeded = new SpannableString(" (D!)");
            limitExceeded.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.limit_reached)), 0, limitExceeded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            formattedTotalSum.append(limitExceeded);
        } else if (sumOfCurrentChosenMonthExpenses > monthlyLimit) {
            Spannable limitExceeded = new SpannableString(" (M!)");
            limitExceeded.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.limit_reached)), 0, limitExceeded.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            formattedTotalSum.append(limitExceeded);
        }

        itemAdapter.clear();
        itemAdapter.addAll(monthTransactionsListWithAddedHeaders);
        itemAdapter.notifyDataSetChanged();
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
        data.putInt("dailyLimit", dailyLimit);
        data.putInt("monthlyLimit", monthlyLimit);
        data.putLong("actualDate", actualDate.getTime());
        modifyTransactionDialog.setArguments(data);

        modifyTransactionDialog.show(fragmentManager, "add new transaction fragment");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setSelector(R.drawable.level_list_selector);
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int numberOfTransactionsOnList = 0;
            MenuItem selectAll;
            int stateOfMultiChoiceOperation = 4; //default - no transaction was deleted

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.deleteTransactions: {
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
                            transactionRepository.deleteTransactions(transactionsIds);
                            updateListData();
                            //it must be after repository operations!, because if not refresh action is called after closing the multichoice mode
                            stateOfMultiChoiceOperation = 2;
                            if (transactionsIds.length > 1) {
                                stateOfMultiChoiceOperation = 3;
                            }
                            mode.finish();
                        });
                        builder.setNegativeButton(R.string.delete_transaction_no_button, (dialog, which) -> {
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        break;
                    }

                    case R.id.selectAll: {

                        if (menuItem.isChecked()) {
                            for (int i = 0; i < getListView().getAdapter().getCount(); i++) {
                                Object item = getListView().getAdapter().getItem(i);
                                if (item instanceof Transaction) {
                                    getListView().setItemChecked(i, true);
                                }
                                menuItem.setIcon(R.drawable.unselect_all);
                                menuItem.setChecked(false);
                            }
                        } else {
                            for (int i = 0; i < getListView().getAdapter().getCount(); i++) {
                                Object item = getListView().getAdapter().getItem(i);
                                if (item instanceof Transaction) {
                                    getListView().setItemChecked(i, false);
                                }
                                menuItem.setIcon(R.drawable.select_all);
                            }
                        }
                        break;
                    }

                    default:
                        break;
                }
                return true;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
                getActivity().getMenuInflater().inflate(R.menu.multi_list_selection, menu);

                listTransactionsFragmentCallback.listTransactionsFragmentCallback(1);
                stateOfMultiChoiceOperation = 4; //reset parameter

                selectAll = menu.findItem(R.id.selectAll);
                numberOfTransactionsOnList = getListView().getAdapter().getCount();

                int totalAmountOfTransactionsOnList = 0;
                for (int i = 0; i < getListView().getAdapter().getCount(); i++) {
                    Object item = getListView().getAdapter().getItem(i);
                    if (item instanceof Transaction) {
                        totalAmountOfTransactionsOnList++;
                    }
                }

                numberOfTransactionsOnList = totalAmountOfTransactionsOnList;

                if (totalAmountOfTransactionsOnList == 1) {
                    selectAll.setIcon(R.drawable.unselect_all);
                } else {
                    selectAll.setIcon(R.drawable.select_all);
                    selectAll.setChecked(true);
                }

                return true;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                listTransactionsFragmentCallback.listTransactionsFragmentCallback(stateOfMultiChoiceOperation);
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                                  boolean arg3) {

                int checkedItems = getListView().getCheckedItemCount();
                //if we checked all transaction then change selectAll icon
                if (checkedItems == numberOfTransactionsOnList) {
                    selectAll.setIcon(R.drawable.unselect_all);
                    selectAll.setChecked(false);
                } else {
                    selectAll.setIcon(R.drawable.select_all);
                    selectAll.setChecked(true);
                }

                int sumOfSelectedTransactions = 0;
                SparseBooleanArray checkedItemPositions = getListView().getCheckedItemPositions();
                for (int i = 0; i < checkedItemPositions.size(); i++) {
                    if (checkedItemPositions.valueAt(i)) {
                        Transaction transaction = (Transaction) getListView().getAdapter().getItem(checkedItemPositions.keyAt(i));
                        sumOfSelectedTransactions = sumOfSelectedTransactions + transaction.getAmount();
                    }
                }

                if (checkedItems == 1) {
                    mode.setTitle(checkedItems + " Item = " + String.format("%.2f", getValueInCurrency(sumOfSelectedTransactions)));
                } else {
                    String text = Double.toString(Math.abs(sumOfSelectedTransactions));
                    int integerPlaces = text.indexOf('.');
                    if (integerPlaces > 7) {
                        mode.setTitle(checkedItems + " Items = " + String.format("%.2e", getValueInCurrency(sumOfSelectedTransactions)));
                    } else {
                        mode.setTitle(checkedItems + " Items = " + String.format("%.2f", getValueInCurrency(sumOfSelectedTransactions)));
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        //when fragment is not visible scroll view to the top of the list
        getListView().setSelectionAfterHeaderView();
    }

    public void setLimits(int dailyLimit, int monthlyLimit) {
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setSearchFilters(SearchFilter searchFilter) {
        transactionsType = searchFilter.getTransactionsType();
        transactionsName = searchFilter.getTransactionsName();
        transactionsCategory = searchFilter.getTransactionsCategory();
        transactionsOrder = searchFilter.getTransactionsOrder();
        updateListData();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateFragment() {
        updateListData();
    }

    public CurrentMonthData getDataFromListTransactionsFragment() {
        return new CurrentMonthData(currentChosenMonth, currentChosenYear, formattedTotalSum);
    }

    public SpannableStringBuilder getFormattedTotalSum() {
        return formattedTotalSum;
    }

    public interface ListTransactionsFragmentCallback {
        void listTransactionsFragmentCallback(int stateOfMultiChoiceOperation);
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

    private void calculateCurrentMonthAndYear(int monthOffset) {
        if (actualMonth + monthOffset < 0) {
            currentChosenYear = actualYear + ((actualMonth + monthOffset + 1) / 12 - 1);
        } else {
            currentChosenYear = actualYear + (actualMonth + monthOffset) / 12;
        }

        currentChosenMonth = (actualMonth + monthOffset) % 12;
        if (currentChosenMonth < 0) {
            currentChosenMonth += 12;
        }
    }
}