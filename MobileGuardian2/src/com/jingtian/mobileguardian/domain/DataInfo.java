package com.jingtian.mobileguardian.domain;

import android.graphics.drawable.Drawable;

public class DataInfo {

	private String packageName;
	private String processName;
	private Drawable icon;
	private long downloadData;
	private long uploadData;
	
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public long getDownloadData() {
		return downloadData;
	}
	public void setDownloadData(long downloadData) {
		this.downloadData = downloadData;
	}
	public long getUploadData() {
		return uploadData;
	}
	public void setUploadData(long uploadData) {
		this.uploadData = uploadData;
	}
	@Override
	public String toString() {
		return "DataInfo [packageName=" + packageName + ", processName=" + processName + ", icon=" + icon
				+ ", downloadData=" + downloadData + ", uploadData=" + uploadData + "]";
	}
	
	
}
