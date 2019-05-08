package com.example.lib;


import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

public class MyServer extends NanoHTTPD {
    public Path rootDir;
    public static EventListener listener;
    public static final Logger LOG = Logger.getLogger(MyServer.class.getName());

    public interface EventListener {

        void sendData(String data);
    }

    public void setEventListener(EventListener listener) {
        this.listener = listener;
    }


    // server constructor
    public MyServer(String hostname, int port, Path rootDir) {
        super(hostname, port);

        this.rootDir = rootDir;
        this.listener = null;

        if (rootDir == null ) {
            this.rootDir = Paths.get("").getFileName();
        }
    }

    //server incoming requests
    @Override
    public Response serve(IHTTPSession session) {

        String uri = session.getUri();
        final Map<String, String> headers = Collections.unmodifiableMap(session.getHeaders());
        Path rootDir = this.rootDir;

        Response response;
        Method method = session.getMethod();

        Methods methods = new Methods();

        //switch through Methods
        switch (method) {
            case OPTIONS:
                response = methods.doOptions();
                break;
            case GET:
            case HEAD:
                response = methods.doGet(uri, headers, rootDir, session);
                break;
            case DELETE:
                response = methods.doDelete(uri, rootDir);
                break;
            case PUT:
                response = methods.doPut(uri, rootDir, session);
                break;
            case PROPFIND:
                response = methods.doPropfind(uri, headers, rootDir);
                break;
            case MKCOL:
                response = methods.doMkcol(uri,rootDir);
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
