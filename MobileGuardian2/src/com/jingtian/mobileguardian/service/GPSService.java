package com.jingtian.mobileguardian.service;

import java.io.IOException;
import java.io.InputStream;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

/*
 * need uses-permissions:
 * ACCESS_FINE_LOCATION
 * ACCESS_COARSE_LOCATION
 * ACCESS_MOCK_LOCATION
 */
public class GPSService extends Service {

	private LocationManager lm;
	private MyLocationListener listener;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
//		List<String> provider = lm.getAllProviders(); // get all location methods
		
		listener = new MyLocationListener();
		//set criteria
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		
		//set more criteria details:
//		criteria.setAccuracy(Criteria.ACCURACY_FINE);//set max accuracy
//		criteria.setAltitudeRequired(false);//no altitude information
//		criteria.setBearingRequired(false);//no direction information
//		criteria.setCostAllowed(true);//whether it is cost allowed
//		criteria.setPowerRequirement(Criteria.POWER_LOW);//set power requirement 
		
		String bestProvider = lm.getBestProvider(criteria, true);
		
		lm.requestLocationUpdates(bestProvider, //criteria will dynamically choose gps, base_station and network
				0, //60000 (i min)
				0, //50
				listener);
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		lm.removeUpdates(listener);
		listener = null;
	}
	
	class MyLocationListener implements LocationListener{

		/**
		 * invoke when location got changed
		 */
		@Override
		public void onLocationChanged(Location location) {
			String longtitude = "longitude " + location.getLongitude()+"\n";
			String latitude = "latitude " + location.getLatitude()+"\n";
			String accuracy = "accuracy " + location.getAccuracy()+"\n";
			
//			// convert the actual location to the Mars-Location
//			InputStream is;
//			try {
//				is = getAssets().open("axisoffset.dat"); //convert the database to the Stream
//				ModifyOffset offset = ModifyOffset.getInstance(is);
//				PointDouble point = offset.s2c(new PointDouble(location.getLongitude(), location.getLatitude()));
//				
//				longtitude = "longitude " + offset.X +"\n";
//				latitude = "latitude " + offset.Y +"\n";
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			//send the last position to the secure number
			SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putString("last_location", longtitude + latitude + accuracy);
			editor.commit();
		}

		/**
		 * invoke when turn off / turn on locating service
		 */
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		/**
		 * invoke when turn on locating service
		 */
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		/**
		 * invoke when turn on locating service
		 */
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
