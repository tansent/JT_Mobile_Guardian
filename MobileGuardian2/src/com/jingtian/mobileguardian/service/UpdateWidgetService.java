package com.jingtian.mobileguardian.service;

import java.util.Timer;
import java.util.TimerTask;

import com.jingtian.mobileguardian.R;
import com.jingtian.mobileguardian.receiver.MyWidget;
import com.jingtian.mobileguardian.utils.SystemInfoUtils;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {

	private ScreenOffReceiver offreceiver;
	private ScreenOnReceiver onreceiver;
	
	protected static final String TAG = "UpdateWidgetService";
	private Timer timer;
	private TimerTask task;
	
	private AppWidgetManager manager;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class ScreenOffReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.i("UpdateWidgetService", "screen lock...");
			stopTimer();
		}
	}
	private class ScreenOnReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.i("UpdateWidgetService", "sreen unlock...");
			startTimer();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		offreceiver = new ScreenOffReceiver();
		onreceiver = new ScreenOnReceiver();
		registerReceiver(onreceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(offreceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		manager = AppWidgetManager.getInstance(this);
		
		startTimer();
	}

	/**
	 * invoke every 3s when screen unlock
	 */
	private void startTimer() {
		if (timer ==null && task == null) {
			//using a Timer to update the widget manually(for every 3s)
			timer = new Timer();
			task = new TimerTask() {
				
				@Override
				public void run() {
//					Log.i(TAG, "update widget");
					
					//which component to operate
					ComponentName provider = new ComponentName(UpdateWidgetService.this, MyWidget.class);
					
					//communal places to share data to translate through different processes
					RemoteViews views = new RemoteViews(getPackageName(), R.layout.process_widget);
					
					views.setTextViewText(R.id.process_count, 
							"Running Processes: "+SystemInfoUtils.getRunningProcessesCount(getApplicationContext()));
					String memSize = Formatter.formatFileSize(getApplicationContext(), SystemInfoUtils.getAvailMemory(getApplicationContext()));
					views.setTextViewText(R.id.process_memory, 
							"Avail Memory: "+ memSize);
					
					
					//customize a broadcast
					Intent intent = new Intent();
					intent.setAction("com.jingtian.mobileguardian.killall");
					//a remote app will operate this(desktop will do)
					//sending a broadcast (or boot an activity, or boot a service)
					PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					views.setOnClickPendingIntent(R.id.btn_clear, pendingIntent);
					
					//updateAppWidget() must be the last 
					manager.updateAppWidget(provider, views);
				}
			};
			timer.schedule(task, 0, 3000); //first time, no delay; then, keep invoking in every 3s
		}
	}
	
	
	/**
	 * unregister all receivers
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(offreceiver);
		unregisterReceiver(onreceiver);
		offreceiver = null;
		onreceiver = null;
		stopTimer();
		
	}

	/**
	 * cancel timer and timerTask
	 */
	private void stopTimer() {
		if (timer !=null && task != null) {
			timer.cancel();
			task.cancel();
			timer = null;
			task = null;
		}
	}
	
	

}
