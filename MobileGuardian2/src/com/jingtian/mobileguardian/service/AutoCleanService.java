package com.jingtian.mobileguardian.service;

import java.util.List;

import com.jingtian.mobileguardian.domain.ProcessInfo;
import com.jingtian.mobileguardian.engine.ProcessInfoProvider;
import com.jingtian.mobileguardian.engine.ProcessInfoProvider2;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class AutoCleanService extends Service {

	private ScreenOffReceiver receiver;
	private ActivityManager am;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		
		receiver = new ScreenOffReceiver();
		//when registering a broadcast receiver, it is time to designate how the BroadcastReceiver receive
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(receiver);
		receiver = null;
	}

	private List<ProcessInfo> processInfos;
	private class ScreenOffReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("AutoCleanService", "screen locked");
			
			/* getRunningAppProcesses require a version under 5.0 */
//			List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
//			for (RunningAppProcessInfo runningAppProcessInfo : processes) {
//				am.killBackgroundProcesses(runningAppProcessInfo.processName);
//			}
			
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
			    // Do something for LOLLIPOP and above versions
				processInfos = ProcessInfoProvider2.getTaskInfos1(getApplicationContext());
			} else{
			    // do something for phones running an SDK before JELLY_BEAN
				processInfos = ProcessInfoProvider.getProcessesInfo(getApplicationContext());
			}
			for (ProcessInfo processInfo : processInfos) {
				am.killBackgroundProcesses(processInfo.getProcessName());
			}
		}
	}
}
