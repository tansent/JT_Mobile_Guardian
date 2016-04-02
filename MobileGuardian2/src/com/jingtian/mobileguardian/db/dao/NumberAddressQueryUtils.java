package com.jingtian.mobileguardian.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NumberAddressQueryUtils {

	private static String path = "data/data/com.jingtian.mobileguardian/files/address.db";

	/**
	 * give a phone number to return a location (query database)
	 * 
	 * @return location string
	 */
	public static String queryNumber(String phoneNumber) {
		String location = phoneNumber;

		// create / open database
		SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

		// normal phone number: 13 14 15 16 18
		if (phoneNumber.matches("^1[34568]\\d{9}$")) { // ^1[34568]\d{9}$
			// query phone number from the database
			Cursor cursor = database.rawQuery(
					"select location from data2 where id = (select outkey from data1 where id = ?)",
					new String[] { phoneNumber.substring(0, 7) }); // 0~6
			while (cursor.moveToNext()) {
				location = cursor.getString(0);
				// break;
			}
			cursor.close();
		} else {
			// other phone number
			switch (phoneNumber.length()) {
			case 3:
				location = "special number";
				break;

			case 4:
				location = "emulator number";
				break;
				
			case 5:
				location = "service number";
				break;
				
			case 7:
				location = "local number";
				break;
				
			case 8:
				location = "local number";
				break;
				
			default:
				//international phone number
				if (phoneNumber.length() > 10 && phoneNumber.startsWith("0") ) {
					//instance: 010-59790386
					Cursor cursor = database.rawQuery(
							"select location from data2 where area = ?",
							new String[] { phoneNumber.substring(1, 3) }); 
					
					while (cursor.moveToNext()) {
						location = cursor.getString(0);
						location = location.substring(0, location.length() - 2); //only get location, not the carrier
						
					}
//					cursor.close();
					//if there is no such number(010-59790386), the code aboce will return null
					
					//instance: 0855-59790386
					cursor = database.rawQuery(
							"select location from data2 where area = ?",
							new String[] { phoneNumber.substring(1, 4) }); 
					
					while (cursor.moveToNext()) {
						location = cursor.getString(0);
						location = location.substring(0, location.length() - 2); //only get location, not the carrier
						
					}
					cursor.close();
				}
				
				break;
			}
		}

		return location; // if get the result, return location; if not get the
							// result, return the phone number
	}
}
