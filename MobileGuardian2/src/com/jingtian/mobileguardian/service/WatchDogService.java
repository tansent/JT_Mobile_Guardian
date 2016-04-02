package com.jingtian.mobileguardian.service;

import java.util.List;

import com.jingtian.mobileguardian.ActivityWatchDog;
import com.jingtian.mobileguardian.db.dao.AppLockDAO;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

/*
 * need permission GET_TASKS
 */
public class WatchDogService extends Service {

	private ActivityManager am; //using ActivityManager to get current running tasks(top 1 is the one on the screen)
	private boolean flag; //watchdog(lock) status
	private AppLockDAO lockDAO;
	private InnerReceiver innerReceiver;
	private String tempstopprotect;
	private ScreenOffReceiver offreceiver;
	private ScreenOnReceiver onreceiver;
	private LockListChange lockListChangeReceiver;
	
	private List<String> protectPackNames;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private Intent intent;
	
	/**
	 * clear lock temp-protection when screen off(every use time is a block)
	 *
	 */
	private class ScreenOffReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			tempstopprotect = null;
			
			flag = false; //save electricity
		}
	}
	
	/**
	 * only when screen on do we need to activate the lock
	 *
	 */
	private class ScreenOnReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			flag = true;
		}
	}
	
	/**
	 * what will this class do when receive the 
	 * "com.jingtian.mobileguardian.locktempstop" broadcast
	 *
	 */
	private class InnerReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("broadcast received!!!");
			tempstopprotect = intent.getStringExtra("packname");
		}
	}
	
	/**
	 * lock list change
	 *
	 */
	private class LockListChange extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			protectPackNames = lockDAO.searchAll();
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		innerReceiver = new InnerReceiver();
		registerReceiver(innerReceiver, new IntentFilter("com.jingtian.mobileguardian.locktempstop"));
		
		offreceiver = new ScreenOffReceiver();
		registerReceiver(offreceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		lockListChangeReceiver = new LockListChange();
		registerReceiver(lockListChangeReceiver, new IntentFilter("com.jingtian.mobileguardian.locklistchange"));
		
		flag = true;
		lockDAO = new AppLockDAO(this);
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		
		//search and store things first (this list is in CPU, judging will be much faster)
		protectPackNames = lockDAO.searchAll();

		//current app needs protection, the watchdog page should be at front
		intent  = new Intent(getApplicationContext(), ActivityWatchDog.class);
		//Service does not have task information, so must be designated using flag
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//set package Name
		
		
		//try to prepare search database and new objects before loop and thread
		new Thread(){
			public void run() {
				
				while(flag){
//					System.out.println("screen lock on");  //print log costs time!
					
					//getRunningTasks can get the app stack. more recent, more on the top
					//need permission GET_TASKS
					List<RunningTaskInfo> appStack= am.getRunningTasks(1); //get top 100 (1 is sufficient)
					
					//get the current screen activiy (most current activity of the most current app)
					String packName = appStack.get(0).topActivity.getPackageName();
					boolean t = protectPackNames.contains(packName);
					if(protectPackNames.contains(packName)){ //lockDAO.search(packName)
						
						//use data in the CPU is way faster than search database
						if (packName.equals(tempstopprotect)) {
							//do nothing
							// if the app is temp-protected, watchdog will continuously do nothing
						
							//after button click, do nothing means finish current activity
						} else {
							// put "new intent", out of the loop 
							
							intent.putExtra("packname", packName);
//							intent.putExtra("ping", value)
							startActivity(intent);
							//launchMode="singleInstance (one activity, one stack)
						}
					}
					try {
						Thread.sleep(20); //in while(true), the system needs rest
						// sleep shorter can make app run faster
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		flag = false;
		
		unregisterReceiver(innerReceiver);
		innerReceiver = null;
		
		unregisterReceiver(offreceiver);
		offreceiver = null;
		
		unregisterReceiver(lockListChangeReceiver);
		lockListChangeReceiver = null;
	}

}
