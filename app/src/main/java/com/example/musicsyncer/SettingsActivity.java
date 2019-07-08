package com.example.musicsyncer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //find and set toolbar
        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        //set toolbar back button and title
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //populate container with preferences
        if(findViewById(R.id.fragment_container) != null) {
            if(savedInstanceState != null){
                return;
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new SettingsFragment())
                    .commit();
        }

    }

}

