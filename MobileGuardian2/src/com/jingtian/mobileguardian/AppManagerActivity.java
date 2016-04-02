package com.jingtian.mobileguardian;

import java.util.ArrayList;
import java.util.List;

import com.jingtian.mobileguardian.db.dao.AppLockDAO;
import com.jingtian.mobileguardian.domain.AppInfo;
import com.jingtian.mobileguardian.engine.AppInfoProvider;
import com.jingtian.mobileguardian.utils.DensityUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AppManagerActivity extends Activity implements OnClickListener {

	private static final String TAG = "AppManagerActivity";
	private TextView tv_avail_rom;
	private TextView tv_avail_sd;
	
	private ListView lv_app_manager;
	private LinearLayout ll_loading;
	
	private TextView tv_status;
	
	private LinearLayout ll_uninstall;
	private LinearLayout ll_share;
	private LinearLayout ll_boot;
	
	/**
	 * all apps' info
	 */
	private List<AppInfo> appInfoList;
	
	/**
	 * user apps
	 */
	private List<AppInfo> userInfoList;
	
	/**
	 * system apps
	 */
	private List<AppInfo> systemInfoList;
	
	/**
	 * pop up a floating window
	 */
	private PopupWindow popupWindow;
	
	/**
	 * store data of every item
	 */
	private AppInfo appInfo;
	
	private AppManagerAdapter adapter;
	
	private AppLockDAO applockDAO;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		applockDAO = new AppLockDAO(this);
		
		tv_avail_rom = (TextView) findViewById(R.id.tv_avail_rom);
		tv_avail_sd = (TextView) findViewById(R.id.tv_avail_sd);
		tv_status = (TextView) findViewById(R.id.tv_status);
		
		//get available sdcard size
		long sdcardSize = getAvailSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
		//get available rom size(app size)
		long romSize = getAvailSpace(Environment.getDataDirectory().getAbsolutePath());
		
		//Formatter can transfer a long string to MB format
		tv_avail_sd.setText("SDcard Avail: "+Formatter.formatFileSize(this, sdcardSize));
		tv_avail_rom.setText("ROM Avail: "+Formatter.formatFileSize(this, romSize));
		
		lv_app_manager = (ListView) findViewById(R.id.lv_app_manager);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		
		//obtain all apps(user + system), show them to the screen
		obtainAppsAndShow(); //also can be used to refresh
		
		//to provide better user experience: set a constant textview to show number of apps
		//setOnScrollListener will work based on the restriction at the first place
		lv_app_manager.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			//invoke while scrolling
			//firstVisibleItem: first view's position on the screen
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				dismissPopupWindow();
				if (userInfoList!=null && systemInfoList!=null) {
					if (firstVisibleItem > userInfoList.size()) {
						tv_status.setText("System Apps: "+ systemInfoList.size());
					}else {
						tv_status.setText("User Apps: "+ userInfoList.size());
					}
				}
				
			}

		});
		
		/**
		 * set listview onItemClickListener (2 places to set: 1, in onItemClicklistener   2, in adapter-onClickListener)
		 */
		lv_app_manager.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if (position == 0) {
					return;
				}else if (position == (userInfoList.size() + 1)) {
					return;
				}else if (position < (userInfoList.size() + 1)) {
					int newPosition = position - 1;
					appInfo = userInfoList.get(newPosition);
				}else {
					int newPosition = position - 1 - userInfoList.size() - 1;
					appInfo = systemInfoList.get(newPosition);
				}
				
				//every time click, dismiss the previous popupwindow
				//make there is only one window here
				dismissPopupWindow();
				
				View contentView = View.inflate(getApplicationContext(), R.layout.popup_app_item, null);
				//only after inflate the view can we initialize it
				ll_uninstall = (LinearLayout) contentView.findViewById(R.id.ll_uninstall);
				ll_share = (LinearLayout) contentView.findViewById(R.id.ll_share);
				ll_boot = (LinearLayout) contentView.findViewById(R.id.ll_boot);
				ll_uninstall.setOnClickListener(AppManagerActivity.this); //internal listener (onClick)
				ll_share.setOnClickListener(AppManagerActivity.this);
				ll_boot.setOnClickListener(AppManagerActivity.this);
				
				popupWindow = new PopupWindow(contentView, -2, -2); //-2: wrap content , -1: match parent   ViewGroup.LayoutParams.WRAP_CONTENT

				//set bakground color to show animation
				popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				
				int[] location = new int[2];
				view.getLocationInWindow(location); // get the location of where being clicked
				
				//number used in code is pixel, which should be transfered before using
				int dip_offset = 50;
				int px_offset = DensityUtil.dip2px(getApplicationContext(), dip_offset);
				
				popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP , px_offset, location[1]);
				
				//set popup window animation (set background color first)
				//expand from one point
				ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, 
						Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.5f);
				sa.setDuration(200);
