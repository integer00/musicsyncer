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
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lib.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class MainActivity extends AppCompatActivity {

    Button start_btn,stop_btn;
    TextView txtStatus;



    String TAG = "Info:";

    // Storage Permissions
    private static final int MY_PERMISSIONS_REQUEST = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = (TextView) findViewById(R.id.txtStatus);


        Path rootDir = Paths.get("/storage/emulated/0");
//        File[] rootDir = this.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS);


        MyServer server = new MyServer("0.0.0.0", 8080, rootDir);

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//            Log.d("PERM","Permission is granted");
//        }else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
            },MY_PERMISSIONS_REQUEST);
            Log.d("PERM","asking for permission");
//        }

        start_btn = new Button(this);
        stop_btn = new Button(this);

        start_btn = (Button)findViewById(R.id.start_btn);
//
//        start_btn.setOnClickListener(startServer);
//        stop_btn.setOnClickListener(stopServer);

    }


//    View.OnClickListener startServer = new View.OnClickListener(){
//
//        @Override
//        public void onClick(View v) {
//
//
//            try {
//                server.start();
//
//                if(server.isAlive()){
//                    start_btn.setEnabled(false);
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    };
//
//    View.OnClickListener stopServer = new View.OnClickListener(){
//
//        @Override
//        public void onClick(View v) {
//
//            server.stop();
//            if(!server.isAlive()){
//                start_btn.setEnabled(true);
//            }
//        }
//    };

}
