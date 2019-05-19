package com.example.lib;

import com.example.lib.Utils.*;

import org.jetbrains.annotations.NotNull;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

import java.time.temporal.ValueRange;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.protocols.http.request.Method;

public class Methods {

    //todo add filechannels for files
    //todo fix partial lenght
    //todo rewrite for fileInput and fileOutput transferTo() for put
    //todo close all streams
    //todo elistener wrap


    public Response res;

    public Response doOptions() {

        res = Response.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, "");

        res.addHeader("Allow", "OPTIONS,HEAD,GET,DELETE,PUT,MKCOL,PROPFIND");
        //Responses to this method are not cacheable.
        res.addHeader("Cache-Control", "no-cache");
        //If no response body is included, the response MUST include a Content-Length field with a field-value of "0".

        //If the Request-URI is not an asterisk, the OPTIONS request applies only to the options that are available when communicating with that resource.

        //The HTTP OPTIONS method is used to describe the communication options for the target resource.
        // The client can specify a URL for the OPTIONS method, or an asterisk (*) to refer to the entire server.

        return res;
    }

    public Response doGet(String uri, Map<String, String> headers, @NotNull Path rootDir, @NotNull IHTTPSession session) {

        Path absPath = Paths.get(rootDir.toString().concat(uri));

        if (session.getUri().equals("/favicon.ico")) {
            return Response.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, "aww");
        }

        if (Files.notExists(absPath)) {
            return Response.newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_HTML, "404");
        }

        if (Files.isDirectory(absPath)) {
            res = Response.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, "");
            return res;
        }

        try (InputStream fc = Files.newInputStream(absPath)) {

            //todo open files many times is bad
            //todo channel\bytearray
            Long fs = Files.size(absPath);



            String mimeType = Tools.getMimeType(absPath);
            String etag = Tools.getEtag(absPath);

            byte[] buffer = new byte[fs.intValue()];
            fc.read(buffer);

            InputStream payload = new ByteArrayInputStream(buffer);


            res = Response.newFixedLengthResponse(Status.OK, mimeType, payload, fs);
            //todo put somewhere upper, maybe latest return
            res.addHeader("Accept-Ranges", "bytes");
            if (etag != null) {
                res.addHeader("ETag", etag);
            }
            res.addHeader("Content-Length", String.valueOf(fs));


//            Handle HEAD request
            if (session.getMethod() == Method.HEAD) {

                res.setStatus(Status.OK);
                res.setData(null);
                //set if for now
                res.addHeader("Content-Length", "0");

                MyServer.LOG.info("HEAD:  " + uri);

                MyServer.listener.sendData("HEAD:  " + uri + "\n") ;
//                MyServer.setMessage("HEAD:  " + uri);


                return res;
            }

            //handle partial
            if (headers.containsKey("range") && headers.get("range").matches("^bytes=(\\d+)-(\\d+|\\*|)")) {

                String val = headers.get("range").replaceAll("[^0-9,\\-]+", "");
                Map<String, Long> b = new HashMap<>();

                String[] t;
                t = val.split("-");

                //todo fix this garbage
                b.put("min_range", (new Long(t[0])));

                if (t.length < 2 || t[1].contains("*")) {
                    b.put("max_range", Files.size(absPath));
                } else {
                    b.put("max_range", (new Long(t[1])));
                }


                try {
                    ValueRange valRange = ValueRange.of(b.get("min_range"), b.get("max_range"));
                } catch (IllegalArgumentException e) {
                    res = Response.newFixedLengthResponse(Status.RANGE_NOT_SATISFIABLE, mimeType, "");
                    res.addHeader("Content-Range", "bytes " + "*/" + Files.size(absPath));
                    return res;
                }


                final byte[] partial_buffer = new byte[(int) (b.get("max_range") - b.get("min_range"))];

                for (int i = 0, d = b.get("min_range").intValue(); i < partial_buffer.length; i++, d++) {
                    partial_buffer[i] = buffer[d];
                }

                InputStream partial_payload = new ByteArrayInputStream(partial_buffer);

                //todo fix zero offset
                res = Response.newFixedLengthResponse(Status.PARTIAL_CONTENT, mimeType, partial_payload, partial_buffer.length);
                res.addHeader("Accept-Ranges", "bytes");
//            res.addHeader("Content-Range", "bytes " + range_start + "-" + range_stop + "/" + file.length());
                res.addHeader("Content-Range", "bytes " + b.get("min_range") + "-" + (b.get("max_range")-1) + "/" + Files.size(absPath));

                MyServer.listener.sendData("GET: " + uri + "\n") ;

                return res;
            }

            Tools.FireEvent("GET:  " + uri + "\n"); ;


            return res;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "something goes wrong");

    }

    public Response doDelete(String uri, @NotNull Path rootDir) {

        //DELETE /file.html HTTP/1.1
        //If a DELETE method is successfully applied, there are several response status codes possible:

        //A 202 (Accepted) status code if the action will likely succeed but has not yet been enacted.
        //A 204 (No Content) status code if the action has been enacted and no further information is to be supplied.
        //A 200 (OK) status code if the action has been enacted and the response message includes a representation describing the status.

        Path path = Paths.get(rootDir.toString().concat(uri));

        if (Files.exists(path)) {

            if (Files.isDirectory(path)) {

                try {
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                            Files.delete(path);
                            MyServer.LOG.info("DELETE: " + path);
                            MyServer.listener.sendData("DELETE:  " + path + "\n") ;


                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
                            Files.delete(directory);
                            MyServer.LOG.info("DELETE: " + directory);
                            MyServer.listener.sendData("DELETE:  " + directory + "\n") ;


                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                res = Response.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, "");
                res.addHeader("Cache-Control", "no-cache");
                return res;
            } else {
                try {
                    Files.delete(path);

                    res = Response.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_HTML, "");
                    res.addHeader("Cache-Control", "no-cache");

                    return res;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
        //no content
        else {
            res = Response.newFixedLengthResponse(Status.NO_CONTENT, NanoHTTPD.MIME_HTML, "");
            return res;
        }

        //something goes wrong
        res = Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "something goes wrong");
        return res;
    }

    public Response doPut(String uri, @NotNull Path rootDir, IHTTPSession session) {

        //If the target resource does not have a current representation and the PUT request
        // successfully creates one, then the origin server must inform
        // the user agent by sending a 201 (Created) response.

        //If the target resource does have a current representation and
        // that representation is successfully modified in accordance with the state
        // of the enclosed representation, then the origin server must send
        // either a 200 (OK) or a 204 (No Content) response to indicate
        // successful completion of the request.


        Map<String, String> m = new HashMap<>();

        Path filename_path = Paths.get(rootDir.toString().concat(uri));

        try {

            session.parseBody(m);
            Path payload_path = Paths.get(m.get("content"));

            FileChannel ch1 = (FileChannel) Files.newByteChannel(payload_path);
            ByteBuffer m1 = Tools.getBB(payload_path);
            long size = ch1.size();

            ch1.close();

            byte[] payload_result;

            //so, nanohttpd returns "" if payload is empty
            if (payload_path.toString().contentEquals("")) {
                payload_result = new byte[0];
            } else {
                payload_result = new byte[(int) size];
            }

            if (!Files.exists(filename_path)) {

                    m1.get(payload_result);

                    Files.write(filename_path, payload_result, StandardOpenOption.CREATE_NEW);

                    MyServer.LOG.info("creating " + filename_path.toString());
                    MyServer.listener.sendData("PUT:  " + filename_path.toString() + "\n") ;


                res = Response.newFixedLengthResponse(Status.CREATED, null, "");
                    res.addHeader("Content-Location", uri);

                    return res;

            } else if (Files.exists(filename_path)) {

//                FileChannel ch2 = (FileChannel) Files.newByteChannel(filename_path);
                ByteBuffer m2 = Tools.getBB(filename_path);

                Integer s = m2.compareTo(m1);

                if (s.equals(0)) {
                    //identical, exit
                    MyServer.LOG.info(filename_path.toString() + " identical, skipping");
                    MyServer.listener.sendData(filename_path.toString() + " identical, skipping" + "\n") ;


                    res = Response.newFixedLengthResponse(Status.NO_CONTENT, null, "");
                    res.addHeader("Content-Location", uri);
                    return res;

                } else {

                    m1.get(payload_result);

                    Files.write(filename_path, payload_result, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                    MyServer.LOG.info("creating " + filename_path.toString());
                    MyServer.listener.sendData("PUT:  " + filename_path.toString() + "\n") ;


                    res = Response.newFixedLengthResponse(Status.OK, null, "");
                    res.addHeader("Content-Location", uri);
                    return res;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NanoHTTPD.ResponseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Cannot put content");
    }

    public Response doPropfind(final String uri, final Map<String, String> headers, @NotNull final Path rootDir) {

        final Path path_root = Paths.get(rootDir.toString().concat(uri));

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;

        final String PROP_NAMESPACE = "DAV:";

        //prepare xml

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document d = documentBuilder.newDocument();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource domSource = new DOMSource(d);
            StringWriter writer = new StringWriter();
            d.setXmlStandalone(true);
//            Long fsize = Files.size(filename);
            final Integer depth;

            if (Files.isRegularFile(path_root)){
                depth = 0;
            }
            else if (headers.get("depth") == null ){
                depth = 1;
            }else{
                depth = Integer.parseInt(headers.get("depth"));
            }

            MyServer.LOG.info("depth is  " + depth);




            final Element rootElement = d.createElementNS(PROP_NAMESPACE, "multistatus");
            d.appendChild(rootElement);


            try {
                Files.walkFileTree(path_root, EnumSet.noneOf(FileVisitOption.class), depth, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {

                        MyServer.LOG.info("trogayu previsit " + path);
                        MyServer.listener.sendData("Visiting directory: " + path + "\n") ;


                        //todo move to tools
                        Element response = d.createElement("response");
                        rootElement.appendChild(response);

                        Element href = d.createElement("href");
                        String s = "";


                        href.appendChild(d.createTextNode(uri));
                        response.appendChild(href);

                        Element propstat = d.createElement("propstat");
                        response.appendChild(propstat);

                        Element prop = d.createElement("prop");
                        propstat.appendChild(prop);

                        Element getcontenttype = d.createElement("getcontenttype");
                        //                getcontenttype.appendChild(d.createTextNode(Tools.getMimeType(new File(filename.toString()))));
                        prop.appendChild(getcontenttype);

                        Element getlastmodified = d.createElement("getlastmodified");
                        getlastmodified.appendChild(d.createTextNode(attrs.lastModifiedTime().toString()));
                        prop.appendChild(getlastmodified);

                        Element getcontentlength = d.createElement("getcontentlength");
//                        Long fsize = Files.size(filename);
//                        getcontentlength.appendChild(d.createTextNode(fsize.toString()));
                        prop.appendChild(getcontentlength);

                        Element resourcetype = d.createElement("resourcetype");
                        if (attrs.isDirectory()) {
                            resourcetype.appendChild(d.createElement("collection"));
                        }
                        prop.appendChild(resourcetype);

                        Element status = d.createElement("status");
                        status.appendChild(d.createTextNode("HTTP/1.1 200 OK"));
                        propstat.appendChild(status);


                        return super.preVisitDirectory(path, attrs);
                    }

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                        MyServer.LOG.info("trogayu visit " + path);
                        MyServer.listener.sendData("Visiting file: " + path + "\n") ;


                        Element response = d.createElement("response");
                        rootElement.appendChild(response);

                        Element href = d.createElement("href");
                        String s = "";
                        if (depth <= 0 ){
                            s = uri;
                        }else {
                            s = uri + path.getFileName().toString();
                        }
                        href.appendChild(d.createTextNode(s));
                        response.appendChild(href);

                        Element propstat = d.createElement("propstat");
                        response.appendChild(propstat);

                        Element prop = d.createElement("prop");
                        propstat.appendChild(prop);

                        Element getcontenttype = d.createElement("getcontenttype");
                        getcontenttype.appendChild(d.createTextNode(Tools.getMimeType(path)));
                        prop.appendChild(getcontenttype);

                        Element getlastmodified = d.createElement("getlastmodified");
                        getlastmodified.appendChild(d.createTextNode(attrs.lastModifiedTime().toString()));
                        prop.appendChild(getlastmodified);

                        Element getcontentlength = d.createElement("getcontentlength");
                        String fsize = String.valueOf(attrs.size());
                        getcontentlength.appendChild(d.createTextNode(fsize));
                        prop.appendChild(getcontentlength);

                        Element resourcetype = d.createElement("resourcetype");
                        if (attrs.isDirectory()) {
                            resourcetype.appendChild(d.createElement("collection"));
                        }
                        prop.appendChild(resourcetype);

                        Element status = d.createElement("status");
                        status.appendChild(d.createTextNode("HTTP/1.1 200 OK"));
                        propstat.appendChild(status);

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {

                        Element response = d.createElement("response");
                        rootElement.appendChild(response);

                        Element href = d.createElement("href");
                        href.appendChild(d.createTextNode(path.getFileName().toString()));
                        response.appendChild(href);

                        Element propstat = d.createElement("propstat");
                        response.appendChild(propstat);

                        Element prop = d.createElement("prop");
                        propstat.appendChild(prop);

                        Element status = d.createElement("status");
                        status.appendChild(d.createTextNode("HTTP/1.1 404 Not Found"));
                        propstat.appendChild(status);


                        return FileVisitResult.CONTINUE;
                    }
                });

            }catch (IOException e) {
                e.printStackTrace();
            }


            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Result result = new StreamResult(os);

            transformer.transform(domSource, result);

            //todo ??
            ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());
            Long size = (long) os.toByteArray().length;
//
//            StreamResult consoleResult = new StreamResult(System.out);
//            transformer.transform(domSource, consoleResult);

            return res = Response.newFixedLengthResponse(Status.MULTI_STATUS, "Application/xml", bis, size);

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


        return Response.newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_HTML, "");
    }

    public Response doMkcol(String uri, @NotNull Path rootDir) {

        // Responses to this method MUST NOT be cached.

        // When invoked without a request body, the collection will be created without member resources.
        // When used with a request body, you can create members and properties on the collections or members.

        Path filename_path = Paths.get(rootDir.toString().concat(uri));

        //When the MKCOL operation creates a new collection resource, all ancestors
        //MUST already exist, or the method MUST fail with a 409 (Conflict) status code. For example, if a request to
        //create collection /a/b/c/d/ is made, and /a/b/c/ does not exist, the request must fail

        if (Files.isDirectory(filename_path) && Files.exists(filename_path)) {
            res = Response.newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_HTML, "");

            res.addHeader("Cache-Control", "no-cache");

            return res;
        }

        try {
            Files.createDirectory(filename_path);

            res = Response.newFixedLengthResponse(Status.CREATED, NanoHTTPD.MIME_HTML, "");

            res.addHeader("Cache-Control", "no-cache");

            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }

        res = Response.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Something goes wrong");
        return res;
    }

}