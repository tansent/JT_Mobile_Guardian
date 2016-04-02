package com.jingtian.mobileguardian;


import java.util.ArrayList;
import java.util.List;

import com.jingtian.mobileguardian.domain.ProcessInfo;
import com.jingtian.mobileguardian.engine.ProcessInfoProvider;
import com.jingtian.mobileguardian.engine.ProcessInfoProvider2;
import com.jingtian.mobileguardian.utils.SystemInfoUtils;
import com.lidroid.xutils.db.annotation.Check;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProcessMonitor extends Activity {

	private TextView tv_process_count;
	private TextView tv_mem_info;
	private LinearLayout ll_loading;
	private ListView lv_process_monitor;
	private TextView tv_status;
	private List<ProcessInfo> processInfos;
	private List<ProcessInfo> userProcessList;
	private List<ProcessInfo> systemProcessList;
	private ProcessMonitorAdapter adapter;
	
	private int runningAppCount; 
	private long memAvail;
	private long memTotal;
	private int memPercent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process_monitor);
		
		tv_process_count = (TextView) findViewById(R.id.tv_process_count);
		tv_mem_info = (TextView) findViewById(R.id.tv_mem_info);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		lv_process_monitor = (ListView) findViewById(R.id.lv_process_monitor);
		tv_status = (TextView) findViewById(R.id.tv_status);
		
		//set app number and memory situation---------
//		setTitle();  ->  obtainAppsAndShow();

		//obtain all processes info and show them on the screen
		obtainAppsAndShow();
		
		//set tags
		lv_process_monitor.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			//invoke while scrolling
			//firstVisibleItem: first view's position on the screen
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (userProcessList!=null && systemProcessList!=null) {
					if (firstVisibleItem > userProcessList.size()) {
						tv_status.setText("System Processes: "+ systemProcessList.size());
					}else {
						tv_status.setText("User Processes: "+ userProcessList.size());
					}
				}
			}
		});
		
		//wherever it clicks, it can click
		lv_process_monitor.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//setting data
				ProcessInfo process = new ProcessInfo();
				if (position == 0) { //user_tag
					return;
				}else if (position == (1 + userProcessList.size())) {
					return;
				}else if (position <= userProcessList.size() ) { //user process  (1 + userInfoList.size())
					int newPosition = position - 1;
					process = userProcessList.get(newPosition);
				}else { //system process
					int newPosition = position - 1 - userProcessList.size() - 1;
					process = systemProcessList.get(newPosition);
				}
