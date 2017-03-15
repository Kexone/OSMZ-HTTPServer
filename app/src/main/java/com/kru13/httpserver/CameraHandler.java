package com.kru13.httpserver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import  android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

/**
 * Created by Jakub on 07.03.2017.
 */

public class CameraHandler {


    private int CAMERA_ID;
    private Camera mCamera;
    private BufferedWriter out;
    private Socket src;
    private byte[] img = null;
    private OutputStream outputStream;


    public CameraHandler(int id) {
        CAMERA_ID = id;
    }

    private void takeAPhoto() {

        mCamera.takePicture(null, null, jpegCallback);

    }

    public void setPreview(SurfaceHolder sh, BufferedWriter out, Socket src, OutputStream o)
    {
        this.out = out;
        this.src = src;
        this.outputStream = o;
        try {
            mCamera = Camera.open(CAMERA_ID);
            mCamera.setPreviewDisplay(sh);
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = parameters.getPreviewSize();
                    YuvImage image = new YuvImage(data, ImageFormat.NV21,
                            size.width, size.height, null);
                    Rect rectangle = new Rect();
                    rectangle.bottom = size.height;
                    rectangle.top = 0;
                    rectangle.left = 0;
                    rectangle.right = size.width;

                    ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                    image.compressToJpeg(rectangle, 100, out2);
                    img = out2.toByteArray();
                }
            });
            mCamera.startPreview();
            saveImg();
           // takeAPhoto();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImg() {
        try {
            out.write("HTTP/1.0 200 OK\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Cache-Control: private\r\n" +
                    "Content-Type: multipart/x-mixed-replace;boundary=boundary\r\n\r\n");
            while(true) {
                byte[] output = this.img;
                if (output != null)
                {
                    out.write("--boundary\r\n" +
                            "Content-Type: image/jpeg\r\n" +
                            "Content-Length: " + output.length + "\r\n");
                    out.write("\r\n");
                    out.flush();
                    this.outputStream.write(output);
                    this.outputStream.flush();

                    out.write("--boundary\r\n");
                    out.flush();
                }
                sleep(100);
            }
    } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


        Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //new SaveImageTask().execute(data);
            FileOutputStream outStream = null;
            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/osmz");
                dir.mkdirs();

                String fileName = "amera.jpg";
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                //Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                //bm.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
                outStream.write(data);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
            }
            try {


                out.write("HTTP/1.0 200 OK\n" +
                        "Connection: close\n" +
                        "Content-Type: text/html\n\n" +
                        "<meta http-equiv=\"refresh\" content=\"5\"> \n" +
                        "<html><body><h1> Cam feed </h1> <img src='osmz/amera.jpg' width='250px'> </body></html>\n");


                //out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };


    private boolean safeCameraOpen() {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(CAMERA_ID);
            qOpened = (mCamera != null);
        } catch (Exception e) {
           // Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
       // mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


}
