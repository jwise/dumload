/* NotifSlave.java
 * Notification interface glue for Dumload.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License, version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.joshuawise.dumload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NotifSlave extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
//		TextView tv = (TextView)findViewById(R.id.suckit);
//		tv.setText("Suck it.");
	}
	
	private void say(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
	
	private int _nextdialog = 0;
	private Dialog _hell_ass_balls = null;
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		Log.e("DumLoad.NotifSlave", "Create for dialog "+(Integer.toString(id)));
		if (id != _nextdialog)
			return null;
		return _hell_ass_balls;
	}
	
	private void showDialog(Dialog d)
	{
		_nextdialog++;
		_hell_ass_balls = d;
		Log.e("DumLoad.NotifSlave", "Attempting to show dialog "+(Integer.toString(_nextdialog)));
		showDialog(_nextdialog);
	}
	
	public void onStart() {
		super.onStart();
	
		Intent i = getIntent(); /* i *am* not an intent! */
		final Activity thisact = this;
		
		final Messenger m = (Messenger)i.getParcelableExtra("com.joshuawise.dumload.returnmessenger");
		String reqtype = i.getStringExtra("com.joshuawise.dumload.reqtype");
		String prompt = i.getStringExtra("com.joshuawise.dumload.prompt");
		
		if (prompt == null || reqtype == null || m == null)	/* i.e., we got called by a dummy notification */
		{
			this.finish();
			return;
		}
	
		if (reqtype.equals("yesno")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Dumload");
			builder.setMessage(prompt);
			builder.setCancelable(false);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Log.e("Dumload.NotifSlave", "Responding with a 1.");
					try {
						Message me = Message.obtain();
						me.arg1 = 1;
						m.send(me);
					} catch (Exception e) {
						Log.e("Dumload.NotifSlave", "Failed to send a message back to my buddy.");
					}
					dialog.cancel();
					thisact.finish();
				}
			});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Log.e("Dumload.NotifSlave", "Responding with a 1.");
					try {
						Message me = Message.obtain();
						me.arg1 = 0;
						m.send(me);
					} catch (Exception e) {
						Log.e("Dumload.NotifSlave", "Failed to send a message back to my buddy.");
					}
					dialog.cancel();
					thisact.finish();
				}
			});
			AlertDialog alert = builder.create();
			showDialog(alert);
		} else if (reqtype.equals("message")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Dumload");
			builder.setMessage(prompt);
			builder.setCancelable(false);
			builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					try {
						Message me = Message.obtain();
						m.send(me);
					} catch (Exception e) {
						Log.e("Dumload.NotifSlave", "Failed to send a message back to my buddy.");
					}
					dialog.cancel();
					thisact.finish();
				}
			});
			AlertDialog alert = builder.create();
			showDialog(alert);
		} else if (reqtype.equals("password")) {
			final Dialog d = new Dialog(this);
			
			d.setContentView(R.layout.passwd);
			d.setTitle("Dumload");
			d.setCancelable(false);
			
			TextView text = (TextView) d.findViewById(R.id.prompt);
			text.setText(prompt);
			
			Button ok = (Button) d.findViewById(R.id.ok);
			ok.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						Message me = Message.obtain();
						me.arg1 = 1;
						TextView entry = (TextView) d.findViewById(R.id.entry);
						Bundle b = new Bundle(1);
						b.putString("response", entry.getText().toString());
						me.setData(b);
						m.send(me);
					} catch (Exception e) {
						Log.e("Dumload.NotifSlave", "Failed to send a message back to my buddy.");
					}
					d.cancel();
					thisact.finish();
				}
			});
			
			Button cancel = (Button) d.findViewById(R.id.cancel);
			cancel.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						Message me = Message.obtain();
						me.arg1 = 0;
						m.send(me);
					} catch (Exception e) {
						Log.e("Dumload.NotifSlave", "Failed to send a message back to my buddy.");
					}
					d.cancel();
					thisact.finish();
				}
			});
			
			
			showDialog(d);
		} else {
			Log.e("Dumload.NotifSlave", "What's a "+reqtype+"?");
		}
	}
}