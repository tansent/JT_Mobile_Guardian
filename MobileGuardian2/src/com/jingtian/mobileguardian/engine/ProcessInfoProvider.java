package com.jingtian.mobileguardian.engine;

import java.util.ArrayList;
import java.util.List;

import com.jingtian.mobileguardian.R;
import com.jingtian.mobileguardian.domain.ProcessInfo;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Debug.MemoryInfo;

/*
 * provide process information of the phone
 */
public class ProcessInfoProvider {

	/**
	 * obtain the list of all processes
	 * @param context
	 * @return
	 */
	public static List<ProcessInfo> getProcessesInfo(Context context){
		//ActivityManager obtain current running files
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		
		//getRunningAppProcesses() only works in ActivityManager under version 5.0(Lollipop)
		List<RunningAppProcessInfo> processList = am.getRunningAppProcesses();
		
		//PackageManager is used to obtain information as name,icon... based on package name
		PackageManager pm = context.getPackageManager();
		
		List<ProcessInfo> results= new ArrayList<ProcessInfo>();
		for (RunningAppProcessInfo processInfo : processList) {
			ProcessInfo process = new ProcessInfo();
			
			//get app's unique package name
			String packageName = processInfo.processName;
			
			//memory(ActivityManager) will not get wrong, so leave it out from try-catch
			MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(new int[]{processInfo.pid});
			long memSize = memoryInfos[0].getTotalPrivateDirty() * 1024;
			process.setMemSize(memSize);
			try {
				//get manifest file of the app
//				pm.getPackageInfo(packageName, 0);
				ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
				
				Drawable icon = appInfo.loadIcon(pm);
				String processName = appInfo.loadLabel(pm).toString();
				
				process.setIcon(icon);
				process.setProcessName(processName);
				process.setPackageName(packageName);
				if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					//user app
					process.setUserProcess(true);
				}else {
					//system app
					process.setUserProcess(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				//some process package cannot be found(they are not written by java)
				//notice if there are illegal symbol in resource file, R file will go wrong
				process.setIcon(context.getResources().getDrawable(R.drawable.ic_default));
				process.setProcessName(packageName);
			}
			results.add(process);
		}
		
		return results;
	}
	
}