//				System.out.println("------------------"+process.toString());
				
				//filter our own process itself (make it unclickable)
				if (getPackageName().equals( process.getPackageName()) ) {
					return;
				}
				//setting views
				
				ViewHolder holder = (ViewHolder) view.getTag();
				if (process.isChecked()) {
					process.setChecked(false);
					holder.cb_checked.setChecked(false);
				} else {
					process.setChecked(true);
					holder.cb_checked.setChecked(true);
				}
			}
		});
		
	}

	private void setTitle() {
		//Formatter.formatFileSize(context, number)   long -> String
		runningAppCount = SystemInfoUtils.getRunningProcessesCount(this);
		memAvail = SystemInfoUtils.getAvailMemory(this);
		memTotal = SystemInfoUtils.getTotalMemory(this); 
		memPercent = SystemInfoUtils.getAvailTotalMemPercent(this);
		tv_process_count.setText("Running Processes:" + Integer.toString(runningAppCount));
		tv_mem_info.setText("Free Mem:" +
		Formatter.formatFileSize(this, memAvail)+ "/" + Formatter.formatFileSize(this, memTotal)
		);
		//tv_mem_info.setText("Memory: " +memPercent + "%");
	}

	private void obtainAppsAndShow() {
		ll_loading.setVisibility(View.VISIBLE);
		
		// obtaining a list of processes takes time
		new Thread(){
			public void run() {
				int sysVersion = Integer.parseInt(VERSION.SDK);  
				
//				if (sysVersion >= 5) {
//					processInfos = ProcessInfoProvider2.getTaskInfos1(getApplicationContext());
//				}else {
//					processInfos = ProcessInfoProvider.getProcessesInfo(getApplicationContext());
//				}
				
				//whether android.os.Build.VERSION_CODES has LOLLIPOP depends on the target sdk
				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
				    // Do something for LOLLIPOP and above versions
					processInfos = ProcessInfoProvider2.getTaskInfos1(getApplicationContext());
				} else{
				    // do something for phones running an SDK before JELLY_BEAN
					processInfos = ProcessInfoProvider.getProcessesInfo(getApplicationContext());
				}
				
//				processInfos = ProcessInfoProvider.getProcessesInfo(getApplicationContext());
//				processInfos = ProcessInfoProvider2.getTaskInfos1(getApplicationContext());
				
				userProcessList = new ArrayList<ProcessInfo>();
				systemProcessList = new ArrayList<ProcessInfo>();
				
				for (ProcessInfo process : processInfos) {
					if (process.isUserProcess()) {
						userProcessList.add(process);
					} else {
						systemProcessList.add(process);
					}
				}
				
				runOnUiThread(new Runnable() {
					public void run() {
						if (adapter == null) {
							adapter = new ProcessMonitorAdapter();
							lv_process_monitor.setAdapter(adapter);
						} else {
							adapter.notifyDataSetChanged(); //will not create new items
						}
						ll_loading.setVisibility(View.INVISIBLE);
						setTitle();
					}
				});
			};
		}.start();
		
	}
	
	private class ProcessMonitorAdapter extends BaseAdapter{
		private static final String TAG = "ProcessMonitorAdapter";

		@Override
		public int getCount() {
			SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
			if (sp.getBoolean("show_system", false)) {
				return 1 + userProcessList.size() + 1 + systemProcessList.size();
			}
			else {
				return 1 + userProcessList.size();
			}
			
			
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			//find view
			View view;
			ViewHolder holder;
			if (convertView != null && convertView instanceof RelativeLayout) { //only customized RelativeLayout should be reused 
				view = convertView; //convertView the unseen(used) objects
				
				//use the result
				holder = (ViewHolder) view.getTag(); //5
				
				Log.i(TAG, "reuse item" + position);
			}
			else {
				view = View.inflate(getApplicationContext(), R.layout.list_item_process_info, null); 
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_process_icon);
				holder.tv_mem = (TextView) view.findViewById(R.id.tv_process_memory);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_process_name);
				holder.cb_checked = (CheckBox) view.findViewById(R.id.cb_checked);
				//store the result
				view.setTag(holder);
				Log.i(TAG, "create item" + position);
			}
			
			
			//fill data
			ProcessInfo process;
			if (position == 0) { //user_tag
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(Color.argb(80, 133, 227, 150));
				tv.setText("User Processes: "+ userProcessList.size());
				return tv;
			}else if (position == (1 + userProcessList.size())) {
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(Color.argb(80, 133, 227, 150));
				tv.setText("System Apps: "+ systemProcessList.size());
				return tv;
			}else if (position <= userProcessList.size() ) { //user app  (1 + userInfoList.size())
				int newPosition = position - 1;
				process = userProcessList.get(newPosition);
			}else { //system app
				int newPosition = position - 1 - userProcessList.size() - 1;
				process = systemProcessList.get(newPosition);
			}
			holder.iv_icon.setImageDrawable(process.getIcon());
			holder.tv_name.setText(process.getProcessName());
			holder.tv_mem.setText("RAM Size: " + Formatter.formatFileSize(getApplicationContext(),process.getMemSize() ));
			holder.cb_checked.setChecked(process.isChecked());
			
			//filter out Mobile Guaridan process itself
			if (getPackageName().equals( process.getPackageName()) ) {
				holder.cb_checked.setVisibility(View.INVISIBLE);
			}
			else {
				//when reusing Mobile Guaridan item(convertView), make it visible
				holder.cb_checked.setVisibility(View.VISIBLE);
			}
			
			return view;
		}
		

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	/**
	 * container of View
	 * recording the result
	 */
	static class ViewHolder{
		TextView tv_name;
		TextView tv_mem;
		ImageView iv_icon;
		CheckBox cb_checked;
	}
	
	/**
	 * 
	 * @param view
	 */
	public void selectAll(View view){
		for (ProcessInfo process : processInfos) {
			//filter our own process itself
			if (getPackageName().equals( process.getPackageName()) ) {
				continue;
			}
			
			process.setChecked(true);
		}
		adapter.notifyDataSetChanged();
	}
	
	public void selectOppo(View view){
		for (ProcessInfo process : processInfos) {
			//filter our own process itself
			if (getPackageName().equals( process.getPackageName()) ) {
				continue;
			}
			
			process.setChecked(!process.isChecked());
		}
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * kill all selected processes
	 * need permission: KILL_BACKGROUND_PROCESSES
	 * @param view
	 */
	public void clearAll(View view){
		if (processInfos == null) {
			return;
		}
		
		//ActivityManager to kill processes
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		
		int count = 0;
		int savedMem = 0;
		//to record those killed processes
		List<ProcessInfo> killedList = new ArrayList<ProcessInfo>();
		for (ProcessInfo process : processInfos) {
			if (process.isChecked()) {
				//need permission KILL_BACKGROUND_PROCESSES
				am.killBackgroundProcesses(process.getPackageName());
				if (process.isUserProcess()) {
					userProcessList.remove(process);
				} else {
					systemProcessList.remove(process);
				}
				count++;
				savedMem += process.getMemSize();
				killedList.add(process);
			}
		}
		processInfos.removeAll(killedList);
		adapter.notifyDataSetChanged(); //notify the screen to refresh data
		Toast.makeText(getApplicationContext(), 
				"Clear "+killedList.size()+" processes. " + "Release "+Formatter.formatFileSize(this, savedMem)+" memory"
				,
				0).show();
		runningAppCount -= count;
		memAvail += savedMem;
		tv_process_count.setText("Running Processes:" + Integer.toString(runningAppCount));
		tv_mem_info.setText("Free Mem:" +
				Formatter.formatFileSize(this, memAvail)+ "/" + Formatter.formatFileSize(this, memTotal)
				);
		//cannot use notifyDataSetChanged(), must create new; because items changed
		//obtainAppsAndShow(); 
	}
	
	/*
	 	public void clearAll(View view){
		//ActivityManager to kill processes
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (ProcessInfo process : processInfos) {
			if (process.isChecked()) {
				//need permission KILL_BACKGROUND_PROCESSES
				am.killBackgroundProcesses(process.getPackageName());
			}
		}
		//cannot use notifyDataSetChanged(), must create new; because items changed
		obtainAppsAndShow(); 
	}
	
	 * */
	
	public void enterSetting(View view){
		Intent intent = new Intent(this, SettingProcessesActivity.class);
//		startActivity(intent); //startActivity will not automatically refresh UI
		startActivityForResult(intent, 0); // when return, invoke onActivityResult()
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//what will be done when refreshing UI after get back from the previous activity
		
		//refresh UI
		adapter.notifyDataSetChanged();
		
//		super.onActivityResult(requestCode, resultCode, data);
	}
}
