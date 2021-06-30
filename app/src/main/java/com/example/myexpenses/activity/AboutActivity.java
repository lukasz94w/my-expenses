package com.example.myexpenses.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.example.myexpenses.R;
import com.example.myexpenses.dialogFragment.ReleaseNotesDialog;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.about));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button contactWithAuthor = findViewById(R.id.contactWithAuthor);
        contactWithAuthor.setOnClickListener(this);
        Button rateApp = findViewById(R.id.rateApp);
        rateApp.setOnClickListener(this);
        Button releaseNotes = findViewById(R.id.releaseNotes);
        releaseNotes.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contactWithAuthor:
            case R.id.rateApp: {
                Toast.makeText(this, "Function not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.releaseNotes: {

                FragmentManager fragmentManager = getSupportFragmentManager();
                ReleaseNotesDialog releaseNotesDialog = new ReleaseNotesDialog();
                releaseNotesDialog.show(fragmentManager, "ReleaseNotesDialog");

                break;
            }
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}