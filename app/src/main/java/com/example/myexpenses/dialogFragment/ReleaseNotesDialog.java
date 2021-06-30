package com.example.myexpenses.dialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myexpenses.R;

public class ReleaseNotesDialog extends DialogFragment implements View.OnClickListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);
        return inflater.inflate(R.layout.dialog_release_notes, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView toolbarText = view.findViewById(R.id.toolbarText);
        toolbarText.setText("Release notes");

        ImageButton toolbarClose = view.findViewById(R.id.toolbarClose);
        toolbarClose.setOnClickListener(this);

        Button OK = view.findViewById(R.id.OK);
        OK.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarClose:
            case R.id.OK: {
                dismiss();
                break;
            }
            default:
                break;
        }
    }
}
