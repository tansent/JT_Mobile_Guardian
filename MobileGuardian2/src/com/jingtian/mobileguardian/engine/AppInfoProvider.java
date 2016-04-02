package com.jingtian.mobileguardian.engine;

import java.util.ArrayList;
import java.util.List;

import com.jingtian.mobileguardian.domain.AppInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/*
 * com.jingtian.mobileguardian.engine:
 * for providing system and apps' information
 */
public class AppInfoProvider {

	/**
	 * obtain information we need of all apps from system(phone)
	 * param context
	 * @return
	 */
	public static List<AppInfo> getAppInfos(Context context){
		PackageManager manager = context.getPackageManager();
		
		//obtain all app's manifest files 
		List<PackageInfo> packageInfos = manager.getInstalledPackages(0);
		//only store information we need to show on screen
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		for (PackageInfo packageInfo : packageInfos) {
			AppInfo appInfo = new AppInfo();
			
			String packageName = packageInfo.packageName;
			Drawable icon = packageInfo.applicationInfo.loadIcon(manager);
			String appName = packageInfo.applicationInfo.loadLabel(manager).toString();
			
			//obtain app status
			int flag = packageInfo.applicationInfo.flags; //this int value can be viewed as the combo status
			
			//once the app is installed, uid will be assigned and stay unchange
			int uid = packageInfo.applicationInfo.uid;
			appInfo.setUid(uid);
			
			if ((flag & ApplicationInfo.FLAG_SYSTEM) == 0) {
				//user app 
				appInfo.setUserApp(true);
			}else {
				//sytem app
				appInfo.setUserApp(false);
			}
			
			if ((flag & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
				// app stored in rom (data/data/package)
				appInfo.setAppInRom(true);
			}else {
				// app stored in SD card
				appInfo.setAppInRom(false);
			}
			
			
			
			appInfo.setPackageName(packageName);
			appInfo.setAppName(appName);
			appInfo.setIcon(icon);
			
			appInfos.add(appInfo);
		}
		
		return appInfos;
	}
}
