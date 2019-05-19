package com.example.lib.Utils;

import com.example.lib.MyServer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Tools {


    @NotNull
    public static String bytesToHex(@NotNull byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Nullable
    public static String getEtag(@NotNull Path fc) {

        try (InputStream is = Files.newInputStream(fc)) {
            byte buffer[] = new byte[(int) Files.size(fc)];
            is.read(buffer);


            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(buffer);
            return bytesToHex(hash).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ByteBuffer getBB(Path p) {

        try (FileChannel ch = (FileChannel) Files.newByteChannel(p)) {

            ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY, 0L, ch.size());

            return bb;
        } catch (IOException e) {
            e.getMessage();
            ByteBuffer bb = ByteBuffer.allocateDirect(0);
            return bb;
        }
    }

    public static String getMimeType(Path fc) {


//        //todo returning to many times
        if (Files.isDirectory(fc)) {
            return "text/plain";
        }

        try (InputStream is = Files.newInputStream(fc)){

            byte buffer[] = new byte[11];
            is.read(buffer);

            String result = MimeTypes.getContentType(buffer);


            if (result == null) {
//                MyServer.LOG.info("Content type for " + file.getAbsolutePath() +" is unknown, returning plaintext");
                return "text/plain";
            } else {
                return result;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "text/plain";
    }

    public static void FireEvent(String data) {

        if (MyServer.listener != null) {
            MyServer.listener.sendData(data);
        }
    }
}

