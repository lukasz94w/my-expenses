package com.example.myexpenses.other;

public class ModifyTransactionDialogCallbackData {

    private final String typeOfOperation;
    private final int monthOffset;

    public ModifyTransactionDialogCallbackData(String typeOfOperation, int monthOffset) {
        this.typeOfOperation = typeOfOperation;
        this.monthOffset = monthOffset;
    }

    public String getTypeOfOperation() {
        return typeOfOperation;
    }

    public int getMonthOffset() {
        return monthOffset;
    }
}
