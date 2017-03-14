package com.kru13.httpserver;

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

import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class SocketServer extends Thread {

	android.os.Handler h;
	ServerSocket serverSocket;
	Semaphore s;
	public static final int MAX_AVAILABLE = 5;
	public final int port = 12345;
	boolean bRunning;
	CameraHandler cam;

	public SocketServer(Handler h, CameraHandler cam) {
		this.h = h;
		this.cam = cam;
	}
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d("SERVER", "Error, probably interrupted in accept(), see log");
			e.printStackTrace();
		}
		bRunning = false;
	}

	public void run() {
		try {
			Log.d("SERVER", "Creating Socket");
			serverSocket = new ServerSocket(port);
			bRunning = true;
			Semaphore semaphore = new Semaphore(MAX_AVAILABLE,true);
			while (bRunning) {
				Log.d("SERVER", "Socket Waiting for connection");
				Socket s = serverSocket.accept();
				Log.d("SERVER", "Socket Accepted");
				if( semaphore.tryAcquire()) {
					Log.d("AVAILABLE", String.valueOf(semaphore.availablePermits()));
					new ClientThread(s,h, semaphore, cam).start();
				}
				else {
					OutputStream o = s.getOutputStream();
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
					out.write("<html><body><h1> sorry jako </h1></body></html>\n");
					out.flush();

					s.close();
					Log.d("SERVER", "Socket Closed");
				}
			}
		}
		catch (IOException e) {
			if (serverSocket != null && serverSocket.isClosed())
				Log.d("SERVER", "Normal exit");
			else {
				Log.d("SERVER", "Error");
				e.printStackTrace();
			}
		} finally {
			serverSocket = null;
			bRunning = false;
		}
	}

}