package com.jingtian.mobileguardian;

import com.jingtian.mobileguardian.ui.SettingItemView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class AntiTheftSetup2 extends BaseSetupActivity {

	private SettingItemView siv_update2;
	private TelephonyManager tm; //need READ_PHONE_STATE uses-permission

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti_theft_setup2);

		// use TelephonyManager can obtain SIM Card information
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String sim = sp.getString("sim", null);

		siv_update2 = (SettingItemView) findViewById(R.id.siv_update2);

		if (TextUtils.isEmpty(sim)) {
			siv_update2.setChecked(false);
		} else {
			siv_update2.setChecked(true);
		}
		siv_update2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// save numbers of the SIM card
				String sim = tm.getSimSerialNumber();
				Editor editor = sp.edit();

				if (siv_update2.isChecked()) { // already checked
					siv_update2.setChecked(false); //set state
					// siv_update.setDesc("update notification off");
					editor.putString("sim", null);
				} else {
					siv_update2.setChecked(true);
					// siv_update.setDesc("update notification on");
					editor.putString("sim", sim);

				}
				editor.commit();

			}
		});
	}

	// /**
	// * Next Button
	// * @param view
	// */
	// public void next(View view){
	// Intent intent = new Intent(this, AntiTheftSetup3.class);
	// startActivity(intent);
	// finish();
	// overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
	// }
	//
	// /**
	// * Previous Button
	// * @param view
	// */
	// public void pre(View view){
	// Intent intent = new Intent(this, AntiTheftSetup1.class);
	// startActivity(intent);
	// finish();
	// overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
	// }

	@Override
	public void showNext() {
		String sim = sp.getString("sim", null);
		if (TextUtils.isEmpty(sim)) {
			Toast.makeText(AntiTheftSetup2.this, "Please bind SIM Card to continue", 1).show();
			return;
		}
		
		Intent intent = new Intent(this, AntiTheftSetup3.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
	}

	@Override
	public void showPre() {
		Intent intent = new Intent(this, AntiTheftSetup1.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
	}
}
