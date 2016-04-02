package com.jingtian.mobileguardian;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AntiTheftSetup3 extends BaseSetupActivity {

	protected EditText etPhone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti_theft_setup3);
		
		etPhone = (EditText) findViewById(R.id.et_setup3_phone);
		
		etPhone.setText(sp.getString("secure_number", null));
	}

	
	@Override
	public void showNext() {
		String phoneNumber = etPhone.getText().toString().trim();
		if (TextUtils.isEmpty(phoneNumber)) {
			Toast.makeText(this, "Please enter a secure number to proceed", 1).show();;
			return;
		}
		
		//save the secure number
		Editor editor = sp.edit();
		editor.putString("secure_number", phoneNumber);
		editor.commit();
		
		
		Intent intent = new Intent(this, AntiTheftSetup4.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
		
	}

	@Override
	public void showPre() {
		Intent intent = new Intent(this, AntiTheftSetup2.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
	}
	
	
	/**
	 * select contacts listener
	 * @param view
	 */
	public void selectContact(View view){
		Intent intent = new Intent(this, SelectContactActivity.class);
		startActivityForResult(intent, 0);
	}
	
	/**
	 * invoke when return back the function results
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data == null) { //click back button, then data will be null
			return;
		}
		
//		String phoneNumber = data.getStringExtra("phone_number");
		
//		if want to change format, using the following:		
		String phoneNumber = data.getStringExtra("phone_number").replace("-", "");
		
		etPhone.setText(phoneNumber);
		
		
	}
}
