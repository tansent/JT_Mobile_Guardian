package com.jingtian.mobileguardian.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageManager;

/*
 * Running app utils
 */
public class SystemInfoUtils {

	/**
	 * get running processes count
	 * @param context: using context can get access to all under layer's services and controllers
	 * @return
	 */
	public static int getRunningProcessesCount(Context context){
//		PackageManager == control panel, it has all apps' resource, no matter the app is running or not
//		ActivityManager == process manager, it only has running apps' resource
		
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningApplist = am.getRunningAppProcesses();
		return runningApplist.size();
	}
	
	/**
	 * obtain memory that is used on current booted apps
	 * @param context
	 * @return
	 */
	public static long getAvailMemory(Context context){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo memoryInfo = new MemoryInfo();
		am.getMemoryInfo(memoryInfo); //?
		return memoryInfo.availMem ;
	}
	
	/**
	 * obtain total memory
	 * @param context
	 * @return
	 */
	public static long getTotalMemory(Context context){
		/* only work above API 4.0
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo memoryInfo = new MemoryInfo();
		am.getMemoryInfo(memoryInfo); 
		return memoryInfo.totalMem;
		*/
		try {
			File file = new File("/proc/meminfo");
			FileInputStream fis = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			String line = reader.readLine();
			//MemTotal:			513000 KB
			StringBuffer sb = new StringBuffer();
			for (char c : line.toCharArray()) {
				if (c>='0' && c<='9') {
					sb.append(c);
				}
			}
			return Long.parseLong(sb.toString()) * 1024;
		} catch (Exception e) {
			e.printStackTrace();
			return 0; //no posting error. The file must exist
		}
		
	}
	
	
	public static int getAvailTotalMemPercent(Context context){
		long memAvail = getAvailMemory(context);
		long memTotal = getTotalMemory(context);
		long percent = (memAvail * 100 / memTotal) ;
//		return (int) (getAvailMemory(context) * 100 / getTotalMemory(context)) ;
		return (int) percent;
	}
}
