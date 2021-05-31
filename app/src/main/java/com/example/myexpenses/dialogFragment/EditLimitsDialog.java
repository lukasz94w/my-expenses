package com.example.myexpenses.dialogFragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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

public class EditLimitsDialog extends DialogFragment implements View.OnClickListener, View.OnTouchListener {

    private float dailyLimit;
    private float monthlyLimit;
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

        dailyLimit = getArguments().getFloat("dailyLimit");
        monthlyLimit = getArguments().getFloat("monthlyLimit");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);
        return inflater.inflate(R.layout.dialog_edit_limits, container);
    }

    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.tool_bar);
        ImageButton toolbarClose = view.findViewById(R.id.toolbarClose);
        toolbarClose.setOnClickListener(this);

        TextView toolbarText = view.findViewById(R.id.toolbarText);
        toolbarText.setText("Set limits");

        setDailyLimit = view.findViewById(R.id.setDailyLimit);
        setDailyLimit.setText(String.format("%.2f", dailyLimit));
        setDailyLimit.setOnTouchListener(this);

        setMonthlyLimit = view.findViewById(R.id.setMonthlyLimit);
        setMonthlyLimit.setText(String.format("%.2f", monthlyLimit));
        setMonthlyLimit.setOnTouchListener(this);

        Button saveNewLimits = view.findViewById(R.id.saveNewLimits);
        saveNewLimits.setOnClickListener(this);
    }

    public interface EditLimitsDialogCommunicator {
        void retrieveData(final Bundle data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarClose: {
                Float valueOfSetDailyLimit;
                try {
                    valueOfSetDailyLimit = Float.valueOf(setDailyLimit.getText().toString());
                } catch (NumberFormatException e) {
                    valueOfSetDailyLimit = 0f;
                }

                Float valueOfSetMonthlyLimit;
                try {
                    valueOfSetMonthlyLimit = Float.valueOf(setMonthlyLimit.getText().toString());
                } catch (NumberFormatException e) {
                    valueOfSetMonthlyLimit = 0f;
                }

                if (!valueOfSetDailyLimit.equals(dailyLimit) || !valueOfSetMonthlyLimit.equals(monthlyLimit)) {
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
                    dailyLimit = Float.parseFloat(String.valueOf(setDailyLimit.getText()));
                } catch (NumberFormatException e) {
                    dailyLimit = 0f;
                }
                //check if amount is empty if so we set it as zero
                try {
                    monthlyLimit = Float.parseFloat(String.valueOf(setMonthlyLimit.getText()));
                } catch (NumberFormatException e) {
                    monthlyLimit = 0f;
                }

                bundle.putFloat("dailyLimit", dailyLimit);
                bundle.putFloat("monthlyLimit", monthlyLimit);
                editLimitsDialogCommunicator.retrieveData(bundle);
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
                Float valueOfSetDailyLimit;
                try {
                    valueOfSetDailyLimit = Float.valueOf(setDailyLimit.getText().toString());
                } catch (NumberFormatException e) {
                    valueOfSetDailyLimit = 0f;
                }

                Float valueOfSetMonthlyLimit;
                try {
                    valueOfSetMonthlyLimit = Float.valueOf(setMonthlyLimit.getText().toString());
                } catch (NumberFormatException e) {
                    valueOfSetMonthlyLimit = 0f;
                }

                if (!valueOfSetDailyLimit.equals(dailyLimit) || !valueOfSetMonthlyLimit.equals(monthlyLimit)) {
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

