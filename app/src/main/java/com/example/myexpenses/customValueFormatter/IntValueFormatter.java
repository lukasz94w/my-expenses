package com.example.myexpenses.customValueFormatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

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