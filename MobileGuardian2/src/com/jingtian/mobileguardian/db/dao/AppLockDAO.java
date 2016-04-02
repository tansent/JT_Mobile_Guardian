package com.jingtian.mobileguardian.db.dao;

import java.util.ArrayList;
import java.util.List;

import com.jingtian.mobileguardian.db.AppLockDBOpenHelper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppLockDAO {

	private AppLockDBOpenHelper helper;
	private Context context;
	
	/**
	 * constructor
	 * @param context
	 */
	public AppLockDAO(Context context){
		helper = new AppLockDBOpenHelper(context);
		this.context = context;
	}
	
	/**
	 * add a package name of an app that is to be locked
	 * @param package_name
	 */
	public void add(String packName){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("package_name", packName);
		db.insert("applock", null, values);
		db.close();
		
		//send a broadcast to notify data change in the list
		Intent intent = new Intent();
		intent.setAction("com.jingtian.mobileguardian.locklistchange");
		context.sendBroadcast(intent);
		//another way with same effect:
		//context.getContentResolver().notifyChange(uri, observer);
	}
	
	/**
	 * delete a package name of an app that is to be locked
	 * @param package_name
	 */
	public void delete(String packName){
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("applock", "package_name = ?", new String[]{packName});
		db.close();
		
		//send a broadcast to notify data change in the list
		Intent intent = new Intent();
		intent.setAction("com.jingtian.mobileguardian.locklistchange");
		context.sendBroadcast(intent);
	}
	
	/**
	 * search a package name of an app that is to be locked
	 * @param package_name
	 */
	public boolean search(String packName){
		boolean result = false;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("applock", null, "package_name = ?", new String[]{packName}, null, null, null);
		if (cursor.moveToNext()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
	
	/**
	 * search all package
	 * @return
	 */
	public List<String> searchAll(){
		//strange error???
//		List<String> protectPackNames = new ArrayList<String>();
//		SQLiteDatabase db = helper.getReadableDatabase();
//		Cursor cursor = db.query("applock", new String[]{"package_name"},null, null, null, null, null);
//		if (cursor.moveToNext()) {
//			protectPackNames.add(cursor.getString(0));
//		}
//		cursor.close();
//		db.close();
//		return protectPackNames;
		List<String> protectPacknames = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("applock", new String[]{"package_name"}, null,null, null, null, null);
		while(cursor.moveToNext()){
			protectPacknames.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		return protectPacknames;
	}
}
