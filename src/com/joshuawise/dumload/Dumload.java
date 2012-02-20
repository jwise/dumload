package com.joshuawise.dumload;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Dumload extends Activity {
	
	private SharedPreferences prefs;
	
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
		final Dumload thisact = this;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Intent i = getIntent(); /* i *am* not an intent! */
		
		if (!i.getAction().equals(Intent.ACTION_SEND))
		{
//			say("Unknown intent for dumload");
//			this.finish();
			Intent pi = new Intent(this, Preferences.class);
			startActivity(pi);
			return;
		}
		
		Bundle extras = i.getExtras();
		final Uri uri = (Uri)extras.getParcelable(Intent.EXTRA_STREAM);
		
		Log.e("Dumload", "Got a send -- starting service.");
		
		Button go = (Button)findViewById(R.id.go);
		go.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String s = ((TextView) findViewById(R.id.entry)).getText().toString();
				android.content.ComponentName cn = getApplicationContext()
				                                     .startService(new Intent()
				                                                     .setClass(getApplicationContext(), Uploader.class)
				                                                     .setData(uri)
				                                                     .putExtra("com.joshuawise.dumload.dest", s));
				if (cn == null)
					say("Failed to start uploader.");
				else
					Log.e("Dumload", "Started service " + cn.toString() + ".");
				finish();
			}
		});
		
		String uribase = uri.toString();
		
		
		((TextView) findViewById(R.id.suckit)).setText("Where to?");
		((TextView) findViewById(R.id.entry)).setText(prefs.getString("defaultUploadPath", "/var/www/") + uribase.substring(uribase.lastIndexOf("/") + 1) + ".jpg");
		
	}
}