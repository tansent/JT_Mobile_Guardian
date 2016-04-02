package com.jingtian.mobileguardian;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityCacheCleaner extends Activity {

	private TextView tv_cache;
	private ProgressBar pb_cache;
	private PackageManager pm;
	private LinearLayout ll_cache_container;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cache_cleaner);
		tv_cache = (TextView) findViewById(R.id.tv_cache);
		pb_cache = (ProgressBar) findViewById(R.id.pb_cache);
		ll_cache_container = (LinearLayout) findViewById(R.id.ll_cache_container);
		
		scanCache();
	}

	/**
	 * scan all the cache information in the cache
	 */
	public void scanCache() {
		pm = getPackageManager();
		new Thread() {
			public void run() {
				// getPackageSizeInfo() is hidden, thus using reflect to obtain it
				// getPackageSizeInfo(): is used to obtain cache information
				Method getPackageSizeInfo = null;
//				Class<?> clazz = getClassLoader().loadClass("PackageManager");
				Method[] methods = PackageManager.class.getMethods();
				for (Method method : methods) {
					if ("getPackageSizeInfo".equals(method.getName())) {
						getPackageSizeInfo = method;
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				List<PackageInfo> infos = pm.getInstalledPackages(0);
				pb_cache.setMax(infos.size());
				int progress = 0;
				Random random = new Random();
				for (PackageInfo packageInfo : infos) {
					
					try {
						// invoke(): using reflect to invoke the original method.
						// 1st param: who invoke the method
						// 2nd... params: parameters needed in the method
						// (3rd param: how program does in the method)
						getPackageSizeInfo.invoke(pm, packageInfo.packageName, new MyPackageStatsObserver());
						// cheat sleep
						Thread.sleep(random.nextInt(50));
					} catch (Exception e) {
						e.printStackTrace();
					}
					progress++;
					pb_cache.setProgress(progress);

				}

				runOnUiThread(new Runnable() {
					public void run() {
						tv_cache.setText("Scan Complete...");
						Toast.makeText(getApplicationContext(), "Scan Complete", 0).show();
					}
				});
			};
		}.start();
	}

	/*
	 * IPackageStatsObserver.Stub - get from reflection need permission:
	 * GET_PACKAGE_SIZE this class is run on another thread
	 */
	class MyPackageStatsObserver extends IPackageStatsObserver.Stub {
		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
			final long cacheSize = pStats.cacheSize;
			long codeSize = pStats.codeSize;
			long dataSize = pStats.dataSize;
			final String packageName = pStats.packageName;
			final ApplicationInfo appInfo;
			try {
				appInfo = pm.getApplicationInfo(packageName, 0);

				runOnUiThread(new Runnable() {
					public void run() {
						tv_cache.setText("Scanning: " + appInfo.loadLabel(pm));
						if (cacheSize > 0) {
							View view = View.inflate(getApplicationContext(), R.layout.list_item_cache_cleaner, null);
							TextView tv_cache_name = (TextView) view.findViewById(R.id.tv_cache_name);
							tv_cache_name.setText(appInfo.loadLabel(pm));
							TextView tv_cache_size = (TextView) view.findViewById(R.id.tv_cache_size);
							tv_cache_size.setText(
									"cache size: " + Formatter.formatFileSize(getApplicationContext(), cacheSize));
							ImageView iv_cache_delete = (ImageView) view.findViewById(R.id.iv_cache_delete);
							
							iv_cache_delete.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									//deleteApplicationCacheFiles
									try {
										//reflect to obtain method
										Method method =PackageManager.class.getMethod("deleteApplicationCacheFiles", String.class,
												IPackageDataObserver.class);
										method.invoke(pm, packageName, new MyPackageDataObserver());
										
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
							ll_cache_container.addView(view, 0);
						}
					}

				});

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

		}
	}
	
	
	/*
	 * delete cache
	 * need Permission: android.permission.DELETE_CACHE_FILES
	 * But only system app can add this permission
	 * 
	 * 
	 */
	class MyPackageDataObserver extends IPackageDataObserver.Stub{
		@Override
		public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
			onRemoveCompleted(packageName, succeeded);
			
		}
		
	}
	
	/**
	 * clear all cache
	 * need permission: 
	 * @param view
	 */
	public void clearAll(View view){
//		//"freeStorageAndNotify"
//		Method[] methods = PackageManager.class.getMethods();
//		Method[] methods = PackageManager.class.getDeclaredMethods();
//		for (Method method : methods) {
////			System.out.println(method.getName());
//			Log.i("Cache", method.getName());
//			if ("freeStorageAndNotify".equals(method.getName())) {
////			if (method.getName().equals("freeStorageAndNotify")) {
//				try {
////					method.invoke(pm, Integer.MAX_VALUE, new MyPackageDataObserver.Stub(){
////						@Override
////						public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
////							System.out.println(succeeded);
////						}});
//					Toast.makeText(getApplicationContext(), "METHOD FOUND!", 0).show();
//					method.invoke(pm, Integer.MAX_VALUE, new MyPackageDataObserver());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
////			return;
//			break;
//		}
//		Toast.makeText(getApplicationContext(), "Cache clear!", 0).show();
		
		
		Method[] methods = PackageManager.class.getMethods();
		for(Method method:methods){
			Log.i("Cache", method.getName());
			if("freeStorageAndNotify".equals(method.getName())){
				try {
					method.invoke(pm, Integer.MAX_VALUE, new IPackageDataObserver.Stub() {
						@Override
						public void onRemoveCompleted(String packageName,
								boolean succeeded) throws RemoteException {
							System.out.println(succeeded);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		Toast.makeText(getApplicationContext(), "Cache clear!", 0).show();
	}
}
