package com.example.myexpenses.dialogFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import com.example.myexpenses.inputFilter.DecimalDigitsInputFilter;
import com.example.myexpenses.R;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myexpenses.other.CurrencyConverter.getValueInSubUnit;

public class AddNewTransactionDialog extends DialogFragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private int actualDay;
    private int actualMonth;
    private int actualYear;
    private Date actualDate;

    private Integer currentChosenMonth;
    private Integer currentChosenYear;

    private int dailyLimit;
    private int monthlyLimit;

    private Button chooseTransactionDate;
    private Button chooseTodayTomorrow;
    private RadioGroup chooseTransactionType;
    private ImageView transactionCategoryImage;
    private Spinner chooseTransactionCategory;
    private EditText chooseTransactionAmount;
    private EditText chooseTransactionName;

    private TransactionRepository transactionRepository;

    private AddNewTransactionDialogCallback addNewTransactionDialogCallback;

    private boolean sharedPrefShouldUseCategoryNameIfNoteIsEmpty;


    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            addNewTransactionDialogCallback = (AddNewTransactionDialogCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Activity must implement AddNewTransactionDialog.AddNewTransactionDialogCallback");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transactionRepository = new TransactionRepository(getContext());

        actualDay = getArguments().getInt("actualDay");
        actualMonth = getArguments().getInt("actualMonth");
        actualYear = getArguments().getInt("actualYear");
        currentChosenMonth = getArguments().getInt("currentChosenMonth");
        currentChosenYear = getArguments().getInt("currentChosenYear");
        dailyLimit = getArguments().getInt("dailyLimit");
        monthlyLimit = getArguments().getInt("monthlyLimit");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(currentChosenYear, currentChosenMonth, actualDay, 0, 0, 0);
        actualDate = new Date();
        actualDate.setTime(calendar.getTime().getTime());

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        //default value if it wasn't initialized yet
        sharedPrefShouldUseCategoryNameIfNoteIsEmpty = sharedPreferences.getBoolean("sharedPrefShouldUseCategoryNameIfNoteIsEmpty", true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);
        return inflater.inflate(R.layout.dialog_new_transaction, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView toolbarText = view.findViewById(R.id.toolbarText);
        toolbarText.setText("Add record");

        SimpleDateFormat simpledateformat = new SimpleDateFormat("EE");
        String currentDayOfWeek = simpledateformat.format(actualDate);
        String currentDateAsString = convertDateToString(actualDay, currentChosenMonth + 1, currentChosenYear, currentDayOfWeek);

        chooseTransactionDate = view.findViewById(R.id.chooseTransactionDate);
        chooseTransactionDate.setText(currentDateAsString);
        chooseTransactionDate.setOnClickListener(this);

        chooseTodayTomorrow = view.findViewById(R.id.chooseTodayTomorrow);
        chooseTodayTomorrow.setOnClickListener(this);
        if (currentChosenMonth != actualMonth || currentChosenYear != actualYear) {
            chooseTodayTomorrow.setText("Today");
        }

        chooseTransactionType = view.findViewById(R.id.chooseTransactionType);
        chooseTransactionType.setOnCheckedChangeListener(this);

        transactionCategoryImage = view.findViewById(R.id.transactionCategoryImage);

        chooseTransactionCategory = view.findViewById(R.id.chooseTransactionCategory);
        ArrayAdapter<String> chooseCategoryAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_categories, getResources().getStringArray(R.array.list_of_expenses));
        chooseTransactionCategory.setAdapter(chooseCategoryAdapter);
        chooseTransactionCategory.setOnItemSelectedListener(this);

        chooseTransactionAmount = view.findViewById(R.id.chooseTransactionAmount);
        chooseTransactionAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 2, 100000)});
        chooseTransactionAmount.setOnTouchListener(this);
        chooseTransactionAmount.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        chooseTransactionName = view.findViewById(R.id.chooseTransactionName);
        chooseTransactionName.setOnTouchListener(this);

        Button saveNewTransaction = view.findViewById(R.id.saveNewTransaction);
        saveNewTransaction.setOnClickListener(this);

        ImageButton toolbarClose = view.findViewById(R.id.toolbarClose);
        toolbarClose.setOnClickListener(this);
    }

    public interface AddNewTransactionDialogCallback {
        void addNewTransactionDialogCallback(int monthOffset);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarClose: {
                if (!chooseTransactionAmount.getText().toString().equals("") || !chooseTransactionName.getText().toString().equals("")) {
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

            case R.id.saveNewTransaction: {

                Transaction transactionToSave = new Transaction();

                String transactionName = chooseTransactionName.getText().toString();

                //check if name field is empty, if so we set it as category name
                if (sharedPrefShouldUseCategoryNameIfNoteIsEmpty) {
                    if (transactionName.equals("")) {
                        transactionName = chooseTransactionCategory.getSelectedItem().toString();
                    }
                }

                int transactionAmount;
                try {
                    float transactionAmountAsFloat = Float.parseFloat(chooseTransactionAmount.getText().toString());
                    transactionAmount = getValueInSubUnit(transactionAmountAsFloat);
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
                                addNewTransactionDialogCallback.addNewTransactionDialogCallback(0);
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
                    addNewTransactionDialogCallback.addNewTransactionDialogCallback(0);
                } else {
                    addNewTransactionDialogCallback.addNewTransactionDialogCallback(getMonthOffset(monthOfNewTransaction, yearOfNewTransaction));
                }
                dismiss();
                break;
            }
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private int getMonthOffset(int monthOfNewTransaction, int yearOfNewTransaction) {

        LocalDate newTransactionDate = LocalDate.of(yearOfNewTransaction, monthOfNewTransaction, 1);
        LocalDate currentChosenMonthInList = LocalDate.of(currentChosenYear, currentChosenMonth, 1);

        Period diff = Period.between(newTransactionDate, currentChosenMonthInList);

        return diff.getMonths();
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
                if (!chooseTransactionAmount.getText().toString().equals("") || !chooseTransactionName.getText().toString().equals("")) {
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
}
