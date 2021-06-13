package com.example.myexpenses.other;

import com.example.myexpenses.model.Transaction;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TransactionsProvider {

    public static List<Transaction> getListOfTestTransactions() {
        List<Transaction> transactionList = new LinkedList<>();

        //INCOMES
        transactionList.add(new Transaction(0, "Wypłata za październik", (new Random().nextInt(50000000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
        transactionList.add(new Transaction(0, "Wypłata za listopad", (new Random().nextInt(500000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
        transactionList.add(new Transaction(0, "Wypłata za grudzień", (new Random().nextInt(500000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(0, "Premia na święta", (new Random().nextInt(500000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(0, "Wypłata za styczeń", (new Random().nextInt(500000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(0, "Wypłata za luty", (new Random().nextInt(500000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".02.2020")));
        transactionList.add(new Transaction(0, "Wypłata za marzec", (new Random().nextInt(500000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(0, "Wypłata za kwiecień", (new Random().nextInt(500000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(0, "Wypłata za maj", (new Random().nextInt(500000)), "Payment", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));

        //OCTOBER
        transactionList.add(new Transaction(1, "Pizza", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
        transactionList.add(new Transaction(1, "Trankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
        transactionList.add(new Transaction(1, "CD Action", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
        transactionList.add(new Transaction(1, "Przegląd roweru", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));
        transactionList.add(new Transaction(1, "Karta graficzna", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".10.2020")));

        //NOVEMBER
        transactionList.add(new Transaction(1, "Wycieczka Wawka", new Random().nextInt(25000) - 25000, "Hobby", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
        transactionList.add(new Transaction(1, "Fifa 2021", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
        transactionList.add(new Transaction(1, "CD Action", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));
        transactionList.add(new Transaction(1, "Delegacja na Słowację", new Random().nextInt(25000) - 25000, "Hobby", returnDate(new Random().nextInt(28) + 1 + ".11.2020")));

        //DECEMBER
        transactionList.add(new Transaction(1, "Prezent na Boże Narodziny mama", new Random().nextInt(25000) - 25000, "Other", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Prezent na Boże Narodziny ojciec", new Random().nextInt(25000) - 25000, "Other", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));

        transactionList.add(new Transaction(1, "Mikołajki PLUM", new Random().nextInt(25000) - 25000, "Other", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));
        transactionList.add(new Transaction(1, "Wymiana lusterka YARIS", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".12.2020")));

        //JANUARY
        transactionList.add(new Transaction(1, "Sylwester", new Random().nextInt(25000) - 25000, "Other", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Happy Meal w macu", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Pizza", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".01.2021")));

        //FEBRUARY
        transactionList.add(new Transaction(1, "Spodnie do biegania", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Koszula galowa", new Random().nextInt(25000) - 25000, "Other", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Pizza", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".02.2021")));

        //MARCH
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567999999999", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567890", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Buty do biegania", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Opaska Xiaomi", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "DOOM", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".03.2021")));

        //APRIL
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567999999999", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567890", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Buty do biegania", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Opaska Xiaomi", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "DOOM", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".04.2021")));

        //MAY
        transactionList.add(new Transaction(1, "Impreza urodzinowa prezent", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567890", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Pizza", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Imprezka", new Random().nextInt(25000) - 25000, "Other", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Buty do biegania", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Opaska Xiaomi", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "DOOM", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));
        transactionList.add(new Transaction(1, "Prezent na komunię", new Random().nextInt(25000) - 25000, "Other", returnDate(new Random().nextInt(28) + 1 + ".05.2021")));

        //JUNE
        transactionList.add(new Transaction(1, "Impreza urodzinowa prezent", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Bardzo długi wpis 1234567890", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Pizza", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Imprezka", new Random().nextInt(25000) - 25000, "Other", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Buty do biegania", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Opaska Xiaomi", new Random().nextInt(25000) - 25000, "Sport", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "DOOM", new Random().nextInt(25000) - 25000, "Game", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Tankowanie", new Random().nextInt(25000) - 25000, "Car", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Jedzenie", new Random().nextInt(25000) - 25000, "Food", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));
        transactionList.add(new Transaction(1, "Prezent na komunię", new Random().nextInt(25000) - 25000, "Other", returnDate(new Random().nextInt(28) + 1 + ".06.2021")));

        return transactionList;
    }

    private static Date returnDate(String date) {
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
}
