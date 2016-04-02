package com.jingtian.mobileguardian.engine;

import java.util.ArrayList;
import java.util.List;

import com.jingtian.mobileguardian.domain.AppInfo;
import com.jingtian.mobileguardian.domain.DataInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;

/*
 * com.jingtian.mobileguardian.engine:
 * for providing system and apps' information
 */
public class DataInfoProvider {

	/**
	 * obtain information we need of all apps from system(phone)
	 * param context
	 * @return
	 */
	public static List<DataInfo> getDataInfos(Context context){
		//obtain a package manager
		PackageManager pm = context.getPackageManager();
				
		List<PackageInfo> list =  pm.getInstalledPackages(0);;
		
		//only store information we need to show on screen
		List<DataInfo> dataInfos = new ArrayList<DataInfo>();
		for (PackageInfo packageInfo : list) {
			DataInfo dataInfo = new DataInfo();
			
			String packageName = packageInfo.packageName;
			Drawable icon = packageInfo.applicationInfo.loadIcon(pm);
			String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
			int uid = packageInfo.applicationInfo.uid;
			long tx = TrafficStats.getUidTxBytes(uid); //bytes upload
			long rx = TrafficStats.getUidRxBytes(uid); //bytes download
			
			if (tx == 0 && rx == 0) {
				continue;
			}
			
			dataInfo.setPackageName(packageName);
			dataInfo.setProcessName(appName);
			dataInfo.setIcon(icon);
			dataInfo.setDownloadData(rx);
			dataInfo.setUploadData(tx);
			
			dataInfos.add(dataInfo);
		}
		
		return dataInfos;
	}
}
