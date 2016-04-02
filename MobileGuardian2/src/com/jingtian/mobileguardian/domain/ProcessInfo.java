package com.jingtian.mobileguardian.domain;

import android.graphics.drawable.Drawable;

/*
 * process info bean
 */
public class ProcessInfo {
	private String packageName; //package name is unique for each process
	private String processName;
	private Drawable icon;
	private long memSize;
	private boolean isUserProcess;
	private boolean isChecked; //whether it is checked
	
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
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
	public long getMemSize() {
		return memSize;
	}
	public void setMemSize(long memSize) {
		this.memSize = memSize;
	}
	public boolean isUserProcess() {
		return isUserProcess;
	}
	public void setUserProcess(boolean isUserProcess) {
		this.isUserProcess = isUserProcess;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	@Override
	public String toString() {
		return "ProcessInfo [packageName=" + packageName + ", processName=" + processName + ", icon=" + icon
				+ ", memSize=" + memSize + ", isUserProcess=" + isUserProcess + ", isChecked=" + isChecked + "]";
	}
	
	
	
}
