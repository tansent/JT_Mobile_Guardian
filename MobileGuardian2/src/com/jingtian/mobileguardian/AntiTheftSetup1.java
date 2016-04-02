package com.jingtian.mobileguardian;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class AntiTheftSetup1 extends BaseSetupActivity {

	//define a gesture detector
	private GestureDetector detector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti_theft_setup1);
		
//		detector = new GestureDetector(this, new SimpleOnGestureListener(){
//			
//		});
	
	}
	
//	public void next(View view){
//		Intent intent = new Intent(this, AntiTheftSetup2.class);
//		startActivity(intent);
//		finish();
//		//this method can only be used after "startActivity(intent)" or "finish()"
//		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
//	}

	@Override
	public void showNext() {
		Intent intent = new Intent(this, AntiTheftSetup2.class);
		startActivity(intent);
		finish();
		//this method can only be used after "startActivity(intent)" or "finish()"
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
		
	}

	@Override
	public void showPre() {
		// TODO Auto-generated method stub
		
	}
}
