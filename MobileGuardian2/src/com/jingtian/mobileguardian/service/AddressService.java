package com.jingtian.mobileguardian.service;

import com.jingtian.mobileguardian.R;
import com.jingtian.mobileguardian.db.dao.NumberAddressQueryUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class AddressService extends Service {

	protected static final String TAG = "AddressService";
	/**
	 * WindowManager - for customized toast
	 */
	private WindowManager wm;
	private View view;

	/**
	 * TelephonyManager
	 */

	private TelephonyManager tm;
	private MyListenerPhone listenerPhone;

	private OutCallReceiver receiver;
	
	/**
	 * params recorder
	 */
	private SharedPreferences sp;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() { // onResume (in server, onCreate and onResume are
								// same)
		// TODO Auto-generated method stub
		super.onCreate();
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		// monitor the incoming phone call
		listenerPhone = new MyListenerPhone();
		tm.listen(listenerPhone, PhoneStateListener.LISTEN_CALL_STATE);

		// register the receiver using code (No specific Receiver class needed,
		// or it will overwrite here)
		// this receiver is for outgoing call
		receiver = new OutCallReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		registerReceiver(receiver, filter);

		// initiate the window
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
	}

	// internal class of a service
	// this Receiver has the same life span as the Service
	class OutCallReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// the phone number
			String phone = getResultData();
			// query databse
			String address = NumberAddressQueryUtils.queryNumber(phone);

			// Toast.makeText(context, address, 1).show();
			myToast(address);
		}

	}

	private class MyListenerPhone extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// state incomingNumber
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:// ringing
				// query database
				String address = NumberAddressQueryUtils.queryNumber(incomingNumber);

				// Toast.makeText(getApplicationContext(), address, 1).show();
				myToast(address);

				break;

			case TelephonyManager.CALL_STATE_IDLE:// telephone state idle
				// delete the view
				if (view != null) {
					wm.removeView(view);
				}

				break;

			default:
				break;
			}
		}

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// cancel monitoring phone call
		tm.listen(listenerPhone, PhoneStateListener.LISTEN_NONE);
		listenerPhone = null;

		// use code to unregister
		unregisterReceiver(receiver);
		receiver = null;

	}

	/**
	 * customized toast
	 * 
	 * @param address
	 */
	private WindowManager.LayoutParams params; //Mobile phone's X & Y
	long[] mHits = new long[2]; //control clicking time
	public void myToast(String address) {
		view = View.inflate(this, R.layout.address_show, null);
		TextView textview = (TextView) view.findViewById(R.id.tv_address);
		
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();
				if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
					// double click in time
					params.x = wm.getDefaultDisplay().getWidth()/2-view.getWidth()/2;
					wm.updateViewLayout(view, params);
					Editor editor = sp.edit();
					editor.putInt("lastx", params.x);
					editor.commit();
				}
			}
		});

		view.setOnTouchListener(new OnTouchListener() {
			int startX;
			int startY;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: //press down on the view
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					Log.i(TAG, "start position: " + startX + ": " +startY);
					break;
				case MotionEvent.ACTION_MOVE: //press and move the view
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();
					Log.i(TAG, "new position: " + newX + ": " +newY);
					int offsetX = newX - startX;
					int offsetY = newY - startY;
					Log.i(TAG, "offset: " + offsetX + ": " +offsetY);
					params.x += offsetX;
					params.y += offsetY;
					
					//bound constrain
					if (params.x < 0) {
						params.x = 0;
					}
					if (params.y < 0) {
						params.y = 0;
					}
					//wm.getDefaultDisplay().getWidth() gets window's width; view.getWidth() gets toast's width
					if (params.x > wm.getDefaultDisplay().getWidth() - view.getWidth()) {
						params.x = wm.getDefaultDisplay().getWidth() - view.getWidth();
					}
					if (params.y > wm.getDefaultDisplay().getHeight() - view.getHeight()) {
						params.y = wm.getDefaultDisplay().getHeight() - view.getHeight();
					}
					
					wm.updateViewLayout(view, params);
					// reset the position
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP: //release the view

					Editor editor = sp.edit();
					editor.putInt("lastx", params.x);
					editor.putInt("lasty", params.y);
					editor.commit();
					break;
				default:
					break;
				}
				return false; //return true in a listener means the event is over
			}
		});

		// "translucent","Cadmium Orange","Bright cerulean","Metal Grey","Apple
		// Green"
		int[] ids = { R.drawable.call_locate_white, R.drawable.call_locate_orange, R.drawable.call_locate_blue,
				R.drawable.call_locate_gray, R.drawable.call_locate_green };

		sp = getSharedPreferences("config", MODE_PRIVATE);

		view.setBackgroundResource(ids[sp.getInt("style_index", 0)]);
		textview.setText(address);

		// parameters of the interface
		params = new WindowManager.LayoutParams();

		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;

		// put the toast to the top-left corner of the screen initially
		params.gravity = Gravity.TOP + Gravity.LEFT;

		// set the toast window pixel
		params.x = sp.getInt("lastx", 100); // 100 pixels to the right
		params.y = sp.getInt("lasty", 100); // 100 pixels to the bottom

		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // |
																		// WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		params.format = PixelFormat.TRANSLUCENT;
		// This type must be set in order to move the toast + SYSTEM_ALERT_WINDOW permission
		params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE; //influence users' operation
		// -----------------------------
		wm.addView(view, params);

	}

}
