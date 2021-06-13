package com.example.myexpenses.dialogFragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.myexpenses.R;
import com.example.myexpenses.inputFilter.DecimalDigitsInputFilter;

import java.util.Locale;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;
import static com.example.myexpenses.other.CurrencyConverter.getValueInSubUnit;

public class EditLimitsDialog extends DialogFragment implements View.OnClickListener, View.OnTouchListener {

    private int dailyLimit;
    private int monthlyLimit;
    private EditText setDailyLimit;
    private EditText setMonthlyLimit;
    private EditLimitsDialogCommunicator editLimitsDialogCommunicator;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            editLimitsDialogCommunicator = (EditLimitsDialogCommunicator) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement EditLimitsDialog.EditLimitsDialogCommunicator");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dailyLimit = getArguments().getInt("dailyLimit");
        monthlyLimit = getArguments().getInt("monthlyLimit");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);
        return inflater.inflate(R.layout.dialog_edit_limits, container);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.tool_bar);
        ImageButton toolbarClose = view.findViewById(R.id.toolbarClose);
        toolbarClose.setOnClickListener(this);

        TextView toolbarText = view.findViewById(R.id.toolbarText);
        toolbarText.setText("Set limits");

        setDailyLimit = view.findViewById(R.id.setDailyLimit);
        setDailyLimit.setText(String.format(Locale.US, "%.2f", getValueInCurrency(dailyLimit)));
        setDailyLimit.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,2, 10000)});
        setDailyLimit.setOnTouchListener(this);

        setMonthlyLimit = view.findViewById(R.id.setMonthlyLimit);
        setMonthlyLimit.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(6,2, 100000)});
        setMonthlyLimit.setText(String.format(Locale.US, "%.2f", getValueInCurrency(monthlyLimit)));
        setMonthlyLimit.setOnTouchListener(this);

        Button saveNewLimits = view.findViewById(R.id.saveNewLimits);
        saveNewLimits.setOnClickListener(this);
    }

    public interface EditLimitsDialogCommunicator {
        void retrieveDataFromEditLimitsDialog(final Bundle data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarClose: {
                int valueOfSetDailyLimit;
                try {
                    valueOfSetDailyLimit = getValueInSubUnit(Float.parseFloat(setDailyLimit.getText().toString()));
                } catch (NumberFormatException e) {
                    valueOfSetDailyLimit = 0;
                }

                int valueOfSetMonthlyLimit;
                try {
                    valueOfSetMonthlyLimit = getValueInSubUnit(Float.parseFloat(setMonthlyLimit.getText().toString()));
                } catch (NumberFormatException e) {
                    valueOfSetMonthlyLimit = 0;
                }

                if (!(valueOfSetDailyLimit == dailyLimit) || !(valueOfSetMonthlyLimit == monthlyLimit)) {
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
            case R.id.saveNewLimits: {

                Bundle bundle = new Bundle();

                //check if amount is empty if so we set it as zero
                try {
                    dailyLimit = getValueInSubUnit(Float.parseFloat(setDailyLimit.getText().toString()));
                } catch (NumberFormatException e) {
                    dailyLimit = 0;
                }
                //check if amount is empty if so we set it as zero
                try {
                    monthlyLimit = getValueInSubUnit(Float.parseFloat(setMonthlyLimit.getText().toString()));
                } catch (NumberFormatException e) {
                    monthlyLimit = 0;
                }

                bundle.putInt("dailyLimit", dailyLimit);
                bundle.putInt("monthlyLimit", monthlyLimit);
                editLimitsDialogCommunicator.retrieveDataFromEditLimitsDialog(bundle);
                dismiss();

                break;
            }
            default:
                break;
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                int valueOfSetDailyLimit;
                try {
                    valueOfSetDailyLimit = getValueInSubUnit(Float.parseFloat(setDailyLimit.getText().toString()));
                } catch (NumberFormatException e) {
                    valueOfSetDailyLimit = 0;
                }

                int valueOfSetMonthlyLimit;
                try {
                    valueOfSetMonthlyLimit = getValueInSubUnit(Float.parseFloat(setMonthlyLimit.getText().toString()));
                } catch (NumberFormatException e) {
                    valueOfSetMonthlyLimit = 0;
                }

                if (!(valueOfSetDailyLimit == dailyLimit) || !(valueOfSetMonthlyLimit == monthlyLimit)) {
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.setDailyLimit: {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (setDailyLimit.getRight() - setDailyLimit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        setDailyLimit.setText("");
                        v.performClick();
                        return false;
                    }
                }
                break;
            }
            case R.id.setMonthlyLimit: {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (setMonthlyLimit.getRight() - setMonthlyLimit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        setMonthlyLimit.setText("");
                        v.performClick();
                        return false;
                    }
                }
            }
            break;
        }
        return false;
    }
}

