package com.example.myexpenses.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myexpenses.R;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private Button contactWithAuthor, rateApp, releaseNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.about));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactWithAuthor = findViewById(R.id.contactWithAuthor);
        contactWithAuthor.setOnClickListener(this);
        rateApp = findViewById(R.id.rateApp);
        rateApp.setOnClickListener(this);
        releaseNotes = findViewById(R.id.releaseNotes);
        releaseNotes.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contactWithAuthor:
            case R.id.rateApp:
            case R.id.releaseNotes: {
                Toast.makeText(AboutActivity.this, "Function is not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                break;
        }
    }

    //prevent left checked icon on navigation drawer and also clear filters f.e.
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

}