//				contentView.startAnimation(sa);
				AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
				aa.setDuration(200);
				AnimationSet animationSet = new AnimationSet(false); //false: animations are independent
				animationSet.addAnimation(sa);
				animationSet.addAnimation(aa);
				contentView.startAnimation(animationSet);
			}
		});
		
		
		/**
		 * long click listener
		 * return boolean: false: other listener will continue to be invoked; true: end here 
		 */
		lv_app_manager.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0) {
					return true;
				}else if (position == (userInfoList.size() + 1)) {
					return true;
				}else if (position < (userInfoList.size() + 1)) {
					int newPosition = position - 1;
					appInfo = userInfoList.get(newPosition);
				}else {
					int newPosition = position - 1 - userInfoList.size() - 1;
					appInfo = systemInfoList.get(newPosition);
				}
				
				//judge if the lock package is stored in databse
				ViewHolder holder = (ViewHolder) view.getTag();
				if (applockDAO.search(appInfo.getPackageName())) {
					applockDAO.delete(appInfo.getPackageName());
					holder.iv_lock_state.setImageResource(R.drawable.unlock);
				}else {
					applockDAO.add(appInfo.getPackageName());
					holder.iv_lock_state.setImageResource(R.drawable.lock);
				}
				return true;
			}
			
		});
		
	}

	private void obtainAppsAndShow() {
		ll_loading.setVisibility(View.VISIBLE);
		
		new Thread(){
			public void run() {
				//obtaining a list of system app could be time consuming
				appInfoList = AppInfoProvider.getAppInfos(AppManagerActivity.this);
				
				userInfoList = new ArrayList<AppInfo>();
				systemInfoList = new ArrayList<AppInfo>();
				//separate apps to user category and system category
				for (AppInfo app : appInfoList) {
					if (app.isUserApp()) {
						userInfoList.add(app);
					}else {
						systemInfoList.add(app);
					}
				}
				
				runOnUiThread(new Runnable() {
					public void run() {
						if (adapter == null) { //first time show to the screen
							//screen will show at the top
							adapter = new AppManagerAdapter();
							lv_app_manager.setAdapter(adapter);
						}
						else {
							//screen will move to the position last time the screen has shown
							adapter.notifyDataSetChanged();
						}
						ll_loading.setVisibility(View.INVISIBLE);
						
					}
				});
				
			};
		}.start();
	}
	
	private void dismissPopupWindow() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}
	
	/**
	 * Adapter: to fill data into the list and show the list
	 * 
	 * Set list every time when enter into the activity(first time, back and return, refresh)
	 *
	 */
	private class AppManagerAdapter extends BaseAdapter{

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
//			For test only:
//			TextView tv = new TextView(AppManagerActivity.this);
//			tv.setText(appInfoList.get(position).toString());
			
			//set screen view
			View view;
			ViewHolder holder;
			//when reusing convertView, judge if it is null as well as if it is the type we want
			if (convertView != null && convertView instanceof RelativeLayout) { //only customized RelativeLayout should be reused 
				view = convertView; //convertView the unseen(used) objects
				
				//use the result
				holder = (ViewHolder) view.getTag(); //5
			}
			else {
				view = View.inflate(getApplicationContext(), R.layout.list_item_app_info, null); 
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
				holder.tv_location = (TextView) view.findViewById(R.id.tv_app_location);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
				holder.iv_lock_state = (ImageView) view.findViewById(R.id.iv_lock_status);
				//store the result
				view.setTag(holder);
			}
			
			// transfer data into the view
			AppInfo app;
			if (position == 0) { //user_tag
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(Color.argb(80, 133, 227, 150));
				tv.setText("User Apps: "+ userInfoList.size());
				return tv;
			}else if (position == (1 + userInfoList.size())) {
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(Color.argb(80, 133, 227, 150));
				tv.setText("System Apps: "+ systemInfoList.size());
				return tv;
			}else if (position <= userInfoList.size() ) { //user app  (1 + userInfoList.size())
				int newPosition = position - 1;
				app = userInfoList.get(newPosition);
			}else { //system app
				int newPosition = position - 1 - userInfoList.size() - 1;
				app = systemInfoList.get(newPosition);
			}
//			if (position < userInfoList.size()) {
//				app = userInfoList.get(position);
//			}else {
//				int newPosition = position - userInfoList.size();
//				app = systemInfoList.get(newPosition);
//			}
//			AppInfo app = appInfoList.get(position);
			holder.iv_icon.setImageDrawable(app.getIcon());
			holder.tv_name.setText(app.getAppName());
			if (app.isAppInRom()) {
				holder.tv_location.setText("App in ROM" + " (" +app.getUid() +")");
			}else {
				holder.tv_location.setText("App in external storage" + " (" +app.getUid() +")");
			}
			
			//judge if the lock package is stored in databse
			if (applockDAO.search(app.getPackageName())) {
				holder.iv_lock_state.setImageResource(R.drawable.lock);
			}else {
				holder.iv_lock_state.setImageResource(R.drawable.unlock);
			}
			
			return view;
		}
		
		@Override
		public int getCount() {
			//user_tag user app: 0 1 2 3  system_tag   system app: 0 1 2 ...
			return 1 + userInfoList.size() + 1 + systemInfoList.size(); //appInfoList.size()
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
	 * container of View (ViewHolder does not have to link to a bean's data)
	 * recording the result
	 */
	static class ViewHolder{
		TextView tv_name;
		TextView tv_location;
		ImageView iv_icon;
		ImageView iv_lock_state;
	}
	
	/**
	 * get disk(SDcard) size
	 * @param path: the root file where to get space
	 */
	public long getAvailSpace(String path){
		StatFs statf = new StatFs(path);
		long totalCount = statf.getBlockCount(); //get total block count
		long availCount = statf.getAvailableBlocks(); //get available block count
		long unitSize = statf.getBlockSize(); //get unit size for each block
		return availCount * unitSize;
	}
	
	@Override
	protected void onDestroy() {
		dismissPopupWindow();
		super.onDestroy();
	}

	/*
	 * equivalent to internal class
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		dismissPopupWindow();
		switch (v.getId()) {
		case R.id.ll_boot:
			Log.i(TAG, "boot" + appInfo.getAppName());
			bootApplication();
			break;
		case R.id.ll_share:
			Log.i(TAG, "share" + appInfo.getAppName());
			shareApplication();
			break;
		case R.id.ll_uninstall:
			Log.i(TAG, "unistall" + appInfo.getAppName());
			if (appInfo.isUserApp()) {
				uninstallApplication();
			}else {
				Toast.makeText(getApplicationContext(), "You do not have ROOT permission to uninstall a system app", 1).show();
//				Runtime.getRuntime().exec(""); //only available with ROOT
			}
			
			break;
		}
		
	}

	/**
	 * boot up an application
	 */
	private void bootApplication() {
//		PackageManager: manage manifest files
		PackageManager manager = getPackageManager();
//		Intent intent = new Intent();
//		intent.setAction("android.intent.action.MAIN");
//		intent.addCategory("android.intent.category.LAUNCHER");
//		//get all bootable activities
//		List<ResolveInfo> list = manager.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
		Intent intent = manager.getLaunchIntentForPackage(appInfo.getPackageName());
		if (intent != null) {
			startActivity(intent);
		}else{
			Toast.makeText(getApplicationContext(), "Sorry, this app has no entrance", 0).show();
		}
	}
	
	/**
	 * unistall an app
	 */
	private void uninstallApplication() {
		// <action android:name="android.intent.action.VIEW" />
		// <action android:name="android.intent.action.DELETE" />
		// <category android:name="android.intent.category.DEFAULT" />
		// <data android:scheme="package" />
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:" + appInfo.getPackageName()));
//		startActivity(intent); //current activity will not be changed
		startActivityForResult(intent, 0); // -> onActivityResult() to refresh current activity
	}
	
	@Override  // <- startActivityForResult
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// refresh current activity
		obtainAppsAndShow(); 
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * share an application
	 */
	private void shareApplication() {
		// Intent { act=android.intent.action.SEND typ=text/plain flg=0x3000000 cmp=com.android.mms/.ui.ComposeMessageActivity (has extras) } from pid 256
		Intent intent = new Intent();
		intent.setAction("android.intent.action.SEND");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT,  //EXTRA_EMAIL - email, EXTRA_TEXT - text (default)
				"I recommend a fantastic app for you, "+ appInfo.getAppName() + "!");
		startActivity(intent);
	}
}
