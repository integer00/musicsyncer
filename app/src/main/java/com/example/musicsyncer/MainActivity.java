package com.example.musicsyncer;

/*
##find correct sdcard

##build basic interface like
                            Listening on $ip:$port
                            Serving $directory

                            Start/Stop button
                        (when someone connected shows like $ip is connected)

#verbose button for stacktrace?
##Add background job for bg service


 */


import android.Manifest;

import android.support.v4.app.ActivityCompat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.lib.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class MainActivity extends AppCompatActivity {

    TextView txtStatus, messages_txtBody;


    // Storage Permissions
    private static final int MY_PERMISSIONS_REQUEST = 1;

    Path rootDir = Paths.get("/storage/emulated/0/sync");
//        File[] rootDir = this.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS);


    MyServer server = new MyServer("0.0.0.0", 8080, rootDir);






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }




    View.OnClickListener startServer = v -> {

        try {

            if (!server.isAlive()) {

                server.start(5000, false);
                txtStatus.append("Listening on " + server.hostname + " on port " + server.getListeningPort() + "\n");
                txtStatus.append("rootdir is " + server.rootDir);
                messages_txtBody.append("Server started. \n");

            } else{
                messages_txtBody.append("Server is already started. \n");
//                messages_txtBody.append(MyServer.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    };


    View.OnClickListener stopServer = v -> {

        if (server.isAlive()){
            server.stop();
            txtStatus.setText("Server has been stoped");
            messages_txtBody.append("Server stopped. \n");
        }

    };

}
