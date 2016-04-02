package com.jingtian.mobileguardian;

import com.jingtian.mobileguardian.service.AddressService;
import com.jingtian.mobileguardian.service.CallSmsGuardianService;
import com.jingtian.mobileguardian.service.WatchDogService;
import com.jingtian.mobileguardian.ui.SettingClickView;
import com.jingtian.mobileguardian.ui.SettingItemView;
import com.jingtian.mobileguardian.utils.JudgeServiceUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

public class SettingCenterActivity extends Activity {

	// set update notification
	private SettingItemView siv_update;

	private SharedPreferences sp;

	// set phone geolocator
	private SettingItemView siv_show_address;

	private Intent showAddress;
	
	// set blockade function
	private SettingItemView siv_callsms_guardian;

	private Intent callSmsGuardian;
	
	// set watchdog function
	private SettingItemView siv_watchdog;

	private Intent watchDogIntent;
	
	// set geolocator background
	private SettingClickView scv_change_bg;
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settingcenter);

		sp = getSharedPreferences("config", MODE_PRIVATE);

		// initialize update notification
		siv_update = (SettingItemView) findViewById(R.id.siv_update);
		siv_update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Editor editor = sp.edit();

				if (siv_update.isChecked()) { // already checked
					siv_update.setChecked(false);
					// siv_update.setDesc("update notification off");
					editor.putBoolean("update", false);
				} else {
					siv_update.setChecked(true);
					// siv_update.setDesc("update notification on");
					editor.putBoolean("update", true);
				}
				editor.commit();
			}
		});

		// initialize phone geolocator
		siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
		showAddress = new Intent(this, AddressService.class);

		boolean isServicealive = JudgeServiceUtils.isAlive(SettingCenterActivity.this,
				"com.jingtian.mobileguardian.service.AddressService");

		if (isServicealive) {
			siv_show_address.setChecked(true);
		} else {
			siv_show_address.setChecked(false);

		}

		siv_show_address.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (siv_show_address.isChecked()) { // checked
					siv_show_address.setChecked(false);
					stopService(showAddress);
				} else {
					siv_show_address.setChecked(true);
					startService(showAddress);
				}

			}
		});

		boolean update = sp.getBoolean("update", false);
		if (update) {
			siv_update.setChecked(true);
			siv_update.setDesc("update notification on");

		} else {
			siv_update.setChecked(false);
			siv_update.setDesc("update notification off");
		}
		
		//set blockade number
		siv_callsms_guardian = (SettingItemView) findViewById(R.id.siv_callsms_guardian);
		callSmsGuardian = new Intent(this, CallSmsGuardianService.class);

//				boolean isServicealive = JudgeServiceUtils.isAlive(SettingCenterActivity.this,
//						"com.jingtian.mobileguardian.service.AddressService");
//
//				if (isServicealive) {
//					siv_show_address.setChecked(true);
//				} else {
//					siv_show_address.setChecked(false);
//
//				}

		siv_callsms_guardian.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (siv_callsms_guardian.isChecked()) { // checked
							siv_callsms_guardian.setChecked(false);
							stopService(callSmsGuardian);
						} else {
							siv_callsms_guardian.setChecked(true);
							startService(callSmsGuardian);
						}

					}
				});

//				boolean update = sp.getBoolean("update", false);
//				if (update) {
//					siv_update.setChecked(true);
//					siv_update.setDesc("update notification on");
//
//				} else {
//					siv_update.setChecked(false);
//					siv_update.setDesc("update notification off");
//				}
		//---------------
		
			
		//set watchdog function
//		siv_watchdog = (SettingItemView) findViewById(R.id.siv_watchdog);
//		watchDogIntent = new Intent(this, WatchDogService.class);
//
//		siv_watchdog.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (siv_watchdog.isChecked()) { // already checked (from checked -> unchecked)
//					siv_watchdog.setChecked(false);
//					stopService(watchDogIntent);
//				} else {
//					siv_watchdog.setChecked(true);
//					startService(watchDogIntent);
//				}
//
//			}
//		});
		
		siv_watchdog = (SettingItemView) findViewById(R.id.siv_watchdog);
		watchDogIntent = new Intent(this, WatchDogService.class);

		siv_watchdog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (siv_watchdog.isChecked()) { // already checked (from checked -> unchecked)
					siv_watchdog.setChecked(false);
					stopService(watchDogIntent);
				} 
				else { //from unchecked -> checked
					Intent intent = new Intent(getApplicationContext(), EnterPwdActivity.class);
					startActivity(intent);
					
					new Thread(){
						public void run() {
							try {
								//not letting user see checkbox changes before setting the ping
								Thread.sleep(500); 
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							runOnUiThread(new Runnable() {
								public void run() {
									
									String ping = sp.getString("ping", null);
									if (!TextUtils.isEmpty(ping)) {
										siv_watchdog.setChecked(true);
										startService(watchDogIntent);
									}
								}
							});
							
							
						};
					}.start();
					
				}

			}
			
		});
			
		
		//set geolocator background
		scv_change_bg = (SettingClickView) findViewById(R.id.scv_change_bg);
//		set attribute outside will overwrite the internal(SettingClickView) attributes 
//		scv_change_bg.setTitle("Setting Address Style");
		
		final String[] styles = {"translucent","Cadmium Orange","Bright cerulean","Metal Grey","Apple Green"};
		int initStyleIndex = sp.getInt("style_index", 0);
		scv_change_bg.setDesc(styles[initStyleIndex]);
		
		scv_change_bg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int initDialogStyleIndex = sp.getInt("style_index", 0);
				
				//pop up a dialog
				AlertDialog.Builder builder = new Builder(SettingCenterActivity.this);
				builder.setTitle("Set geolocator style");
				//second para: default value
				builder.setSingleChoiceItems(styles, initDialogStyleIndex, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) { //which: the one choosen
						
						//save the choice
						Editor editor =  sp.edit();
						editor.putInt("style_index", which);
						editor.commit();
						
						scv_change_bg.setDesc(styles[which]);
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("Cancel", null);
				builder.show();
			}
		});
		
	}

	/**
	 * from 2nd time forward, invoke onResume() 
	 * (onStart can also work, but this one is even safer)
	 * (restore from losing focus status)
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// initialize phone geolocator
		siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
		showAddress = new Intent(this, AddressService.class);

		boolean isServicealive = JudgeServiceUtils.isAlive(SettingCenterActivity.this,
				"com.jingtian.mobileguardian.service.AddressService");

		if (isServicealive) {
			siv_show_address.setChecked(true);
		} else {
			siv_show_address.setChecked(false);
		}
		
		// initialize number blockade
		boolean isPhoneServicealive = JudgeServiceUtils.isAlive(SettingCenterActivity.this,
				"com.jingtian.mobileguardian.service.CallSmsGuardianService");
		siv_callsms_guardian.setChecked(isPhoneServicealive);

		// initialize watch dog
		boolean isWatchdogServicealive = JudgeServiceUtils.isAlive(SettingCenterActivity.this,
				"com.jingtian.mobileguardian.service.WatchDogService");
		siv_watchdog.setChecked(isWatchdogServicealive);
	}
	
	/**
	 * back from EnterPwdActivity (set ping page), 
	 * we need to have the checkbox changes and service starts immediately
	 */
	@Override
	protected void onStart() {
		super.onStart();
		
		String ping = sp.getString("ping", null);
		if (!TextUtils.isEmpty(ping)) {
			siv_watchdog.setChecked(true);
			startService(watchDogIntent);
		}
	}
}
