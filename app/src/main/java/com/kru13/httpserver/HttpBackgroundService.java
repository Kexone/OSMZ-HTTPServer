package com.kru13.httpserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import static android.content.ContentValues.TAG;
import static com.kru13.httpserver.HttpServerActivity.ll;
import static com.kru13.httpserver.HttpServerActivity.sh;
import static com.kru13.httpserver.HttpServerActivity.txtView;

/**
 * Created by Jakub on 22.03.2017.
 */

public class HttpBackgroundService extends Service {

    private SocketServer s;
    private CameraHandler camera;
    private Context baseContext;
    Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String text = (String)msg.obj;
            Log.d("TEXT", text);
            //btn1.setText(text);
            if(txtView != null && baseContext != null) {
            txtView.setText(text);
            TextView tt = new TextView(baseContext);
            tt.setText(text);
            tt.setTextColor(Color.BLACK);
            ll.setBackgroundColor(Color.CYAN);
            ll.addView(tt);
            }
            //scrlView.addView(txtView);

        }
    };

    public HttpBackgroundService() {

    }
    public HttpBackgroundService(Context baseContext) {
        this.baseContext = baseContext;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: SERVICE STARTED");

        camera = new CameraHandler(0);
        Log.d("CAMERA", camera.toString());
        s = new SocketServer(h, camera, sh);
        s.start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Close socket server
        s.close();
        try {
            s.join();
        } catch (InterruptedException e) {
            Log.d(TAG, "onDestroy: " + e.getMessage());
            e.printStackTrace();
        }

        Log.d(TAG, "onDestroy: SERVICE STOPPED");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
