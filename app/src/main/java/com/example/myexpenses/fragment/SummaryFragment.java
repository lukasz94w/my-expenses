package com.example.myexpenses.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.myexpenses.R;
import com.example.myexpenses.model.Transaction;
import com.example.myexpenses.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SummaryFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Spinner choosePeriodSpinner;
    private TextView expensesAmount, incomesAmount, summariesAmount, dbReadedRecords;
    private TransactionRepository transactionRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        //before initializing spinner because it uses IncomeRepository and ExpenseRepository instances
        transactionRepository = new TransactionRepository(this.getActivity());

        choosePeriodSpinner = view.findViewById(R.id.choosePeriodSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, getListOfLastSixMonths());
        choosePeriodSpinner.setAdapter(adapter);
        choosePeriodSpinner.setOnItemSelectedListener(this);

        expensesAmount = view.findViewById(R.id.expensesAmount);
        incomesAmount = view.findViewById(R.id.incomesAmount);
        summariesAmount = view.findViewById(R.id.summariesAmount);
        dbReadedRecords = view.findViewById(R.id.dbReadedRecords);

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.choosePeriodSpinner: {
                String chosenItem = choosePeriodSpinner.getSelectedItem().toString();
                List<Date> chosenPeriod = parseDatesFromSpinner(chosenItem);

                Long periodStart = chosenPeriod.get(0).getTime();
                Long periodEnd = chosenPeriod.get(1).getTime();

                List<Transaction> listOfIncomes = transactionRepository.findIncomesBetweenDates(periodStart, periodEnd);
                List<Transaction> listOfExpenses = transactionRepository.findExpensesBetweenDates(periodStart, periodEnd);
                double sumOfIncomes = listOfIncomes.stream()
                        .mapToDouble(o->o.getAmount())
                        .sum();
                double sumOfExpenses = listOfExpenses.stream()
                        .mapToDouble(o->o.getAmount())
                        .sum();

                printIncomesAndExpenses(listOfExpenses, listOfIncomes, sumOfExpenses, sumOfIncomes);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public List<String> getListOfLastSixMonths() {
        ArrayList<String> listOfMonths = new ArrayList<>();

        for (int i = 0; i <= 5; i++) {
            int calculatedMonth = Calendar.getInstance().get(Calendar.MONTH) - i + 1; // +1 because months are indexed starting at 0
            int calculatedYear = Calendar.getInstance().get(Calendar.YEAR);
            if (calculatedMonth <= 0) {
                calculatedYear = calculatedYear - 1;
                calculatedMonth = calculatedMonth + 12;
            }

            if (calculatedMonth == 1 || calculatedMonth == 3 || calculatedMonth == 5 || calculatedMonth == 7 || calculatedMonth == 8 || calculatedMonth == 10 || calculatedMonth == 12) {
                if (calculatedMonth >= 10) {
                    listOfMonths.add("01/" + calculatedMonth + "/" + calculatedYear + " do 31/" + calculatedMonth + "/" + calculatedYear);
                } else {
                    listOfMonths.add("01/0" + calculatedMonth + "/" + calculatedYear + " do 31/0" + calculatedMonth + "/" + calculatedYear);
                }
            } else if (calculatedMonth == 2) {
                //check if year is leap
                if (((calculatedYear % 4 == 0) && (calculatedYear % 100 != 0) || (calculatedYear % 400 == 0))) {
                    listOfMonths.add("01/0" + calculatedMonth + "/" + calculatedYear + " do 29/0" + calculatedMonth + "/" + calculatedYear);
                } else {
                    listOfMonths.add("01/0" + calculatedMonth + "/" + calculatedYear + " do 28/0" + calculatedMonth + "/" + calculatedYear);
                }
            } else {
                if (calculatedMonth >= 10) {
                    listOfMonths.add("01/" + calculatedMonth + "/" + calculatedYear + " do 30/" + calculatedMonth + "/" + calculatedYear);
                } else {
                    listOfMonths.add("01/0" + calculatedMonth + "/" + calculatedYear + " do 30/0" + calculatedMonth + "/" + calculatedYear);
                }
            }
        }
        return listOfMonths;
    }

    public List<Date> parseDatesFromSpinner(String selectedSpinnerItem) {
        List<Date> dateFromAndDateTo = new ArrayList<>();
        String[] spinnerParts = selectedSpinnerItem.split("/| do ");

        Calendar startOfChosenPeriod = Calendar.getInstance();
        Calendar endOfChosenPeriod = Calendar.getInstance();

        startOfChosenPeriod.set(Calendar.MILLISECOND, 0);
        startOfChosenPeriod.set(Calendar.SECOND, 0);
        startOfChosenPeriod.set(Calendar.MINUTE, 0);
        startOfChosenPeriod.set(Calendar.HOUR_OF_DAY, 0);
        startOfChosenPeriod.set(Calendar.DAY_OF_MONTH, Integer.parseInt(spinnerParts[0]));
        startOfChosenPeriod.set(Calendar.MONTH, Integer.parseInt(spinnerParts[1]) - 1);
        startOfChosenPeriod.set(Calendar.YEAR, Integer.parseInt(spinnerParts[2]));
        dateFromAndDateTo.add(startOfChosenPeriod.getTime());

        endOfChosenPeriod.set(Calendar.MILLISECOND, 999);
        endOfChosenPeriod.set(Calendar.SECOND, 59);
        endOfChosenPeriod.set(Calendar.MINUTE, 59);
        endOfChosenPeriod.set(Calendar.HOUR_OF_DAY, 23);
        endOfChosenPeriod.set(Calendar.DAY_OF_MONTH, Integer.parseInt(spinnerParts[3]));
        endOfChosenPeriod.set(Calendar.MONTH, Integer.parseInt(spinnerParts[4]) - 1);
        endOfChosenPeriod.set(Calendar.YEAR, Integer.parseInt(spinnerParts[5]));
        dateFromAndDateTo.add(endOfChosenPeriod.getTime());

        return dateFromAndDateTo;
    }

    public void printIncomesAndExpenses(List<Transaction> listOfExpenses, List<Transaction> listOfIncomes, double sumOfExpenses, double sumOfIncomes) {
        expensesAmount.setText(String.valueOf(sumOfExpenses));
        incomesAmount.setText(String.valueOf(sumOfIncomes));
        summariesAmount.setText(String.valueOf(sumOfExpenses - sumOfIncomes));

        StringBuilder dbReadedRecordsBuilder = new StringBuilder();
        dbReadedRecordsBuilder.append("INCOMES: \n");
        for (Transaction transaction : listOfIncomes) {
            String log = "Id: " + transaction.getId() + " || Name: " + transaction.getName() + " || Kwota: " + transaction.getAmount() + " || Date: " + transaction.getDate();
            dbReadedRecordsBuilder.append(log + "\n");
        }
        dbReadedRecordsBuilder.append("\n");

        dbReadedRecordsBuilder.append("EXPENSES: \n");
        for (Transaction transaction : listOfExpenses) {
            String log = "Id: " + transaction.getId() + " || Name: " + transaction.getName() + " || Kwota: " + transaction.getAmount() + " || Category: " + transaction.getCategory() + " || Date: " + transaction.getDate();
            dbReadedRecordsBuilder.append(log + "\n");
        }
        dbReadedRecordsBuilder.toString();
        dbReadedRecords.setText(dbReadedRecordsBuilder);
    }

    //TEST METHODS
    public Date returnDate(String date) {
        final String userInput = date;
        final String[] timeParts = userInput.split("\\.");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeParts[0]));
        cal.set(Calendar.MONTH, Integer.parseInt(timeParts[1]) - 1);
        cal.set(Calendar.YEAR, Integer.parseInt(timeParts[2]));
        final Date myDate = cal.getTime();
        return myDate;
    }

