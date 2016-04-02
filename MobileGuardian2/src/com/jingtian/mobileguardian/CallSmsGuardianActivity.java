package com.jingtian.mobileguardian;

import java.util.List;

import com.jingtian.mobileguardian.db.dao.BlackNumberDAO;
import com.jingtian.mobileguardian.domain.BlackNumberInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CallSmsGuardianActivity extends Activity {

	private ListView lv_callsms_guardian;
	private List<BlackNumberInfo> list;
	private List<BlackNumberInfo> segmentlist;
	private BlackNumberDAO dao;
	private CallSmsGuardianAdapter adapter;
	private LinearLayout ll_loading;
	private int offset = 0;
	private int limit = 20;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_sms_guardian);
		lv_callsms_guardian = (ListView) findViewById(R.id.lv_callsms_guardian);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		dao = new BlackNumberDAO(this); //open | create database
		
		searchAndShowData();
		
		//listenning the listview
		lv_callsms_guardian.setOnScrollListener(new OnScrollListener() {
			
			//when state changes
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					//obatin the last position in the view
					int lastPosition = lv_callsms_guardian.getLastVisiblePosition();
					//if lastPosition == lastLoadedPosition
					if (lastPosition == (list.size()-1)) {
						offset += limit;
						searchAndShowData();
					}
					
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					
					break;
				case OnScrollListener.SCROLL_STATE_FLING: //scroll move due to fling
	
					break;
				default:
					break;
				}
			}
			
			//activated while scrolling
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				
			}
		});
		
		Toast.makeText(getApplicationContext(), "Block contacts' calls, SMS or both", 1).show();
	}

	private void searchAndShowData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				//search all black number may take some time -> put into a new thread
