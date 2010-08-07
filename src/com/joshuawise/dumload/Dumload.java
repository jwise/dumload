package com.joshuawise.dumload;

import java.io.InputStream;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class Dumload extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView tv = (TextView)findViewById(R.id.suckit);
		tv.setText("Suck it.");
	}
	
	private void say(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
	
	public void onStart() {
		super.onStart();
		
		Intent i = getIntent(); /* i *am* not an intent! */
		if (i.getAction().equals(Intent.ACTION_SEND))
		{
			Bundle extras = i.getExtras();
			Uri uri = (Uri)extras.getParcelable(Intent.EXTRA_STREAM);
			
			Log.e("Dumload", "Got a send -- starting service.");
			// Never let an ML programmer touch Java.
			android.content.ComponentName cn = getApplicationContext().startService(new Intent().setClass(getApplicationContext(), Uploader.class).setData(uri));
			
			if (cn == null)
				say("Fuuuuuuuuuck.");
			else
				Log.e("Dumload", "Started service " + cn.toString() + ".");
			
			TextView tv = (TextView)findViewById(R.id.suckit);
			tv.setText("Action was send: "+uri.toString());
		} else {
			TextView tv = (TextView)findViewById(R.id.suckit);
			tv.setText("Action was something else");
		}
	}
}