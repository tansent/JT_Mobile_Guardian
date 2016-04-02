package com.jingtian.mobileguardian.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class JudgeServiceUtils {

	/**
	 * judge if a service is still alive
	 */
	public static boolean isAlive(Context context, String serviceName){
		//ActivityManager also takes care of Service
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		
		//get up to 100[para] services running on the phone
		List<RunningServiceInfo> list = manager.getRunningServices(100); 
		for (RunningServiceInfo runningServiceInfo : list) {
			String name = runningServiceInfo.service.getClassName();
			if (serviceName.equals(name)) {  //service looked for exists
				return true;
			}
		}
		return false;
	}
}
