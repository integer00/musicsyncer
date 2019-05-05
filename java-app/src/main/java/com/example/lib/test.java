package com.example.lib;

import com.example.lib.Utils.*;

import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ValueRange;
import java.util.EnumSet;
import java.util.stream.Stream;

import javax.xml.namespace.NamespaceContext;
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




public class test {

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String getEtag(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte bytes[] = new byte[(int) file.length()];
            fis.read(bytes);


            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            System.out.println(bytesToHex(hash));
            System.out.println(bytesToHex(hash).substring(0, 32));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    //GET
    //HTTP/1.1 200 OK
    //Date: Mon, 15 Apr 2019 18:16:25 GMT
    //"Accept-Ranges": "bytes"
    //Content-Type: video/mp4
    //Content-Length: 5251725

    //GET RANGE
    //"Range: bytes=0-255"

    //HTTP/1.1 206 Partial Content
    //Date: Mon, 15 Apr 2019 18:16:44 GMT
    //Content-Type: video/mp4
    //Content-Length: 256


    public static int getPartial(byte arraySize[], int ARR_START, int ARR_STOP) {


        int newSize = ARR_STOP - ARR_START;

        byte[] result = new byte[newSize];

//        System.out.print("size" + result.length);
        //iterator i = 0 ; i <

        for (int i = ARR_START; i < newSize; i++) {

//            System.out.print(result[i]);

            result[i] = arraySize[i];
            System.out.print(" " + result[i]);

        }

//        int first = 0;
//        int newContentLength = a2+1;

        //                    74 70 73 70
        //                                0 1 1 1 0
        // -1 -40 -1 -32 0 16 74 70 73 70 0 1 1 1 0


        System.out.println("Content-Length:" + newSize);


//        System.out.print();


        return 0;
    }

    public static void main(String[] args) throws IOException {

        //depth 0
        //depth 1-infinity

        final Path path = Paths.get("/Users/kdm/test/webroot/testdav2");


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
            Integer depth;
            final String uri = "/1.txt";



            final Element rootElement = d.createElementNS(PROP_NAMESPACE, "multistatus");
            d.appendChild(rootElement);


            try {
                Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {

                        MyServer.LOG.info("trogayu previsit " + path);

                        //todo move to helper
                        Element response = d.createElement("response");
                        rootElement.appendChild(response);

                        Element href = d.createElement("href");
                        String s = "";
                        //if uri is root
                        if (uri.equals("/")) {
                            s = "/";
                            //if uri is path to something
                        } else if (uri.endsWith("/")) {
                            s = uri;
                            //else uri ???
                        } else if (uri.startsWith("/")) {
                            s = uri + "/" + path.getFileName();
                        } else {
                            s = uri + "/";
                        }
                        href.appendChild(d.createTextNode(path.getFileName().toString()));
                        response.appendChild(href);

                        Element propstat = d.createElement("propstat");
                        response.appendChild(propstat);

                        Element prop = d.createElement("prop");
                        propstat.appendChild(prop);

                        Element getcontenttype = d.createElement("getcontenttype");
                        //                getcontenttype.appendChild(d.createTextNode(Helper.getMimeType(new File(filename.toString()))));
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

                        Element response = d.createElement("response");
                        rootElement.appendChild(response);

                        Element href = d.createElement("href");
                        String s = "";
                        //if uri is root
                        if (uri.equals("/")) {
                            s = "/";
                            //if uri is path to something
                        } else if (uri.endsWith("/")) {
                            s = uri;
                            //else uri ???
//                        } else if (uri.startsWith("/")) {
//                           s = uri + "/" + path.getFileName();
                        } else {
                            s = uri + path.getFileName().toString();
                        }
                        href.appendChild(d.createTextNode(s));
                        response.appendChild(href);

                        Element propstat = d.createElement("propstat");
                        response.appendChild(propstat);

                        Element prop = d.createElement("prop");
                        propstat.appendChild(prop);

                        Element getcontenttype = d.createElement("getcontenttype");
                        getcontenttype.appendChild(d.createTextNode(Helper.getMimeType(path)));
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
                        String s = "";
                        //if uri is root
                        if (uri.equals("/")) {
                            s = "/";
                            //if uri is path to something
                        } else if (uri.endsWith("/")) {
                            s = uri;

                            //else uri ???
                        } else {
                            s = uri + "/";
                        }
                        //path.getFileName().toString()
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


            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(domSource, consoleResult);

//


//            StreamResult consoleResult = new StreamResult(System.out);
//            transformer.transform(domSource, consoleResult);


        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


    }
}
//
//        try {
//            //1st and second files might be null size'd content
//            //1st file might me empty, so paths would not find it so exception
//            //
//            String ff = "/Users/kdm/test/1.txt";
//            String ff2 = "/Users/kdm/test/2.txt";
//
//            Path f1 = Paths.get(ff);
//            Path f2 = Paths.get(ff2);
//
//            byte[] payload;
//
//            FileChannel ch1 = (FileChannel) Files.newByteChannel(f1);
//            ByteBuffer m1 = com.example.lib.Utils.Helper.getBB(f1);
//
//            FileChannel ch2 = (FileChannel) Files.newByteChannel(f2);
//            ByteBuffer m2 = com.example.lib.Utils.Helper.getBB(f2);
//
//            Integer s = m2.compareTo(m1);
//            System.out.println("s");
//
//
//
//            if (s.equals(0)) {
//                //identical, exit
//            }
//
//            else{
//                //nanohttpd sends empty payload as "", so checking it here
//                if(ff.contentEquals("")){
//                    payload = new byte[0];
//                }
//                else {
//                    payload = new byte[(int)ch1.size()];
//                }
//
//                m1.get(payload);
//                System.out.println("s");
//
//                Files.write(f2,payload,StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
//            }
//
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//    }
//}


//        String e = getEtag(new File("/Users/kdm/test/testdav/dav/1.txt"));
//
//
//        Path path = Paths.get("/Users/kdm/test/webroot");
//        File f = new File("/Users/kdm/test/webroot");
//
//        String a[] = f.list();
//        for ( String s : a){
//            System.out.println(s);
//        }
//
//        System.out.println();



//        try {
//            Files.walkFileTree(path,new SimpleFileVisitor<Path>()
//            {
//
//                @Override
//                public FileVisitResult visitFile(Path filePath,BasicFileAttributes attributes)
//                {
//                    System.out.println(filePath);
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //PROPFIND /container/ HTTP/1.1
        // Host: www.example.com
        // Content-Length: xxxx
        // Content-Type: application/xml; charset="utf-8"
        //
        // <?xml version="1.0" encoding="utf-8" ?>
        // <D:propfind xmlns:D='DAV:'>      < ------- Element (D: <-- prefix) (xmlns:D='DAV' <-- namespace for :D is 'DAV'))
        //      <D:prop>                     < ------- Element within element
        //      <D:lockdiscovery/>
        //      </D:prop>
        // </D:propfind>


//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder documentBuilder;
//
//        String PROP_NAMESPACE = "xmlns='DAV:'";

        // /

//
//        try {
//            documentBuilder = documentBuilderFactory.newDocumentBuilder();
//            Document d = documentBuilder.newDocument();
//            TransformerFactory transformerFactory = TransformerFactory.newInstance();
//            Transformer transformer = transformerFactory.newTransformer();
//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//            DOMSource domSource = new DOMSource(d);
//            StringWriter writer = new StringWriter();
//            d.setXmlStandalone(true);
//            String depth = "";
//
//
//            if (Files.isRegularFile(filename) && depth.equeals("0|1|infinity")) {
//                Element rootElement = d.createElementNS(PROP_NAMESPACE, "multistatus");
//                d.appendChild(rootElement);
//
//                Element response = d.createElement("response");
//                rootElement.appendChild(response);
//
//                Element href = d.createElement("href");
//                href.appendChild(d.createTextNode("www.href.com"));
//                response.appendChild(href);
//
//                Element propstat = d.createElement("propstat");
//                response.appendChild(propstat);
//
//                Element prop = d.createElement("prop");
//                propstat.appendChild(prop);
//
//                Element resourcetype = d.createElement("resourcetype");
//                resourcetype.appendChild(d.createElement("collection"));
//                prop.appendChild(resourcetype);
//
//                Element status = d.createElement("status");
//                status.appendChild(d.createTextNode("HTTP/1.1 200 OK"));
//                propstat.appendChild(status);
//            } else if (Files.isDirectory(filename)) {
//                //only folder
//                if (depth.equals("0")) {
//                    Element rootElement = d.createElementNS(PROP_NAMESPACE, "multistatus");
//                    d.appendChild(rootElement);
//
//                    Element response = d.createElement("response");
//                    rootElement.appendChild(response);
//
//
//                    Element href = d.createElement("href");
//                    href.appendChild(d.createTextNode(uri));
//                    response.appendChild(href);
//
//                    Element propstat = d.createElement("propstat");
//                    response.appendChild(propstat);
//
//                    Element status = d.createElement("status");
//                    status.appendChild(d.createTextNode("HTTP/1.1 200 OK"));
//                    propstat.appendChild(status);
//
//                    Element prop = d.createElement("prop");
//                    propstat.appendChild(prop);
//
//                    Element resourcetype = d.createElement("resourcetype");
//                    resourcetype.appendChild(d.createTextNode("collection"));
//                    prop.appendChild(resourcetype);
//
//                    Element getcontenttype = d.createElement("getcontenttype");
//                    getcontenttype.appendChild(d.createTextNode("text/plain"));
//                    prop.appendChild(getcontenttype);
//
//                    Element getcontentlength = d.createElement("getcontentlength");
//                    getcontentlength.appendChild(d.createTextNode("33"));
//                    prop.appendChild(getcontentlength);
//                }
//                //infinity
//                else {
//                    File files = new File(rootDir.getAbsolutePath().concat(uri));
//
//                    String filesArray2[] = files.list();
//
//                    Element rootElement = d.createElementNS(PROP_NAMESPACE, "multistatus");
//                    d.appendChild(rootElement);
//
//                    for (String each : filesArray2) {
//
//                        String s = "";
//                        if (uri.equals("/")) {
//                        } else {
//                            s = uri;
//                        }
//
//                        Element response = d.createElement("response");
//                        rootElement.appendChild(response);
//
//                        Element href = d.createElement("href");
//                        href.appendChild(d.createTextNode(s + "/" + each));
//                        response.appendChild(href);
//
//                        Element propstat = d.createElement("propstat");
//                        response.appendChild(propstat);
//
//                        Element status = d.createElement("status");
//                        status.appendChild(d.createTextNode("HTTP/1.1 200 OK"));
//                        propstat.appendChild(status);
//
//                        Element prop = d.createElement("prop");
//                        propstat.appendChild(prop);
//
//                        Element resourcetype = d.createElement("resourcetype");
//                        if (new File(rootDir.getAbsolutePath().concat("/" + each)).isDirectory()) {
//                            resourcetype.appendChild(d.createTextNode("collection"));
//                        }
//                        prop.appendChild(resourcetype);
//
//                        Element getcontenttype = d.createElement("getcontenttype");
//                        getcontenttype.appendChild(d.createTextNode("text/plain"));
//                        prop.appendChild(getcontenttype);
//
//                        Element getcontentlength = d.createElement("getcontentlength");
//                        getcontentlength.appendChild(d.createTextNode("33"));
//                        prop.appendChild(getcontentlength);
//
//                    }
//                }
//            } else {
//                error
//            }
//
//
//
//            ByteArrayOutputStream os = new ByteArrayOutputStream();
//            Result result = new StreamResult(os);
//
//            transformer.transform(domSource, result);
//
//
//            ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());
//            Long size = (long) os.toByteArray().length;
//
//            StreamResult consoleResult = new StreamResult(System.out);
//            transformer.transform(domSource, consoleResult);
//
//            return res = Response.newFixedLengthResponse(Status.MULTI_STATUS, "Application/xml", new ByteArrayInputStream(os.toByteArray()), size);
//
//
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (TransformerConfigurationException e) {
//            e.printStackTrace();
//        } catch (TransformerException e) {
//            e.printStackTrace();
//        }

//
////        boolean l1IsFull = false;
//
////        String range = "bytes=0-255";
//        String range = "bytes=755-255";
////        String range = "bytes=755-255";
////        String range = "bytes=0-";
////        String range = "bytes=0-*";
//
//        String val = range.replaceAll("bytes=", "");
//        String parsed[] = new String[2];
//        parsed = val.split("-");
//
//        Long fileSize = (long) 69999;
//
//        Long l[] = new Long[2];
//
//
//        //handle empty 2nd argument
//        if (parsed.length != 2) {
//            try {
//                l[1] = fileSize;
//
//                for (int i = 0; i < parsed.length; i++) {
//                    l[i] = Long.parseLong(parsed[i]);
//                }
//            } catch (NullPointerException e) {
//                e.printStackTrace();
//            }
//        }
//        //handle * 2nd argument
//        else if (parsed[1].contentEquals("*")) {
//            try {
//                l[1] = fileSize;
//                l[0] = Long.parseLong(parsed[0]);
//            } catch (NullPointerException e) {
//                e.printStackTrace();
//            }
//         }
//         //should be fine
//         else {
//            for(int i=0; i < parsed.length;i++){
//                l[i] = Long.parseLong(parsed[i]);
//            }
//        }
//
//
//        // if minimal bigger than maximal throws 416 not satisfiable
//        // if maximal is bigger than file size then it is ok(?)
//        //get range
//        try{
//            ValueRange valRange = ValueRange.of(l[0],l[1]);
//        }catch (IllegalArgumentException e){
//            System.out.println("416");
//            e.printStackTrace();
//        }
//
////        File file = new File("/Users/kdm/test/webroot/cat.jpg");
////
////        try {
////            FileInputStream fis = new FileInputStream(file);
////            byte array[] = new byte[(int)file.length()];
//////            System.out.print("size" + (int)file.length());
////
////            fis.read(array);
////
////            getPartial(array,0,25);
////
////        } catch (FileNotFoundException e) {
////            e.printStackTrace();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
//
////        int size = getPartial(array.length,0,15);
//


