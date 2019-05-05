package com.example.lib;


import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

public class MyServer extends NanoHTTPD {

    private Path rootDir;

    public static final Logger LOG = Logger.getLogger(MyServer.class.getName());


    public static void main(String[] args) {

        String hostname = "127.0.0.1";
        int port = 8080;
        Path rootDir = Paths.get("/Users/kdm/test/webroot");


        //create server
        MyServer server = new MyServer(hostname, port, rootDir);


        try {
            server.start(5000, false);
            MyServer.LOG.info("Serving on " + hostname + " at port " + port + ", webroot is '" + server.rootDir + "'.");

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

    // server constructor
    public MyServer(String hostname, int port, Path rootDir) {
        super(hostname, port);
        this.rootDir = rootDir;

        //add exeption for non valid rootdir
        if (rootDir == null ) {
            this.rootDir = Paths.get("").getFileName();
        }

    }


    //server incoming requests
    @Override
    public Response serve(IHTTPSession session) {

        Map<String,String> files = new HashMap<>();

        String uri = session.getUri();
        final Map<String, String> headers = Collections.unmodifiableMap(session.getHeaders());
        Path rootDir = this.rootDir;

        //parameters sended to server eg. ?username=asd?password=asd
        Map<String, List<String>> params = session.getParameters();

        Response response;
        Method method = session.getMethod();

        //switch through Methods
        switch (method) {
            case OPTIONS:
                response = Methods.doOptions();
                break;
            case GET:
            case HEAD:
                response = Methods.doGet(uri, headers, rootDir, session);
                break;
            case DELETE:
                response = Methods.doDelete(uri, rootDir);
                break;
            case PUT:
                response = Methods.doPut(uri, rootDir, session);
                break;
            case PROPFIND:
                response = Methods.doPropfind(uri, headers, rootDir);
                break;
            case MKCOL:
                response = Methods.doMkcol(uri,rootDir);
                break;


            //When a request method is received that is unrecognized or not implemented by an origin server,
            //the origin server SHOULD respond with the 501 (Not Implemented) status code.
            default:
                response = Response.newFixedLengthResponse(Status.NOT_IMPLEMENTED, MIME_HTML, "");
                break;
        }

        //returning response object
        return response;
    }

}
