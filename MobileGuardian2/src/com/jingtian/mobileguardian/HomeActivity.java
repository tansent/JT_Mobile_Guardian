package com.jingtian.mobileguardian;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jingtian.mobileguardian.utils.MD5Utils;
import com.lidroid.xutils.BitmapUtils;
import com.viewpagerindicator.CirclePageIndicator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;


public class HomeActivity extends Activity {
	
	protected static final String TAG = "HomeActivity";
	private TextView tv_banner;
	private ViewPager viewPager;
	private CirclePageIndicator indicator;
	private GridView list_home;
	private MyAdapter adapter;
	private MyPagerAdapter pagerAdapter;
	private int[] imageStr = {R.drawable.image_banner1,R.drawable.image_banner2,R.drawable.image_banner3,R.drawable.image_banner4};
	private String[] bannerTitles = {"Anti theft","App Manager","Process Monitor","Virus Killer"};
	private Handler mHandler; //use recursive on tap window to auto-move
	
	private static String[] names = {
			"Anti theft " , "Call Guardian" , "App Manager",
			"Process Monitor" , "Data Control" , "Virus Killer",
			"Cache Cleaner" , "Advance Tools" , "Setting Center"
	}; 
	
	private static int[] imageIDs = {
			R.drawable.safe,R.drawable.callmsgsafe,R.drawable.app,
			R.drawable.taskmanager,R.drawable.netmanager,R.drawable.trojan,
			R.drawable.sysoptimize,R.drawable.atools,R.drawable.settings
	};
	
	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		list_home = (GridView) findViewById(R.id.list_home);
		indicator = (CirclePageIndicator) findViewById(R.id.indicator);
		viewPager = (ViewPager) findViewById(R.id.vp_main);
//		viewPager.setOnPageChangeListener(new BannerPageChange()); //should be indicator
		
		pagerAdapter = new MyPagerAdapter();
		viewPager.setAdapter(pagerAdapter);
		indicator.setViewPager(viewPager); //must after viewPager.setAdapter
		indicator.setSnap(true);
		indicator.setOnPageChangeListener(new BannerPageChange());//when indicator changes, title should change
		
		
		tv_banner = (TextView) findViewById(R.id.tv_banner);
		tv_banner.setText(bannerTitles[0]);
		
		adapter = new MyAdapter();
		list_home.setAdapter(adapter);
		
		// auto-play tab window
		if (mHandler == null) { //every time scroll to this page, but we only create once
			mHandler = new Handler() {
				public void handleMessage(android.os.Message msg) {
					int currentItem = viewPager.getCurrentItem();

					if (currentItem < bannerTitles.length - 1) { //boundary
						currentItem++;
					} else {
						currentItem = 0;
					}

					viewPager.setCurrentItem(currentItem);// next page
					mHandler.sendEmptyMessageDelayed(0, 5000);//continue sending
				};
			};

			mHandler.sendEmptyMessageDelayed(0, 5000);// send delay message to handler in 3s
		}
		
		list_home.setOnItemClickListener(new OnItemClickListener() {

			Intent intent;
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) { 
				
				case 0:	//click "Anti Theft"
					showAntiTheftDialog();
					break;
					
				case 1:	//click "Call Guardian"
					intent = new Intent(HomeActivity.this, CallSmsGuardianActivity.class);
					startActivity(intent);
					break;
					
				case 2:	//click "App Manager"
					intent = new Intent(HomeActivity.this, AppManagerActivity.class);
					startActivity(intent);
					break;
					
				case 3:	//click "Process Monitor"
					intent = new Intent(HomeActivity.this, ProcessMonitor.class);
					startActivity(intent);
					break;
					
				case 4:	//click "Data Control"
					intent = new Intent(HomeActivity.this, ActivityDataControl.class);
					startActivity(intent);
					break;
					
				case 5:	//click "Virus Killer"
					intent = new Intent(HomeActivity.this, VirusKillerActivity.class);
					startActivity(intent);
					break;
					
				case 6:	//click "Cache Cleaner"
					intent = new Intent(HomeActivity.this, ActivityCacheCleaner.class);
					startActivity(intent);
					break;
				
				case 7:	//click "Advanced Tools"
					intent = new Intent(HomeActivity.this, AdvancedToolActivity.class);
					startActivity(intent);
					break;
				
				case 8:	//click "Setting Center"
					intent = new Intent(HomeActivity.this, SettingCenterActivity.class);
					startActivity(intent);
					break;

				default:
					break;
				}
				
			}
			
		});
		
//		new Thread(){
//			public void run() {
				AdView mAdView = (AdView) findViewById(R.id.adView);
		        AdRequest adRequest = new AdRequest.Builder().build();
		        mAdView.loadAd(adRequest);