//    public void deleteAllRecords() {
//        incomeRepository.deleteAllRecords();
//        expenseRepository.deleteAllRecords();
//    }
//
//    public void createIncomes() {
//        incomeRepository.create(new Income("Wypłata za październik", 3310, returnDate("08.10.2020")));
//        incomeRepository.create(new Income("Wypłata za listopad", 3411, returnDate("07.11.2020")));
//        incomeRepository.create(new Income("Wypłata za grudzień", 4112, returnDate("07.12.2020")));
//        incomeRepository.create(new Income("Premia na święta", 500, returnDate("14.12.2020")));
//        incomeRepository.create(new Income("Wypłata za styczeń", 3501, returnDate("07.01.2021")));
//        incomeRepository.create(new Income("Wypłata za luty", 3402, returnDate("06.02.2021")));
//        incomeRepository.create(new Income("Wypłata za marzec", 3803, returnDate("09.03.2021")));
//    }
//
//    public void createExpensesPart1() {
//        //OCTOBER
//        expenseRepository.create(new Expense("Pizza", 45, "Jedzenie", returnDate("15.10.2020")));
//        expenseRepository.create(new Expense("Tankowanie", 100, "Samochód", returnDate("15.10.2020")));
//        expenseRepository.create(new Expense("CD Action", 11, "Gry", returnDate("17.10.2020")));
//        expenseRepository.create(new Expense("Przegląd roweru", 150, "Sport", returnDate("19.10.2020")));
//
//        //NOVEMBER
//        expenseRepository.create(new Expense("Wycieczka Wawka", 45, "Podróże", returnDate("02.11.2020")));
//        expenseRepository.create(new Expense("Fifa 2021", 190, "Gry", returnDate("07.11.2020")));
//        expenseRepository.create(new Expense("CD Action", 11, "Gry", returnDate("09.11.2020")));
//        expenseRepository.create(new Expense("Delegacja na Słowację", 250, "Podróże", returnDate("21.11.2020")));
//
//        //DECEMBER
//        expenseRepository.create(new Expense("Prezent na Boże Narodziny mama", 155, "Inne", returnDate("03.12.2020")));
//        expenseRepository.create(new Expense("Prezent na Boże Narodziny ojciec", 135, "Inne", returnDate("03.12.2020")));
//        expenseRepository.create(new Expense("Mikołajki PLUM", 135, "Inne", returnDate("06.12.2020")));
//        expenseRepository.create(new Expense("Wymiana lusterka YARIS", 200, "Samochód", returnDate("12.12.2020")));
//    }
//
//    public void createExpensesPart2() {
//        //JANUARY
//        expenseRepository.create(new Expense("Sylwester", 300, "Inne", returnDate("01.01.2021")));
//        expenseRepository.create(new Expense("Happy Meal w macu", 20, "Jedzenie", returnDate("09.01.2021")));
//        expenseRepository.create(new Expense("Pizza", 35, "Jedzenie", returnDate("20.01.2021")));
//        expenseRepository.create(new Expense("Tankowanie", 150, "Samochód", returnDate("21.01.2021")));
//        expenseRepository.create(new Expense("Zakupy w LIDLu", 55, "Jedzenie", returnDate("22.01.2021")));
//
//        //FEBRUARY
//        expenseRepository.create(new Expense("Spodnie do biegania", 150, "Sport", returnDate("03.02.2021")));
//        expenseRepository.create(new Expense("Koszula galowa", 70, "Ubrania", returnDate("09.02.2021")));
//        expenseRepository.create(new Expense("Pizza", 35, "Jedzenie", returnDate("12.02.2021")));
//        expenseRepository.create(new Expense("Tankowanie", 90, "Samochód", returnDate("18.02.2021")));
//        expenseRepository.create(new Expense("Zakupy w LIDLu", 78, "Jedzenie", returnDate("22.02.2021")));
//
//        //MARCH
//        expenseRepository.create(new Expense("Buty do biegania", 350, "Sport", returnDate("02.03.2021")));
//        expenseRepository.create(new Expense("Opaska Xiaomi", 70, "Sport", returnDate("09.03.2021")));
//        expenseRepository.create(new Expense("DOOM", 95, "Gry", returnDate("10.03.2021")));
//        expenseRepository.create(new Expense("Tankowanie", 120, "Samochód", returnDate("14.03.2021")));
//        expenseRepository.create(new Expense("Zakupy w LIDLu", 95, "Jedzenie", returnDate("22.03.2021")));
//    }
}