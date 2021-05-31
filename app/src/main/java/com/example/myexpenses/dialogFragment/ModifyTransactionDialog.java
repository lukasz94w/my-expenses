package com.example.myexpenses.dialogFragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myexpenses.R;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class ModifyTransactionDialog extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener, View.OnTouchListener {

    private int actualDay;
    private int actualMonth;
    private int actualYear;
    private Date actualDate;

    private Integer currentChosenMonth;
    private Integer currentChosenYear;

    private float dailyLimit;
    private float monthlyLimit;

    private Button chooseTransactionDate;
    private Button chooseTodayTomorrow;
    private RadioGroup chooseTransactionType;
    private ImageView transactionCategoryImage;
    private Spinner chooseTransactionCategory;
    private EditText chooseTransactionAmount;
    private EditText chooseTransactionName;

    private Transaction selectedTransaction;

    private ModifyTransactionDialog.ModifyTransactionDialogCommunicator modifyTransactionDialogCommunicator;
    private TransactionRepository transactionRepository;

    private final int TRANSACTION_DELETED = 1;
    private final int TRANSACTION_UPDATED = 2;
    private final int TRANSACTION_ADDED_NEEDED_UPDATE_LIST = 3;
    private final int TRANSACTION_ADDED_NO_NEED_TO_UPDATE_LIST = 4;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            modifyTransactionDialogCommunicator = (ModifyTransactionDialog.ModifyTransactionDialogCommunicator) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement ModifyTransactionDialog.ModifyTransactionDialogCommunicator");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transactionRepository = new TransactionRepository(getContext());

        selectedTransaction = (Transaction) getArguments().getSerializable("selectedTransaction");

        actualDay = getArguments().getInt("actualDay");
        actualMonth = getArguments().getInt("actualMonth");
        actualYear = getArguments().getInt("actualYear");
        currentChosenMonth = getArguments().getInt("currentChosenMonth");
        currentChosenYear = getArguments().getInt("currentChosenYear");
        dailyLimit = getArguments().getFloat("dailyLimit");
        monthlyLimit = getArguments().getFloat("monthlyLimit");
        actualDate = new Date();
        actualDate.setTime(getArguments().getLong("actualDate"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);
        return inflater.inflate(R.layout.dialog_modify_transaction, container);
    }

    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton toolbarClose = view.findViewById(R.id.toolbarClose);
        toolbarClose.setOnClickListener(this);
        TextView toolbarText = view.findViewById(R.id.toolbarText);
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

        chooseTransactionDate = view.findViewById(R.id.chooseTransactionDate);
        chooseTransactionDate.setText(currentDateAsString);
        chooseTransactionDate.setOnClickListener(this);

        chooseTodayTomorrow = view.findViewById(R.id.chooseTodayTomorrow);
        chooseTodayTomorrow.setOnClickListener(this);
        chooseTodayTomorrow.setText("Today");

        chooseTransactionCategory = view.findViewById(R.id.chooseTransactionCategory);
        chooseTransactionCategory.setOnItemSelectedListener(this);

        chooseTransactionType = view.findViewById(R.id.chooseTransactionType);
        chooseTransactionType.setOnCheckedChangeListener(this);
        RadioButton chosenTransactionExpense = view.findViewById(R.id.chosenTransactionExpense);
        RadioButton chosenTransactionIncome = view.findViewById(R.id.chosenTransactionIncome);

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

        transactionCategoryImage = view.findViewById(R.id.transactionCategoryImage);

        chooseTransactionAmount = view.findViewById(R.id.chooseTransactionAmount);
        double primaryValueOfUpdatedTransaction = selectedTransaction.getAmount();
        chooseTransactionAmount.setText(String.format("%.2f", Math.abs(primaryValueOfUpdatedTransaction)));
        chooseTransactionAmount.setOnTouchListener(this);
        chooseTransactionAmount.requestFocus();

        chooseTransactionName = view.findViewById(R.id.chooseTransactionName);
        chooseTransactionName.setText(selectedTransaction.getName());
        chooseTransactionName.setOnTouchListener(this);

        Button deleteTransactionButton = view.findViewById(R.id.deleteTransactionButton);
        deleteTransactionButton.setOnClickListener(this);

        Button saveTransaction = view.findViewById(R.id.saveTransaction);
        saveTransaction.setOnClickListener(this);

        Button saveNewTransaction = view.findViewById(R.id.saveNewTransaction);
        saveNewTransaction.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarClose: {
                int transactionId = selectedTransaction.getId();
                String transactionName = chooseTransactionName.getText().toString();
                RadioButton checkedRadioButton = getView().findViewById(chooseTransactionType.getCheckedRadioButtonId());
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
                    transactionAmount = -transactionAmount;
                }
                String transactionCategory = chooseTransactionCategory.getSelectedItem().toString();
                Date transactionDate = convertStringToDate(chooseTransactionDate.getText().toString());
                Transaction transactionAtMomentOfClosingPopup = new Transaction(transactionId, transactionType, transactionName, transactionAmount, transactionCategory, transactionDate);

                if (!selectedTransaction.equals(transactionAtMomentOfClosingPopup)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(false);
                    builder.setMessage(R.string.unsaved_data_message);
                    builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> dismiss());
                    builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    dismiss();
                }
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

            case R.id.deleteTransactionButton: {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false);
                builder.setMessage(R.string.delete_transaction_message);
                builder.setPositiveButton(R.string.delete_transaction_yes_button, (dialog, which) -> {
                    transactionRepository.deleteTransaction(selectedTransaction.getId());
                    modifyTransactionDialogCommunicator.ModifyTransactionCallback(TRANSACTION_DELETED);
                    dismiss();
                });
                builder.setNegativeButton(R.string.delete_transaction_no_button, (dialog, which) -> {
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
            }

            case R.id.saveTransaction: {
                Transaction transactionToSave = new Transaction();

                int transactionId = selectedTransaction.getId();

                //check if name field is empty, if so we set it as category name
                String transactionName = chooseTransactionName.getText().toString();
                if (transactionName.equals("")) {
                    transactionName = chooseTransactionCategory.getSelectedItem().toString();
                }

                float transactionAmount;
                try {
                    transactionAmount = Float.parseFloat(chooseTransactionAmount.getText().toString());
                    if (transactionAmount == 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(false);
                    builder.setMessage(R.string.incorrect_value_message);
                    builder.setPositiveButton(R.string.OK, (dialog, which) -> {
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return;
                }

                //check there typeOfTransaction: 0 - income, 1 expense,
                RadioButton checkedRadioButton = getView().findViewById(chooseTransactionType.getCheckedRadioButtonId());
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

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(transactionDate);
                int monthOfUpdatedTransaction = calendar.get(Calendar.MONTH);
                int yearOfUpdatedTransaction = calendar.get(Calendar.YEAR);
                double primaryValueOfUpdatedTransaction = selectedTransaction.getAmount();

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
                        if (isDailyLimitExceeded && isMonthlyLimitExceeded) {
                            builder.setMessage(R.string.limit_warning_daily_and_monthly_message);
                        } else if (isDailyLimitExceeded) {
                            builder.setMessage(R.string.limit_warning_daily_message);
                        } else {
                            builder.setMessage(R.string.limit_warning_monthly_message);
                        }
                        builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> {
                            transactionRepository.updateTransaction(transactionToSave);
                            modifyTransactionDialogCommunicator.ModifyTransactionCallback(TRANSACTION_UPDATED);
                            dismiss();
                        });

                        builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                            dismiss();
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        return;
                    }
                }
                transactionRepository.updateTransaction(transactionToSave);
                modifyTransactionDialogCommunicator.ModifyTransactionCallback(TRANSACTION_UPDATED);
                dismiss();
                break;
            }

            case R.id.saveNewTransaction: {

                Transaction transactionToSave = new Transaction();

                //check if name field is empty, if so we set it as category name
                String transactionName = chooseTransactionName.getText().toString();
                if (transactionName.equals("")) {
                    transactionName = chooseTransactionCategory.getSelectedItem().toString();
                }

                float transactionAmount;
                try {
                    transactionAmount = Float.parseFloat(chooseTransactionAmount.getText().toString());
                    if (transactionAmount == 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                    builder.setCancelable(false);
                    builder.setMessage(R.string.incorrect_value_message);
                    builder.setPositiveButton(R.string.OK, (dialog, which) -> {
                    });
                    android.app.AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    break;
                }

                //check there typeOfTransaction: 0 - income, 1 expense,
                RadioButton checkedRadioButton = getView().findViewById(chooseTransactionType.getCheckedRadioButtonId());
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
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                        builder.setCancelable(false);
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
                                modifyTransactionDialogCommunicator.ModifyTransactionCallback(TRANSACTION_ADDED_NEEDED_UPDATE_LIST);
                            }
                            dismiss();
                        });

                        builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                        });
                        android.app.AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        break;
                    }
                }
                transactionRepository.create(transactionToSave);
                //check if we need to actualise current seen list of transactions and total sum
                if ((monthOfNewTransaction == currentChosenMonth) && (yearOfNewTransaction == currentChosenYear)) {
                    modifyTransactionDialogCommunicator.ModifyTransactionCallback(TRANSACTION_ADDED_NEEDED_UPDATE_LIST);
                }
                modifyTransactionDialogCommunicator.ModifyTransactionCallback(TRANSACTION_ADDED_NO_NEED_TO_UPDATE_LIST);
                dismiss();
                break;
            }

            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.chooseTransactionType) {
            int idOfCheckedRadiobutton = chooseTransactionType.getCheckedRadioButtonId();
            RadioButton checkedRadiobutton = getView().findViewById(idOfCheckedRadiobutton);
            String chosenTransactionTypeName = checkedRadiobutton.getText().toString();

            ArrayAdapter<String> chooseCategoryAdapter;
            if (chosenTransactionTypeName.equals("Expense")) {
                chooseCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_categories, getResources().getStringArray(R.array.list_of_expenses));
            } else {
                chooseCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_categories, getResources().getStringArray(R.array.list_of_incomes));
            }
            chooseTransactionCategory.setAdapter(chooseCategoryAdapter);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.chooseTransactionCategory) {
            String transactionTypeName = chooseTransactionCategory.getSelectedItem().toString().toLowerCase().replace(" ", "_");
            int idOfSuitableDrawableForTransactionTypeName = requireContext().getResources().getIdentifier(transactionTypeName, "drawable", getContext().getPackageName());
            transactionCategoryImage.setBackgroundResource(idOfSuitableDrawableForTransactionTypeName);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface ModifyTransactionDialogCommunicator {
        void ModifyTransactionCallback(int typeOfOperation);

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                int transactionId = selectedTransaction.getId();
                String transactionName = chooseTransactionName.getText().toString();
                RadioButton checkedRadioButton = getView().findViewById(chooseTransactionType.getCheckedRadioButtonId());
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
                    transactionAmount = -transactionAmount;
                }
                String transactionCategory = chooseTransactionCategory.getSelectedItem().toString();
                Date transactionDate = convertStringToDate(chooseTransactionDate.getText().toString());
                Transaction transactionAtMomentOfClosingPopup = new Transaction(transactionId, transactionType, transactionName, transactionAmount, transactionCategory, transactionDate);

                if (!selectedTransaction.equals(transactionAtMomentOfClosingPopup)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(false);
                    builder.setMessage(R.string.unsaved_data_message);
                    builder.setPositiveButton(R.string.limit_yes_button, (dialog, which) -> dismiss());
                    builder.setNegativeButton(R.string.limit_no_button, (dialog, which) -> {
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    super.onBackPressed();
                }
            }
        };
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
}
