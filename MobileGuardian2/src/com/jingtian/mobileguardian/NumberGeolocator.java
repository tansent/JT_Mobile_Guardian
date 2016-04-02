package com.jingtian.mobileguardian;

import com.jingtian.mobileguardian.db.dao.NumberAddressQueryUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 * need uses-permission  android.permission.VIBRATE
 */
public class NumberGeolocator extends Activity {

	private static final String TAG = "NumberGeolocator";
	private EditText ed_phone;
	private TextView tv_result;
	private Vibrator vibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_number_guery);
		ed_phone = (EditText) findViewById(R.id.ed_phone);
		tv_result =  (TextView) findViewById(R.id.tv_result); //
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		//change dynamically (no need to click button)
		ed_phone.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s != null && s.length() > 3) {
					String location = NumberAddressQueryUtils.queryNumber(s.toString());
					tv_result.setText(location);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	/**
	 * Query the number
	 * @param view
	 */
	public void numberAddressQuery(View view){
		String phone = ed_phone.getText().toString().trim();
		if (TextUtils.isEmpty(phone)) {
			Toast.makeText(this, "Please enter a phone number", 0).show();
			
			//define and play an animation
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
//			shake.setInterpolator(new Interpolator() {  //self-define animation
//				
//				@Override
//				public float getInterpolation(float input) {
//					// TODO Auto-generated method stub
//					return 0;
//				}
//			});
			ed_phone.startAnimation(shake);
			vibrator.vibrate(1000); // vibrate for 1 sec
			
//			long[] pattern = {200,200,300,300}; // vib, stop, vib ,stop
//			vibrator.vibrate(pattern, 1); //2nd para: starting from which pattern in the array
			//--------------------------
			
			return;
		}else {
			//2 ways to query the phone number database:
			//1. through internet    2. through local database
			//write a utils class to connect to the phone database
			Log.i(TAG, "The number you want to query is \n"+phone);
			
			String location = NumberAddressQueryUtils.queryNumber(phone);
			if(location.equals(phone)){
				tv_result.setText("Sorry, cannot get the location");
			}else {
				tv_result.setText(location);
			}
			
		}
	}
}
