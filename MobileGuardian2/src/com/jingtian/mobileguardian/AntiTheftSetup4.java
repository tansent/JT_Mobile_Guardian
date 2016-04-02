package com.jingtian.mobileguardian;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AntiTheftSetup4 extends BaseSetupActivity {

	private SharedPreferences sp;
	
	private CheckBox protecting;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti_theft_setup4);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		protecting = (CheckBox) findViewById(R.id.cb_setup4_protecting);
		
		boolean protectingState = sp.getBoolean("protecting", false);
		if (protectingState) {
			protecting.setChecked(true);
			protecting.setText("Anti-Theft function on ");
		}else {
			protecting.setChecked(false);
			protecting.setText("Anti-Theft function off ");
		}
		
		protecting.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			//parameters stands for state AFTER checking
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					protecting.setText("Anti-Theft function on ");
				} else {
					protecting.setText("Anti-Theft function off ");
				}
				
				Editor editor = sp.edit();
				editor.putBoolean("protecting", isChecked);
				editor.commit();
			}
		});
	}
	
//	/**
//	 * Next Button
//	 * @param view
//	 */
//	public void next(View view){
//		Intent intent = new Intent(this, AntiTheftActivity.class);
//		startActivity(intent);
//		
//		Editor editor = sp.edit();
//		editor.putBoolean("configed", true);
//		editor.commit();
//		finish();
//		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
//	}
//
//	/**
//	 * Previous Button
//	 * @param view
//	 */
//	public void pre(View view){
//		Intent intent = new Intent(this, AntiTheftSetup3.class);
//		startActivity(intent);
//		finish();
//		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
//	}

	@Override
	public void showNext() {
		Intent intent = new Intent(this, AntiTheftActivity.class);
		startActivity(intent);
		
		Editor editor = sp.edit();
		editor.putBoolean("configed", true);
		editor.commit();
		finish();
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
		
	}

	@Override
	public void showPre() {
		Intent intent = new Intent(this, AntiTheftSetup3.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
	}
}
