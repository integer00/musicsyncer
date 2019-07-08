package com.example.musicsyncer;

/*
##find correct sdcard

                        (when someone connected shows like $ip is connected)

#verbose button for stacktrace?
##Add background job for bg service

#find this fucking sd card
#tune settings activity
# knobs for ext\int
#scroll down for new messages



 */


import android.Manifest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.lib.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class MainActivity extends AppCompatActivity {

    TextView txtStatus, messages_txtBody;

    private Toolbar myToolbar;

    // Storage Permissions
    private static final int MY_PERMISSIONS_REQUEST = 1;

    private Path rootDir;
    private String ipAddress;
    private MyServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //set toolbar
        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //check and restore config
        ipAddress = sharedPreferences.getString("ip", "");

        if(sharedPreferences.getString("webroot","").equals("internal")){
            rootDir = getApplicationHomeFolder("internal").toPath();
        } else if (sharedPreferences.getString("webroot","").equals("external")){
            rootDir = getApplicationHomeFolder("external").toPath();
        } else {
            //leave as printed
            rootDir = Paths.get(sharedPreferences.getString("webroot",""));
        }

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        messages_txtBody = (TextView) findViewById(R.id.messages_txtBody);
        messages_txtBody.setMovementMethod(new ScrollingMovementMethod());


        Button start_btn = findViewById(R.id.start_btn);
        Button stop_btn = findViewById(R.id.stop_btn);

        start_btn.setOnClickListener(startServer);
        stop_btn.setOnClickListener(stopServer);


        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        }, MY_PERMISSIONS_REQUEST);

        //prepare server
        server = new MyServer(ipAddress, 8080, rootDir);

        //get event message
        server.setEventListener(new MyServer.EventListener() {

            @Override
            public void sendData(String data) {

                //starting thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messages_txtBody.append(data);
                    }
                });

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

//        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
////        preferencesEditor.putString("ip","test");
//        preferencesEditor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // If option menu item is Settings, return true.
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    View.OnClickListener startServer = v -> {

        try {

            if (!server.isAlive()) {

                Log.i("DEBUG", "trying start on " + ipAddress + " " + rootDir);

                server.start(5000, false);
                txtStatus.setText("Listening on " + server.hostname + " on port " + server.getListeningPort() + "\n" +
                        "rootdir is " + server.rootDir);
                messages_txtBody.append("Server started. \n");

            } else {
                messages_txtBody.append("Server is already started. \n");
//                messages_txtBody.append(MyServer.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    };


    View.OnClickListener stopServer = v -> {

        if (server.isAlive()) {
            server.stop();
            txtStatus.setText("Server has been stoped");
            messages_txtBody.append("Server stopped. \n");
        }

    };

    public File getApplicationHomeFolder(String type) {

        File[] file_dir = ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_MUSIC);
        // Get the directory for the user's public music directory.
        if (type.equals("internal")) {
            return file_dir[0];
        } else if (type.equals("external")) {
            return file_dir[1];
        } else
            return null;
    }
}