//				list = dao.searchAllBlackNumber(); //users may wait too long
				
				if (list == null) {
					//only search part of the records to reduce users' waiting time
					list = dao.searchPartBlackNumber(offset, limit);
				}else {
					//add the new set to the end
					segmentlist = dao.searchPartBlackNumber(offset, limit);
					list.addAll(segmentlist); 
					
				}
				
				runOnUiThread(new Runnable() {
					public void run() {
						ll_loading.setVisibility(View.INVISIBLE);
						
						if (adapter == null) {
							// a new adapter is a NEW page(can be used to separate page)
							adapter = new CallSmsGuardianAdapter(); //adapter is used to show data to the interface
							lv_callsms_guardian.setAdapter(adapter);
						}else if(adapter != null && segmentlist.size() == limit){
							//can be used to separate set
							adapter.notifyDataSetChanged(); //refresh (still at the current position)
						} else if(adapter != null && segmentlist.size() != limit){
							Toast.makeText(CallSmsGuardianActivity.this, "No more data", 0).show();
						} 
						
					}
				});
			};
		}.start();
	}
	
	private class CallSmsGuardianAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return list.size();
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			
			//1.using convertView to reduce the quantity of creating views
			if (convertView != null) {
				view = convertView; //convertView the unseen(used) objects
				
				//use the result
				holder = (ViewHolder) view.getTag(); //5%
			}
			else {
				view = View.inflate(getApplicationContext(), R.layout.list_item_callsms, null); 
				//2.recording the results to get faster 
				holder = new ViewHolder();
				holder.tv_black_number = (TextView) view.findViewById(R.id.tv_black_number);
				holder.tv_black_mode = (TextView) view.findViewById(R.id.tv_black_mode);
				holder.iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
				holder.rl_blockade_number = (RelativeLayout) view.findViewById(R.id.rl_blockade_number);
				//store the result
				view.setTag(holder);
			}
			
			//set text for each item
			holder.tv_black_number.setText(list.get(position).getNumber());
			String mode = list.get(position).getMode();
			if ("1".equals(mode)) {
				holder.tv_black_mode.setText("Call Blockade");
			} else if ("2".equals(mode)) {
				holder.tv_black_mode.setText("SMS Blockade");
			} else { //"3".equals(mode)
				holder.tv_black_mode.setText("Call & SMS Blockade");
			}
			
			//delete icon click
			holder.iv_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					AlertDialog.Builder builder = new Builder(CallSmsGuardianActivity.this);
					builder.setTitle("Warning");
					builder.setMessage("Confirm to delete the blockade number?");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
													//note duplicate name for packages
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//delete from the database
							dao.deleteBlackNumber(list.get(position).getNumber());
							//update contents in the activity(same operation as done to database)
							list.remove(position);
							//update adapter
							adapter.notifyDataSetChanged();
							
						}
					});
					
					builder.setNegativeButton("Cancel", null); //cancel and return
					builder.show();
				}
			});
			
			//update blockade number
			holder.rl_blockade_number.setOnClickListener(new OnClickListener() {
				EditText et_delete_blacknumber;
				CheckBox cb_phone_delete;
				CheckBox cb_sms_delete;
				Button bt_ok_delete;
				Button bt_cancel_delete;
			
				@Override
				public void onClick(View v) {
					
					AlertDialog.Builder builder = new Builder(CallSmsGuardianActivity.this);
					final AlertDialog dialog = builder.create();
					View contentView = View.inflate(CallSmsGuardianActivity.this, R.layout.dialog_update_blacknumber, null);
					
					et_delete_blacknumber = (EditText) contentView.findViewById(R.id.et_delete_blacknumber);
					cb_phone_delete = (CheckBox) contentView.findViewById(R.id.cb_phone);
					cb_sms_delete = (CheckBox) contentView.findViewById(R.id.cb_sms);
					bt_ok_delete = (Button) contentView.findViewById(R.id.bt_ok);
					bt_cancel_delete = (Button) contentView.findViewById(R.id.bt_cancel);
					
					et_delete_blacknumber.setText(list.get(position).getNumber());
					String mode = list.get(position).getMode();
					if ("3".equals(mode)) {
						cb_phone_delete.setChecked(true);
						cb_sms_delete.setChecked(true);
					}
					else if ("2".equals(mode)) {
						cb_sms_delete.setChecked(true);
					}
					else if ("1".equals(mode)) {
						cb_phone_delete.setChecked(true);
					}
					
					dialog.setView(contentView,0,0,0,0); //0,0,0,0 no frame
					dialog.show();
					
					bt_cancel_delete.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					
					bt_ok_delete.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							String newText;
							newText = et_delete_blacknumber.getText().toString().trim();
							
							String mode;
							if (cb_phone_delete.isChecked() && cb_sms_delete.isChecked()) {
								//blockade both
								mode = "3";
							}
							else if (cb_phone_delete.isChecked()) {
								//blockade phone call
								mode = "1";
							}
							else if (cb_sms_delete.isChecked()) {
								//blockade sms
								mode = "2";
							}else {
								//neither selected
								Toast.makeText(CallSmsGuardianActivity.this, "Select blockade mode", 0).show();
								return;
							}
							//add record to the database
							dao.updateBlackNumberMode(newText, mode);
							//update contents in the listview set
							BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
							blackNumberInfo.setNumber(newText);
							blackNumberInfo.setMode(mode);
							list.remove(position);
							list.add(position, blackNumberInfo); //add the new record to the front
							adapter.notifyDataSetChanged();
							
							dialog.dismiss();
							
						}
					});
					
				}
			});
			
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
	 * container of View
	 * recording the result
	 */
	static class ViewHolder{
		TextView tv_black_number;
		TextView tv_black_mode;
		ImageView iv_delete;
		RelativeLayout rl_blockade_number;
	}
	
	private EditText et_blacknumber;
	private CheckBox cb_phone;
	private CheckBox cb_sms;
	private Button bt_ok;
	private Button bt_cancel;
	
	public void addBlackNumber(View view){
		AlertDialog.Builder builder = new Builder(this);
		final AlertDialog dialog = builder.create();
		View contentView = View.inflate(this, R.layout.dialog_add_blacknumber, null);
		
		et_blacknumber = (EditText) contentView.findViewById(R.id.et_blacknumber);
		cb_phone = (CheckBox) contentView.findViewById(R.id.cb_phone);
		cb_sms = (CheckBox) contentView.findViewById(R.id.cb_sms);
		bt_ok = (Button) contentView.findViewById(R.id.bt_ok);
		bt_cancel = (Button) contentView.findViewById(R.id.bt_cancel);
		
		dialog.setView(contentView,0,0,0,0); //0,0,0,0 no frame
		dialog.show();
		
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String blackNumber = et_blacknumber.getText().toString().trim();
				if (TextUtils.isEmpty(blackNumber)) {
					Toast.makeText(CallSmsGuardianActivity.this, "Number cannot be null", 0).show();
					return;
				}
				
				String mode;
				if (cb_phone.isChecked() && cb_sms.isChecked()) {
					//blockade both
					mode = "3";
				}
				else if (cb_phone.isChecked()) {
					//blockade phone call
					mode = "1";
				}
				else if (cb_sms.isChecked()) {
					//blockade sms
					mode = "2";
				}else {
					//neither selected
					Toast.makeText(CallSmsGuardianActivity.this, "Select blockade mode", 0).show();
					return;
				}
				//add record to the database
				dao.addBlackNumber(blackNumber, mode);
				//update contents in the listview set
				BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
				blackNumberInfo.setNumber(blackNumber);
				blackNumberInfo.setMode(mode);
				list.add(0, blackNumberInfo); //add the new record to the front
				adapter.notifyDataSetChanged();
				
				dialog.dismiss();
			}
		});
	}

}
