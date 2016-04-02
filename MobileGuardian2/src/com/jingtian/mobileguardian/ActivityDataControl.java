package com.jingtian.mobileguardian;

import java.util.List;

import com.jingtian.mobileguardian.domain.DataInfo;
import com.jingtian.mobileguardian.engine.DataInfoProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityDataControl extends Activity {

	private TextView tv_data_download;
	private TextView tv_data_upload;
	private LinearLayout ll_loading;
	private ListView lv_data_control;
	private TextView tv_app_with_data;

	private List<DataInfo> appDataInfos;
	private DataCotrolAdapter adapter;

	private int appWithDataCount;
	private long dataDownload;
	private long dataUpload;

	// public boolean firstTimeBoot = true;
	public SharedPreferences sp;
	private CheckBox cb_note;
	private Button bt_ok;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_control);

		sp = getSharedPreferences("config", MODE_PRIVATE);
		boolean isFirstTimeBootDataControl = sp.getBoolean("not_first_time_boot_data_control", false);
		if (isFirstTimeBootDataControl == false) {

			AlertDialog.Builder builder = new Builder(this);
			final AlertDialog dialog = builder.create();
			View contentView = View.inflate(this, R.layout.dialog_first_enter_reminder, null);
			cb_note = (CheckBox) contentView.findViewById(R.id.cb_note);
			bt_ok = (Button) contentView.findViewById(R.id.bt_ok);

			dialog.setView(contentView, 0, 0, 0, 0); // 0,0,0,0 no frame
			dialog.show();

			bt_ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					if (cb_note.isChecked()) {
						Editor editor = sp.edit();
						editor.putBoolean("not_first_time_boot_data_control", true);
						editor.commit();
					}

					dialog.dismiss();
				}
			});

		}

		tv_data_download = (TextView) findViewById(R.id.tv_data_download);
		tv_data_upload = (TextView) findViewById(R.id.tv_data_upload);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		lv_data_control = (ListView) findViewById(R.id.lv_data_control);
		tv_app_with_data = (TextView) findViewById(R.id.tv_app_with_data);

		obtainAppsAndShow();

		/*
		 * //obtain a package manager PackageManager pm = getPackageManager();
		 * 
		 * List<ApplicationInfo> list = pm.getInstalledApplications(0); for
		 * (ApplicationInfo applicationInfo : list) { //proc/uid_stat/10086 int
		 * uid = applicationInfo.uid;
		 * 
		 * long tx = TrafficStats.getUidTxBytes(uid); //bytes upload long rx =
		 * TrafficStats.getUidRxBytes(uid); //bytes download //if return -1,
		 * means no data generated or OS does not support
		 * 
		 * Log.i("data controll", uid +
		 * Formatter.formatFileSize(getApplicationContext(), tx) + " / "+
		 * Formatter.formatFileSize(getApplicationContext(), rx)); }
		 * 
		 * TrafficStats.getMobileTxBytes(); //3g/2g mobile upload
		 * TrafficStats.getMobileRxBytes(); //3g/2g mobile download
		 * 
		 * TrafficStats.getTotalTxBytes(); //all data sent(wifi + 2g/3g)
		 * TrafficStats.getTotalRxBytes(); //all data received(wifi + 2g/3g)
		 * 
		 * Log.i("data controll",
		 * Formatter.formatFileSize(getApplicationContext(),
		 * TrafficStats.getTotalTxBytes()) + " / "+
		 * Formatter.formatFileSize(getApplicationContext(),
		 * TrafficStats.getTotalRxBytes()));
		 */
	}

	private void obtainAppsAndShow() {
		ll_loading.setVisibility(View.VISIBLE);

		// obtaining a list of processes takes time
		new Thread() {
			public void run() {
				appDataInfos = DataInfoProvider.getDataInfos(getApplicationContext());
				// for (DataInfo info : appDataInfos) {
				// if (info.getUploadData() == 0 && info.getDownloadData() == 0)
				// {
				// appDataInfos.remove(info);
				// }
				// }

				appWithDataCount = appDataInfos.size();

				runOnUiThread(new Runnable() {
					public void run() {
						if (adapter == null) {
							adapter = new DataCotrolAdapter();
							lv_data_control.setAdapter(adapter);
						} else {
							adapter.notifyDataSetChanged(); // will not create
															// new items
						}
						ll_loading.setVisibility(View.INVISIBLE);
						setTitle();
					}
				});

			};
		}.start();
	}

	private class DataCotrolAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return appDataInfos.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// find view
			View view;
			ViewHolder holder;
			
			// only customized RelativeLayout should be reused
			if (convertView != null && convertView instanceof RelativeLayout) { 
				
				view = convertView; // convertView the unseen(used) objects

				// use the result
				holder = (ViewHolder) view.getTag(); // 5
			} else {
				view = View.inflate(getApplicationContext(), R.layout.list_item_data_info, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_process_icon);
				holder.tv_upload = (TextView) view.findViewById(R.id.tv_data_upload);
				holder.tv_download = (TextView) view.findViewById(R.id.tv_data_download);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_process_name);
				// store the result
				view.setTag(holder);
			}

			// fill data
			DataInfo appData;
			if (position == 0) { // user_tag
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(Color.argb(80, 133, 227, 150));
				tv.setText("Apps downloaded data: " + appWithDataCount);
				return tv;
			} else {
				appData = appDataInfos.get(position - 1);
			}
			holder.iv_icon.setImageDrawable(appData.getIcon());
			holder.tv_name.setText(appData.getProcessName());
			holder.tv_download.setText(
					"download: " + Formatter.formatFileSize(getApplicationContext(), appData.getDownloadData()));
			holder.tv_upload
					.setText("upload: " + Formatter.formatFileSize(getApplicationContext(), appData.getUploadData()));
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
	 * container of View recording the result
	 */
	static class ViewHolder {
		TextView tv_name;
		TextView tv_upload;
		TextView tv_download;
		ImageView iv_icon;
	}

	private void setTitle() {
		// Formatter.formatFileSize(context, number) long -> String

		dataUpload = TrafficStats.getMobileTxBytes(); // 3g/2g mobile upload
		dataDownload = TrafficStats.getMobileRxBytes(); // 3g/2g mobile download

		tv_data_download.setText("Data Download: " + Formatter.formatFileSize(this, dataDownload));
		tv_data_upload.setText("Data Upload: " + Formatter.formatFileSize(this, dataUpload));
		tv_app_with_data.setText("Apps downloaded data: " + appWithDataCount);
	}
}
