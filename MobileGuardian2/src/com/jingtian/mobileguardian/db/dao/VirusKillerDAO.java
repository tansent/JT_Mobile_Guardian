package com.jingtian.mobileguardian.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * virus detection
 * @author Tylor
 *
 */
public class VirusKillerDAO {

	/**
	 * search if the digest of the file exists in the virus database
	 * @param md5
	 * @return
	 */
	public static boolean isVirus(String md5){
		String path = "/data/data/com.jingtian.mobileguardian/files/antivirus.db";
		boolean result = false;
		//turn on the virus database
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("select * from datable where md5 = ?", new String[]{md5});
		while (cursor.moveToNext()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
}
