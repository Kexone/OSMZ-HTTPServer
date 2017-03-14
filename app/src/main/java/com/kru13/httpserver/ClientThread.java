package com.kru13.httpserver;


import android.graphics.Camera;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import android.os.Handler;
import android.view.SurfaceHolder;

/**
 * Created by Jakub on 21.02.2017.
 */

public class ClientThread extends Thread {

    Socket s;
    boolean bRunning;
    android.os.Handler h;
    Semaphore semaphore;
    String fileName;
    CameraHandler cam;
    SurfaceHolder sh;

    public ClientThread(Socket s, android.os.Handler h, Semaphore sem, CameraHandler cam, SurfaceHolder sh) {
        this.s = s;
        this.h = h;
        this.semaphore = sem;
        this.cam = cam;
        this.sh = sh;
    }
    public void close() {
        try {
            s.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

    public void run() {
        try {
            bRunning = true;
            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                Log.d("SERVER", "Socket Accepted");

                OutputStream o = s.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String tmp = "";
                String getRequest = "";
                try {
                    while (!(tmp = in.readLine()).isEmpty()) {
                        Log.d("HLAVICKA", tmp);
                        getRequest = tmp.startsWith("GET") ? tmp : getRequest;

                    }
                }
                catch (Exception e) {
                    Message msg = h.obtainMessage();
                    msg.obj = "Error " + e.getLocalizedMessage();
                    msg.sendToTarget();
                }
                String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                try {
                    fileName =  getRequest.split(" ")[1];
                }
                catch(Exception e) {
                    fileName = "System is bizi";
                }
                Log.d("PICTURE",fileName.replace("/",""));
                File f = new File( rootPath + fileName);
                if(fileName.contains("camera") ) {
                    cam.setPreview(sh, out, s, o);


                    //if(fileName.endsWith(".jpg")) out.write("Content-Type: image/jpg\n");

                }
                else if(f.exists()) {
                    out.write("HTTP/1.0 200 OK\n");
                    out.write("Connection: close\n");
                    if(fileName.endsWith(".png")) out.write("Content-Type: image/png\n");
                    if(fileName.endsWith(".jpg")) out.write("Content-Type: image/jpg\n");
                    if(fileName.endsWith(".htm")) out.write("Content-Type: text/html\n");
                    out.write("Content-Lenght:" + f.length() + "\n");
                    out.write("\n");
                    out.flush();
                    Message msg = h.obtainMessage();
                    msg.obj = fileName + " " + f.length();
                    msg.sendToTarget();
                    FileInputStream fis = new FileInputStream(f);
                    byte buffer[] = new byte[1024];
                    int len;
                    while ( (len = fis.read(buffer,0, 1024) ) > 0 ) {
                        o.write(buffer, 0, len );
                    }
                    out.flush();
                    s.close();
                }
                else {
                    out.write("HTTP/1.0 404 Not Found\n" +
                            "Connection: close\n" +
                            "Content-Type: text/html\n\n" +
                            "<!DOCTYPE html>\n<html><body><h1> sorry jako </h1></body></html>\n");
                    out.flush();
                    s.close();
                }
                //out.write(result);
                //out.flush();

              //  s.close();
                Log.d("SERVER", "Socket Closed");
            }
        }
        catch (IOException e) {
            if (s != null && s.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        }
        finally {
            s = null;
            bRunning = false;
            semaphore.release();
        }
    }
}
