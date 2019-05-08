package com.example.lib;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class test{

    public static void main(String[] args) {

        String hostname = "127.0.0.1";
        int port = 8080;
        Path rootDir = Paths.get("/Users/kdm/test/webroot");


        //create server
        MyServer server = new MyServer(hostname, port, rootDir);


        server.setEventListener(new MyServer.EventListener() {

            @Override
            public void sendData(String data) {
                MyServer.LOG.info("EventListener:  " + data);
            }
        });


        try {
            server.start(5000, false);
            MyServer.LOG.info("Serving on " + server.hostname + " at port " + server.getListeningPort() + ", webroot is '" + server.rootDir + "'.");

            System.out.println("Server started, Hit Enter to stop.\n");
            System.in.read();

        } catch (IOException e) {
            System.err.println("Couldn't start server:\n" + e);
            System.exit(-1);
        } finally {
            server.stop();
            System.out.println("Server stopped.\n");
        }
    }
        }