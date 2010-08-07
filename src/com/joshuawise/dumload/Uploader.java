package com.joshuawise.dumload;

import java.io.InputStream;

import com.jcraft.jsch.*;
import java.lang.Boolean;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.app.NotificationManager;
import android.app.Notification;
import android.os.Handler;
import android.os.Messenger;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

public class Uploader extends Service implements Runnable, UserInfo, UIKeyboardInteractive {
	private Uri uri;
	private String homedir;
	private Thread me;
	private static final int HELPME_ID = 1;
	
	public Object _theObject;

	private Object /* pick one type, and fixate on it */ dance(final String type, final String text)	/* for inside the thread */
	{
		final Uploader thisupl = this;
		final Message msg = Message.obtain();
		
		/* t(*A*t) */
		Thread t = new Thread() {
			public void run() {
				Looper.prepare();
				int bogon = (int)SystemClock.elapsedRealtime();
				
				NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				Notification notification = new Notification(R.drawable.icon, "Dumload prompt", System.currentTimeMillis());
				
				Handler h = new Handler() {
					public void handleMessage(Message M) {
						msg.copyFrom(M);
						Looper.myLooper().quit();
					}
				};
				Messenger m = new Messenger(h);
				
				Intent intent = new Intent(thisupl, NotifSlave.class);
					
				intent.setAction("com.joshuawise.dumload.NotifSlave");
				intent.putExtra("com.joshuawise.dumload.returnmessenger", m);
				intent.putExtra("com.joshuawise.dumload.reqtype", type);
				intent.putExtra("com.joshuawise.dumload.prompt", text);
				intent.setData((Uri.parse("suckit://"+SystemClock.elapsedRealtime())));
				
				PendingIntent contentIntent = PendingIntent.getActivity(thisupl, 0, intent, 0);
				notification.defaults |= Notification.DEFAULT_VIBRATE;
				notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
				notification.setLatestEventInfo(getApplicationContext(), "I've been had!", "Dumload needs your input.", contentIntent);
				
				Log.e("Dumload.Uploader[thread]", "Notifying...");
				
				mNotificationManager.notify(bogon, notification);
				
				Log.e("Dumload.Uploader[thread]", "About to go to 'sleep'...");
				Looper.loop();
				Log.e("Dumload.Uploader[thread]", "And we're alive!");
					
				Log.e("Dumload.Uploader[thread]", "result was: "+(Integer.toString(msg.arg1)));
				
				mNotificationManager.cancel(bogon);
			}
		};
		
		t.start();
		try {
			t.join();
		} catch (Exception e) {
			return null;
		}
		
		if (type.equals("yesno"))
			return new Boolean(msg.arg1 == 1);
		else if (type.equals("message"))
			return null;
		else if (type.equals("password")) {
			if (msg.arg1 == 0)
				return null;
			Bundle b = msg.getData();
			return b.getString("response");
		} else
			return null;
	}
	
	/* UserInfo bits */
	String _password = null;
	public String getPassword()
	{
		return _password;
	}
	public boolean promptPassword(String message)
	{
		_password = (String)dance("password", message); 
		return (_password != null);
	}
	
	String _passphrase = null;
	public String getPassphrase()
	{
		return _passphrase;
	}
	public boolean promptPassphrase(String message)
	{
		_passphrase = (String)dance("password", message); 
		return (_passphrase != null);
	}
	
	public boolean promptYesNo(String str)
	{
		return ((Boolean)dance("yesno", str)).booleanValue();
	}
	
	public void showMessage(String str)
	{
		dance("message", str);
	}
	
	public String[] promptKeyboardInteractive(String dest, String name, String instr, String[] prompt, boolean[] echo)
	{
		int i;
		String [] responses = new String[prompt.length];
		
		Log.e("Dumload.Uploader", "dest: "+dest);
		Log.e("Dumload.Uploader", "name: "+name);
		Log.e("Dumload.Uploader", "instr: "+instr);
		for (i = 0; i < prompt.length; i++)
		{
			responses[i] = (String) dance("password", "[" + dest + "]\n" + prompt[i]);
		}
		return responses;
	}
	
	@Override
	public void run()
	{
		Looper.prepare();
		
		Log.e("Dumload.Uploader[thread]", "This brought to you from the new thread.");
		
		try {
			JSch jsch = new JSch();
			jsch.setKnownHosts(homedir + "/known_hosts");
			Session s = jsch.getSession("joshua", "nyus.joshuawise.com", 22);
			s.setUserInfo(this);
			s.connect();
			
			Channel channel = s.openChannel("exec");
			((ChannelExec)channel).setCommand("echo foo > /tmp/lol");
			channel.connect();
			
			dance("message", "done");

			channel.disconnect();
			s.disconnect();
		} catch (JSchException e) {
			Log.e("Dumload.uploader[thread]", "JSchException: "+(e.toString()));
		}
		
		
		Log.e("Dumload.uploader[thread]", "And now I'm back to life!");
	}
	
	private void say(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onStart(Intent i, int startId)
	{
		uri = i.getData();
		homedir = getApplicationContext().getFilesDir().getAbsolutePath();
		int shits = 0;
		
		super.onStart(i, startId);
		
		Log.e("Dumload.Uploader", "Started.");
		Log.e("Dumload.Uploader", "My path is "+homedir);
		
		try {
			InputStream is = getContentResolver().openInputStream(uri);
			shits = is.available();
		} catch (Exception e) {
		}
		
		say("Your shit was "+(Integer.toString(shits))+" bytes long");
		
		me = new Thread(this, "Uploader thread");
		me.start();
	}
	
	@Override
	public IBinder onBind(Intent i) {
		Log.e("Dumload.Uploader", "bound");
		
		return null;
	}
}
