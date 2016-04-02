package com.jingtian.mobileguardian.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/*
 * when booting up(when sending this broadcast), we compare current
 * SIM card information of the phone with the SIM card information
 * stored in the SharedPreference file
 * 
 * note: broadcast itself does not contain SIM card information,
 * just taking advantage of the timing
 * 
 * need SEND_SMS uses-permission
 */

//need RECEIVE_BOOT_COMPLETED uses-permission
public class BootCompleteReceiver extends BroadcastReceiver {

	private static final String TAG = "BootCompleteReceiver";
	private SharedPreferences sp; //use to obtain SIM card information stored in the SharedPreference file
	private TelephonyManager tm;  //use to obtain current SIM card information
	
	@Override
	public void onReceive(Context context, Intent intent) {
		sp = context.getSharedPreferences("sim", Context.MODE_PRIVATE);
		
		boolean protecting = sp.getBoolean("protecting", false);
		if (protecting) {
			//anti-theft function is on
			tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			
			//get SIM card information stored in the SharedPreference file
			String savedSIM = sp.getString("sim", "") + "for_testing_use";
			
			//get current SIM card information 
			String currentSIM = tm.getSimSerialNumber();
			
			Log.i(TAG, savedSIM + "----" + currentSIM);
			
			if (savedSIM.equals(currentSIM)) {
				// SIM card not changed
				
			} else {
				// SIM card changed
				Toast.makeText(context, "SIM Card changed", 0).show();
				System.out.println("SIM card change");
				Log.i(TAG, "SIM card change!!!!!!!");
				
				//send SMS (need SEND_SMS uses-permission)
				
				SmsManager.getDefault().sendTextMessage(
						sp.getString("secure_number", ""), //sending number
						null, 
						"Your SIM Card has been switched", 
						null, null);
			}
		}
		
		
	}

}
