package com.jingtian.mobileguardian;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.jingtian.mobileguardian.utils.StreamTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

public class SplashActivity extends Activity {

	protected static final String TAG = "SplashActivity";
	protected static final int ENTER_HOME = 0;
	protected static final int SHOW_UPDATE_DIALOG = 1;
	protected static final int URL_ERROR = 2;
	protected static final int NETWORK_ERROR = 3;
	protected static final int JSON_ERROR = 4;
	private TextView tv_splash_version;
	private TextView tv_update_info;
	private String newVersion;
	private String description;
	private String apkurl;
	private SharedPreferences sp;

	private Handler handler = new Handler() {

		/**
		 * this method handle the corresponding solution for each case
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case SHOW_UPDATE_DIALOG:
				Log.i(TAG, "app updates");
				showUpdateDialog();
				break;

			case ENTER_HOME:
				enterHome();
				break;

			case URL_ERROR:
				enterHome();
				Toast.makeText(getApplicationContext(), "URL Parse Error", 0).show();
				break;

			case NETWORK_ERROR:
				enterHome();
				Toast.makeText(getApplicationContext(), "Network Connection Error", 0).show();
				break;

			case JSON_ERROR:
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON Parse Error", 0).show();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("version " + getVersionName());

		tv_update_info = (TextView) findViewById(R.id.tv_update_info);
		
		//if data/data/shared_pref has the file named "config", then just open
		//if no such file, create the xml file named "config"
		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean update = sp.getBoolean("update", false);
		
		//create a shortcut at the desktop
		installShortCut();
		
		
		//copy databases(files) from assets folder -> data/data
		copyDB("address.db");
		copyDB("antivirus.db");
		
		
		if (update) {
			// check latest version
			updateVersion();
		} else {
			//wait for 2 secs and then enter to home page
			handler.postDelayed(new Runnable() { //this handler is system's handler
				public void run() {
					enterHome();
				}
			}, 2000);
		}
		

		// AlphaAnimation alphaAnimation = new AlphaAnimation(0.2f, 1.0f);
		// alphaAnimation.setDuration(500);
		// findViewById(R.id.rl_root_splash).startAnimation(alphaAnimation);
	}

	/**
	 * install shortcut at the first desktop
	 */
	private void installShortCut() {
		//only create a shortcut the first time
		boolean shortcut = sp.getBoolean("shortcut", false);
		if(shortcut)
			return;
		Editor editor = sp.edit();
		//intent: give desktop app a broadcast, tell them that we need to create a shortcut
		Intent intent = new Intent();
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		//shortcut includes 3: 1,name  2,icon 3,to do what when click
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Mobile Guardian");
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		
		//shortcutIntent: to do what(launch app) when click
		Intent shortcutIntent = new Intent();
		shortcutIntent.setAction("android.intent.action.MAIN");
		shortcutIntent.addCategory("android.intent.category.LAUNCHER");
		shortcutIntent.setClassName(getPackageName(), "com.jingtian.mobileguardian.SplashActivity");
//		shortcutIntent.setAction("com.itheima.xxxx"); //when app is deleted, shortcut remains
//		shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		sendBroadcast(intent);
		editor.putBoolean("shortcut", true);
		editor.commit();
//		Toast.makeText(getApplicationContext(), "Shortcut Created", 0).show();
	}

	/**
	 * copy the database to data/data/<package>/files/address.db
	 */
	private void copyDB(String filename) {
		try {
			File file = new File(getFilesDir(),filename);
			if (file.exists() && file.length() > 0) {
				//file exists, no need to copy
				Log.i(TAG, "file exists, no need to copy");
			}else {
				InputStream is = getAssets().open(filename);
				
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int length = 0;
				while ((length = is.read(buffer)) != -1) {
					fos.write(buffer,0,length);
				}
				is.close();
				fos.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * enter into the home page
	 */
	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);

		// finish the current page
		finish();
	}

	/**
	 * show a dialog to let clients decide if they want to upgrade
	 */
	protected void showUpdateDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("Upgrade Notification");
		builder.setMessage(description);
		
		//builder.setCancelable(false); //force to upgrade
		
		builder.setOnCancelListener(new OnCancelListener() { // when click somewhere else or back button
			
			@Override
			public void onCancel(DialogInterface dialog) {
				enterHome();
				dialog.dismiss(); //dismiss the current dialog
			}
		});
		

		builder.setPositiveButton("Upgrade Now", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// download APK and install
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// +
																								// getVersionName()
					// sdcard exists, using afinal.lib to download
					FinalHttp finalHttp = new FinalHttp();
					finalHttp.download(apkurl,
							Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileGuardian"+ newVersion + ".apk",
							new AjaxCallBack<File>() {

						@Override
						public void onFailure(Throwable t, int errorNo, String strMsg) {
							t.printStackTrace();
							Toast.makeText(getApplicationContext(), "Fail to download", 0).show();
							enterHome();
							super.onFailure(t, errorNo, strMsg);
						}

						@Override
						public void onLoading(long count, long current) {
							super.onLoading(count, current);
							tv_update_info.setVisibility(View.VISIBLE);

							int progress = (int) (current * 100 / count);
							tv_update_info.setText("Downloading Progress: " + progress + "%");
						}

						@Override
						public void onSuccess(File t) {
							super.onSuccess(t);

							installAPK(t);
						}

						/**
						 * to install app (replace the old version)
						 * 
						 * @param t
						 */
						private void installAPK(File t) {

							Intent intent = new Intent();
							intent.setAction("android.intent.action.VIEW");
							intent.addCategory("android.intent.category.DEFAULT");
							intent.setDataAndType(Uri.fromFile(t), "application/vnd.android.package-archive");

							startActivity(intent);
						}
					});

				} else {
					Toast.makeText(getApplicationContext(), "No sdcard, please try again after installing the card", 0)
							.show();
					return;
				}
			}

		});

		builder.setNegativeButton("Upgrade Later", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// cancel the dialog and enter into the main page
				dialog.dismiss();
				enterHome();
			}
		});

		builder.show(); // don't forget to show
	}

	/**
	 * get version name from the manifest file
	 * 
	 * @return
	 */
	private String getVersionName() {
		PackageManager pm = getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(getPackageName(), 0); // get
																		// manifest
																		// file
			return pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "1.0"; // default value
		}
	}

	/**
	 * check the version and download the latest version Permision needed:
	 * android.permission.INTERNET
	 */
	private void updateVersion() {
		// Internet functions should run in a separated thread.
		new Thread() {

			public void run() {

				Message msg = Message.obtain();

				// recording a 2s time to show log
				long startTime = System.currentTimeMillis();

				try {
					// url: http://104.194.97.103:8080/updateinfo.html
					URL url = new URL(getString(R.string.serverurl));
					// connect nets
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(4000); // 4s
					if (conn.getResponseCode() == 200) {
						InputStream is = conn.getInputStream();
						// convert stream to string
						String result = StreamTools.readFromStream(is);
						// send a log of a corresponding class
						Log.i(TAG, "successfully connect the internet" + result);
						// analyze the result
						JSONObject jsonObject = new JSONObject(result);

						newVersion = (String) jsonObject.get("version");
						description = (String) jsonObject.get("description");
						apkurl = (String) jsonObject.get("apkurl");

						// verify if there is a new version
						if (getVersionName().equals(newVersion)) {
							// current version is the latest, no need to update
							msg.what = ENTER_HOME;
						} else {
							// there is a new version, update needed
							// when updating UI in the branch thread, a handler
							// is needed.
							msg.what = SHOW_UPDATE_DIALOG;
						}
					}

				} catch (MalformedURLException e) {
					// wrong spell the url
					msg.what = URL_ERROR;
					e.printStackTrace();
				} catch (IOException e) {
					// unable to connect the server
					msg.what = NETWORK_ERROR;
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					msg.what = JSON_ERROR;
					e.printStackTrace();
				} finally {

					long endTime = System.currentTimeMillis();
					// time used to verify version and connect the Internet
					long timeSpend = endTime - startTime;

					if (timeSpend < 2000) // fix the loading time to at least 2s
					{
						try {
							Thread.sleep(2000 - timeSpend);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handler.sendMessage(msg);
				}
			};
		}.start();
	}

}
