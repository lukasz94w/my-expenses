package com.example.myexpenses.customValueFormatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class IntValueFormatter extends ValueFormatter {

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return String.valueOf((int) value);
    }

    @Override
    public String getFormattedValue(float value) {
        return String.valueOf((int) value);
    }
}