package com.jingtian.mobileguardian;

import com.jingtian.mobileguardian.utils.SmsUtils;
import com.jingtian.mobileguardian.utils.SmsUtils.BackUpCallBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class AdvancedToolActivity extends Activity {
	
	protected static final String TAG = "AdvancedToolActivity";
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_advanced_tools);
	}
	
	/**
	 * after clicking, enter into the Geolocator page
	 * @param view
	 */
	public void numberQuery(View view){
		Intent intent = new Intent(this, NumberGeolocator.class);
		startActivity(intent);
	}
	
	/**
	 * back up sms of the phone
	 * @param view
	 */
	public void smsBackup(View view){
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); //default: .STYLE_SPINNER
		progressDialog.setMessage("Backing up messages");
		progressDialog.show();
		
		new Thread(){
			public void run() {
				try {
					SmsUtils.backupSms(AdvancedToolActivity.this, new BackUpCallBack() {
						
						@Override
						public void beforeBackup(int max) {
							progressDialog.setMax(max);
						}
						
						@Override
						public void onSmsBackup(int progress) {
							progressDialog.setProgress(progress);
						}
						
					});
					
					//runOnUiThread: easy way to change UI in a thread
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(AdvancedToolActivity.this, "SMS Backup success", 0).show();
						}
					});
					
				} catch (Exception e) {
					e.printStackTrace();
					
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(AdvancedToolActivity.this, "SMS Backup fail", 0).show();
						}
					});
				} finally {
					progressDialog.dismiss();
				}
			};
		}.start();
		
		
	}
	
	/**
	 * restore sms of the phone
	 * @param view
	 */
	int isSaveOriginal = 0; //0: has not selected   1:save original sms   2:not save
	public void smsRestore(View view){
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); //default: .STYLE_SPINNER
		progressDialog.setMessage("Restoring messages");
		
		
		//show a dialog to let user choose whether to keep original messages
		
		AlertDialog.Builder builder = new Builder(AdvancedToolActivity.this);
		builder.setTitle("Message");
		builder.setMessage("Keep original messages?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
									
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isSaveOriginal = 1;
			}
		});
		
		builder.setNegativeButton("No", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isSaveOriginal = 2;
			}
		}); 
		builder.show();
		//---------------------------------------
		
		new Thread(){
			
			public void run() {
				try {
					
					//halting while waiting for use's choice
					while(true){
						if(isSaveOriginal != 0){//user has made a decision
							runOnUiThread(new Runnable() {
								public void run() {
									progressDialog.show();
								}
							});
							break;
						}
//						Log.i(TAG, "in loop");
					}
					
					SmsUtils.restoreSMS(AdvancedToolActivity.this, isSaveOriginal, new BackUpCallBack() {
						
						@Override
						public void beforeBackup(int max) {
							progressDialog.setMax(max);
						}
						
						@Override
						public void onSmsBackup(int progress) {
							progressDialog.setProgress(progress);
						}
						
					} );
					
					//runOnUiThread: easy way to change UI in a thread
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(AdvancedToolActivity.this, "SMS Backup success", 0).show();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(AdvancedToolActivity.this, "SMS Restore Fail", 0).show();
						}
					});
				} finally {
					progressDialog.dismiss();
					isSaveOriginal = 0;
				}
			};
			
		}.start();
		
		
	}
}
