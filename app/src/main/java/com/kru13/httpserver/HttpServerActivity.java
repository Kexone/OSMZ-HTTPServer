package com.kru13.httpserver;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Semaphore;

public class HttpServerActivity extends Activity implements OnClickListener{

	private SocketServer s;
	private TextView txtView;
	private ScrollView scrlView;
	private LinearLayout ll;
	private CameraHandler camera;
	public SurfaceView sv;
	public SurfaceHolder sh;
	Button btn1;

	Handler h = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String text = (String)msg.obj;
			Log.d("TEXT", text);
			//btn1.setText(text);
			txtView.setText(text);
			TextView tt = new TextView(getBaseContext());
			tt.setText(text);
			tt.setTextColor(Color.BLACK);
			ll.setBackgroundColor(Color.CYAN);
			ll.addView(tt);
			//scrlView.addView(txtView);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http_server);

		btn1 = (Button) findViewById(R.id.button1);
		Button btn2 = (Button) findViewById(R.id.button2);
		scrlView = (ScrollView) findViewById(R.id.scrollView);
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		txtView = (TextView) findViewById(R.id.textView);
		ll = (LinearLayout) findViewById(R.id.linearLayoutInScroll);
		sv = (SurfaceView) findViewById(R.id.surfaceView);
		sh = sv.getHolder();
		//Log.d("FIRST", System.getenv("EXTERNAL_STORAGE"));
		//Log.d("SECOND", System.getenv("SECONDARY_STORAGE"));
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.http_server, menu);
		return true;
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.button1) {
			if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED && (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
				requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 88);
				return;
			}
			camera = new CameraHandler(0);
			Log.d("CAMERA", camera.toString());
			s = new SocketServer(h, camera, sh);
			s.start();
			Toast.makeText(this, "Server running", Toast.LENGTH_SHORT).show();
		}
		if (v.getId() == R.id.button2) {
			s.close();
			try {
				s.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Toast.makeText(this, "Server stopped", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	@TargetApi(23)
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == 88 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
			camera = new CameraHandler(0);

			s = new SocketServer(h, camera,sh);
			s.start();

			Toast.makeText(this, "Server running", Toast.LENGTH_SHORT).show();
		}
		else if (requestCode == 88){
			requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 88);
		}
	}

}
