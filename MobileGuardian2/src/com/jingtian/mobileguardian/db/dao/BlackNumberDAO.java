package com.jingtian.mobileguardian.db.dao;

import java.util.ArrayList;
import java.util.List;

import com.jingtian.mobileguardian.db.BlackNumberDBOpenHelper;
import com.jingtian.mobileguardian.domain.BlackNumberInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/*
 * Add, Delete, Modify, Search of the BlackNumber database
 */
public class BlackNumberDAO {

	private BlackNumberDBOpenHelper helper;

	/**
	 * constructor
	 * @param context
	 */
	public BlackNumberDAO(Context context) {
		helper = new BlackNumberDBOpenHelper(context); //create database if has not
	}
	
	/**
	 * search if the black number exists in the database
	 * @param number
	 */
	public boolean searchBlackNumber(String number){
		boolean result = false;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from blacknumber where number = ?", new String[]{number});
		while (cursor.moveToNext()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
	
	/**
	 * search the black number exists in the database and return the mode
	 * @param number 
	 */
	public String searchBlackNumberMode(String number){
		String result = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select mode from blacknumber where number = ?", new String[]{number});
		while (cursor.moveToNext()) {
			result = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return result;
	}
	
	/**
	 * search all black numbers
	 * @param number
	 */
	public List<BlackNumberInfo> searchAllBlackNumber(){
		List<BlackNumberInfo> resultList = new ArrayList<BlackNumberInfo>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select number,mode from blacknumber order by id desc",null);
		while (cursor.moveToNext()) {
			BlackNumberInfo blackNumber = new BlackNumberInfo();
			String number = cursor.getString(0);
			String mode = cursor.getString(1);
			blackNumber.setNumber(number);
			blackNumber.setMode(mode);
			resultList.add(blackNumber);
		}
		cursor.close();
		db.close();
		return resultList;
	}
	
	/**
	 * search part of the result
	 * @param offset: start position
	 * @param limit: number of records
	 * @return
	 */
	public List<BlackNumberInfo> searchPartBlackNumber(int offset, int limit){
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		List<BlackNumberInfo> resultList = new ArrayList<BlackNumberInfo>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select number,mode from blacknumber order by id desc limit ? offset ?",
				new String[]{String.valueOf(limit), String.valueOf(offset)});
		while (cursor.moveToNext()) {
			BlackNumberInfo blackNumber = new BlackNumberInfo();
			String number = cursor.getString(0);
			String mode = cursor.getString(1);
			blackNumber.setNumber(number);
			blackNumber.setMode(mode);
			resultList.add(blackNumber);
		}
		cursor.close();
		db.close();
		return resultList;
	}
	
	/**
	 * insert a new blacknumber and its blockade mode
	 * @param number
	 * @param blockade mode : 1-call  2-SMS  3-Both
	 */
	public void addBlackNumber(String number, String mode){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("number", number);
		values.put("mode", mode);
		db.insert("blacknumber", null, values);
		db.close();
	}
	
	/**
	 * modify the blockade mode of a blacknumber
	 * @param number
	 * @param new blockade mode
	 */
	public void updateBlackNumberMode(String number, String newMode){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", newMode);
		db.update("blacknumber", values, "number = ?", new String[]{number});
		db.close();
	}
	
	/**
	 * delete a blacknumber
	 * @param number
	 */
	public void deleteBlackNumber(String number){
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("blacknumber", "number = ?",new String[]{number});
		db.close();
	}
	
	/**
	 * delete all blacknumbers
	 * @param number
	 */
	public void deleteAll(){
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("blacknumber", null, null);
		db.close();
	}
}
