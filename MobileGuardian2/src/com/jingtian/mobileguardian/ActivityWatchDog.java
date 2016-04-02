package com.jingtian.mobileguardian;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityWatchDog extends Activity {

	private EditText et_setup_pwd;
	private String packName;
	private TextView tv_watchdog_appname;
	private ImageView iv_watchdog_icon;
	
	private SharedPreferences sp;
	private String ping;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watchdog);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		ping = sp.getString("ping", "");
		
		et_setup_pwd = (EditText) findViewById(R.id.et_watchdog_pwd);
		tv_watchdog_appname = (TextView) findViewById(R.id.tv_watchdog_appname);
		iv_watchdog_icon = (ImageView) findViewById(R.id.iv_watchdog_icon);
		
		Intent intent = getIntent();
		packName = intent.getStringExtra("packname"); //package name that is needed to protect
		
		PackageManager pm = getPackageManager();
		try {
			ApplicationInfo info = pm.getApplicationInfo(packName, 0);
			tv_watchdog_appname.setText(info.loadLabel(pm));
			iv_watchdog_icon.setImageDrawable(info.loadIcon(pm));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * to avoid others take a glance of the locked app
	 */
	@Override
	public void onBackPressed() {//its original super method has been removed
		//back to desktop
//      <action android:name="android.intent.action.MAIN" />
//      <category android:name="android.intent.category.HOME" />
//      <category android:name="android.intent.category.DEFAULT" />
//      <category android:name="android.intent.category.MONKEY"/>
		Intent intent = new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addCategory("android.intent.category.MONKEY");
		startActivity(intent);
		//home button: all apps minimize,
		// onDestory function will not be executed, 
		// only onStop will be executed (package name will not change)
	}
	@Override
	protected void onStop() {
		super.onStop();
		finish(); //finish all activities
	}
	
	public void watchdogPassword(View view){
		String psw = et_setup_pwd.getText().toString().trim();
		if (TextUtils.isEmpty(psw) ) {
			Toast.makeText(this, "passwords cannot be null", 0).show();
			return;
		}
		if (ping.equals(psw)) {
			//using customized broadcast to notify other service/activity about what to do
			Intent intent = new Intent();
			intent.setAction("com.jingtian.mobileguardian.locktempstop");
			intent.putExtra("packname", packName);
			sendBroadcast(intent);
			
			finish(); //cancel current page
		}else {
			Toast.makeText(this, "passwords incorrect", 0).show();
			return;
		}
		
	}
	
}
