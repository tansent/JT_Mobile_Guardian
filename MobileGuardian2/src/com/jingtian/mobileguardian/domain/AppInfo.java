package com.jingtian.mobileguardian.domain;

import android.graphics.drawable.Drawable;

/*
 * app info
 */
public class AppInfo {
	private Drawable icon;
	private String appName;
	private String packageName;
	private boolean isAppInRom;
	private boolean isUserApp;
	private int uid;
	
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public boolean isAppInRom() {
		return isAppInRom;
	}
	public void setAppInRom(boolean isAppInRom) {
		this.isAppInRom = isAppInRom;
	}
	public boolean isUserApp() {
		return isUserApp;
	}
	public void setUserApp(boolean isUserApp) {
		this.isUserApp = isUserApp;
	}
	@Override
	public String toString() {
		return "AppInfo [icon=" + icon + ", appName=" + appName + ", packageName=" + packageName + ", isAppInRom="
				+ isAppInRom + ", isUserApp=" + isUserApp + "]";
	}
	
	
}