//			};
//		}.start();
		
        
		indicator.onPageSelected(0); //must wait until all get loaded
	}
	
	/**
	 * pop up anti-theft function
	 */
	protected void showAntiTheftDialog(){
		if (isPswSet()) {
			//the password is set
			showEnterDialog();
		} else {
			//the password is not set (first time to come)
			showSetPswDialog();
		}
	}
	
	private EditText et_set_psw;
	private EditText et_confirm_psw;
	private Button bt_ok;
	private Button bt_cancel;
	private AlertDialog dialog;
	/**
	 * password dialog
	 */
	private void showSetPswDialog() {
		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		View view = View.inflate(HomeActivity.this, R.layout.dialog_set_psw, null);
		
		et_set_psw = (EditText) view.findViewById(R.id.et_set_psw);
		et_confirm_psw = (EditText) view.findViewById(R.id.et_confirm_psw);
		bt_ok = (Button) view.findViewById(R.id.bt_ok);
		bt_cancel = (Button) view.findViewById(R.id.bt_cancel);
		
		
		//listener does not have sequence to invoke, it will last forever
		bt_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// cancel the dialog
				dialog.dismiss();
			}
		});
		
		bt_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String password = et_set_psw.getText().toString().trim();
				String password_confirm = et_confirm_psw.getText().toString().trim();
				if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm)) {
					Toast.makeText(HomeActivity.this, "password cannot be null", 0).show();
					return;
				}
				if (!password.equals(password_confirm)) {
					Toast.makeText(HomeActivity.this, "passwords must be identical", 0).show();
					return;
				}
				Editor editor = sp.edit();
				editor.putString("password", MD5Utils.encrypt(password));
				editor.commit();
				Toast.makeText(HomeActivity.this, "passwords saved successfully", 0).show();
				Log.i(TAG, "saved in shared preference and then return to the home page");
				dialog.dismiss();
				Intent intent = new Intent(HomeActivity.this, AntiTheftActivity.class);
				startActivity(intent);
			}
		});
		
		dialog = builder.create();
		dialog.setView(view,0,0,0,0);
		dialog.show();
	}

	/**
	 * enter dialog
	 */
	private void showEnterDialog() {
		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		
		
		View view = View.inflate(HomeActivity.this, R.layout.dialog_enter_psw, null);
		
		et_set_psw = (EditText) view.findViewById(R.id.et_set_psw);
		bt_ok = (Button) view.findViewById(R.id.bt_ok);
		bt_cancel = (Button) view.findViewById(R.id.bt_cancel);
		
		
		//listener does not have sequence to invoke, it will last forever
		bt_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// cancel the dialog
				dialog.dismiss();
			}
		});
		
		bt_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String password = et_set_psw.getText().toString().trim();
				String password_stored = sp.getString("password", "");
				if (TextUtils.isEmpty(password)) {
					Toast.makeText(HomeActivity.this, "password cannot be null", 0).show();
					return;
				}
				if (!MD5Utils.encrypt(password).equals(password_stored)) {
					Toast.makeText(HomeActivity.this, "passwords entered incorrect", 0).show();
					et_set_psw.setText("");
					return;
				}
				
				
				Log.i(TAG, "passwords entered correct");
				dialog.dismiss();
				Intent intent = new Intent(HomeActivity.this, AntiTheftActivity.class);
				startActivity(intent);
			}
		});
		
		dialog = builder.create();
		dialog.setView(view,0,0,0,0);
		dialog.show();
		
	}

	/**
	 * judge if the password is set
	 * @return
	 */
	private boolean isPswSet(){
		String password = sp.getString("password", null);
//		if (TextUtils.isEmpty(password)) {
//			return false;
//		} else {
//			return true;
//		}
		return !TextUtils.isEmpty(password);
	}
	
	//Adapter is used to fill Grids
	private class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return names.length;
		}
		
		//For each grid:
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(HomeActivity.this, R.layout.list_item_home, null);
			ImageView imageView = (ImageView) view.findViewById(R.id.iv_item);
			TextView textView = (TextView) view.findViewById(R.id.tv_item);
			imageView.setImageResource(imageIDs[position]);
			textView.setText(names[position]);
			return view;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}
	
	
	/**
	 * top news (headlines)adapter (viewPager)
	 * 
	 */
	class MyPagerAdapter extends PagerAdapter {
  
		//From XUtils. for loading images
		private BitmapUtils utils;
		public MyPagerAdapter() {
			//define BitmapUtils
			utils = new BitmapUtils(getApplicationContext());
			
			// set default image background
			utils.configDefaultLoadingImage(R.drawable.topnews_item_default);
		}

		@Override
		public int getCount() {
			return imageStr.length; 
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView image = new ImageView(getApplicationContext());
			image.setImageResource(imageStr[position]);
			// fill the component to the whole screen
			image.setScaleType(ScaleType.FIT_XY);
//			TopNewsData topNewsData = mTopNewsList.get(position);
//			utils.display(image, topNewsData.topimage);// using bitmap utils to load url image
			
			container.addView(image);
			
////			image.setOnClickListener(new ...); //only setOnClickListener or setOnTouchListener
//			image.setOnTouchListener(new TopNewsTouchListener());//set listener
			
			return image;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}
	
	class BannerPageChange implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}

		/**
		 * only invoke when user changes the pager (need to set manually for the first time)
		 * @param position
		 */
		@Override
		public void onPageSelected(int position) {
			tv_banner.setText(bannerTitles[position]);
		}
		
	}
}
