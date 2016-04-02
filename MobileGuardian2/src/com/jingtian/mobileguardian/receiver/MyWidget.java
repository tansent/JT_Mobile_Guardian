package com.jingtian.mobileguardian.receiver;

import com.jingtian.mobileguardian.service.UpdateWidgetService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/*
 * The widget is basically a broadcast receiver, which needs to be configured in manifest file:
 
 <receiver android:name="com.jingtian.mobileguardian.receiver.MyWidget" >
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>

            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/process_widget_provider" />
        </receiver>
 
 */
public class MyWidget extends AppWidgetProvider {

	/**
	 * as long as the widget is operated, onReceive() will be invoked
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		//no matter what user does, boot the service
		Intent intentt = new Intent(context, UpdateWidgetService.class);
		context.startService(intentt);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	/**
	 * the first time invokes widget (invoke first widget)
	 */
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Intent intent = new Intent(context, UpdateWidgetService.class);
		context.startService(intent);
	}

	/**
	 * delete last widget
	 */
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Intent intent = new Intent(context, UpdateWidgetService.class);
		context.stopService(intent);
	}

	
}
