package com.jingtian.mobileguardian.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * SQLiteOpenHelper is used to create database
 */
public class AppLockDBOpenHelper extends SQLiteOpenHelper {

	/**
	 * Database named "applock.db" starting with version 1
	 * @param context
	 */
	public AppLockDBOpenHelper(Context context) {
		super(context, "applock.db", null, 1); 
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table applock (id integer primary key autoincrement, package_name varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
