package com.jingtian.mobileguardian;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public abstract class BaseSetupActivity extends Activity {
	//1.define a GestureDetector
	private GestureDetector detector;
	
	protected SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		//2.instance
		detector = new GestureDetector(this, new SimpleOnGestureListener(){

			/**
			 * Invoke when fling fingers
			 */
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, //position
					float velocityX, float velocityY) {  //speed
				
				
				if(Math.abs(velocityX)<20){
					Toast.makeText(getApplicationContext(), "fling too slowly", 0).show();
					return true;
				}
				
				if(Math.abs((e2.getRawY() - e1.getRawY())) > 200){
					Toast.makeText(getApplicationContext(), "cannot fling this way", 0).show();
					
					return true;
				}
				
				if((e2.getRawX() - e1.getRawX())> 200 ){
					System.out.println("show previous page: from left to right");
					showPre();
					return true;
					
				}
				
				if((e1.getRawX()-e2.getRawX()) > 200 ){
					System.out.println("show next page: from right to left");
					showNext();
					return true;
				}
				
				return super.onFling(e1, e2, velocityX, velocityY);
			}
			
		});
	}
	
	public abstract void showNext();
	public abstract void showPre();
	/**
	 * next
	 * @param view
	 */
	public void next(View view){
		showNext();
		
	}
	
	/**
	 *   previous
	 * @param view
	 */
	public void pre(View view){
		showPre();
		
	}
		
	//3.use GestureDetector
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
}
