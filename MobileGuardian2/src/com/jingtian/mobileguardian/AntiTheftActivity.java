package com.jingtian.mobileguardian;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AntiTheftActivity extends Activity {

	private SharedPreferences sp;
	private TextView tvProtecting;
	private ImageView ivProtecting;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		//judge if the initial configuration has set,
		boolean configed = sp.getBoolean("configed", false);
		if (configed) {
			//if the configuration has set, stay at the current page
			setContentView(R.layout.activity_anti_theft);
			
			//findViewById can only used AFTER setContentView of the current page
			tvProtecting = (TextView) findViewById(R.id.tv_protecting);
			ivProtecting = (ImageView) findViewById(R.id.iv_protecting);
			
			//set secure number
			String secureNumber = sp.getString("secure_number", ""); // "" will not pop up nullpointerexception
			tvProtecting.setText(secureNumber);
			
			boolean protecting = sp.getBoolean("protecting", false);
			if (protecting) {
				//anti-theft protecting is on
				ivProtecting.setImageResource(R.drawable.lock);
			} else {
				//anti-theft protecting is off
				ivProtecting.setImageResource(R.drawable.unlock);
			}
			
			Toast.makeText(getApplicationContext(), "Send commands by bound secure number to trigger effects", 1).show();
		} else {
			//configuration has not been set, switch to the setting page
			Intent intent = new Intent(this, AntiTheftSetup1.class);
			startActivity(intent);
			//turn off the current page
			finish();
		}
		
	}
	
	public void enterSetup(View view){
		Intent intent = new Intent(AntiTheftActivity.this,AntiTheftSetup1.class);
		startActivity(intent);
		//close current page
		finish();
	}
}
