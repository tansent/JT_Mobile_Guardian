package com.jingtian.mobileguardian;

import java.util.Timer;
import java.util.TimerTask;

import com.jingtian.mobileguardian.service.AutoCleanService;
import com.jingtian.mobileguardian.utils.JudgeServiceUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingProcessesActivity extends Activity {

	private CheckBox cb_show_system;
	private CheckBox cb_auto_clean;
	
	/**
	 * use for global & static parameter storage
	 * for display, not for services
	 */
	private SharedPreferences sp; 
	
//	Timer timer;
//	TimerTask task;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_setting_processes);
		
		cb_show_system = (CheckBox) findViewById(R.id.cb_show_system);
		cb_auto_clean = (CheckBox) findViewById(R.id.cb_auto_clean);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		//show state to screen
		cb_show_system.setChecked(sp.getBoolean("show_system", false));
		
		//check change
		cb_show_system.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor = sp.edit();
				//boolean isChecked : current checkbox's status
				editor.putBoolean("show_system", isChecked);
				editor.commit();
			}
		});
		
		cb_auto_clean.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//when lock screen, a broadcast will be sent. 
				//this broadcast can only work by registering through code (not manifest file)
				
				//thus, define a service to constantly listen the broadcast
				Intent intent = new Intent(getApplicationContext(), AutoCleanService.class);
				if (isChecked) {
					startService(intent);
				}else {
					stopService(intent);
				}
			}
		});
		
		//methods to set timer 
//1		timer.schedule(task, delay, period);
		
		//3000: time before finifh ;  1000: interval time 
//2		CountDownTimer cdt = new CountDownTimer(3000, 1000) {
//			
//			@Override
//			public void onTick(long millisUntilFinished) {
//				System.out.println(3000);
//			}
//			
//			@Override
//			public void onFinish() {
//				System.out.println("finished");
//			}
//		};
//		cdt.start();
	}
	
	/**
	 * when showing screen, for the first time and the following time
	 */
	@Override
	protected void onStart() {
		boolean isServiceRun = JudgeServiceUtils.isAlive(getApplicationContext(), 
				"com.jingtian.mobileguardian.service.AutoCleanService");
		cb_auto_clean.setChecked(isServiceRun);
		
		super.onStart();
	}
}
