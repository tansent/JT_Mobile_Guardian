package com.jingtian.mobileguardian.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import com.jingtian.mobileguardian.domain.SMSInfo;

import android.R.xml;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

/*
 * upside for using XML file to store sms: multi-platform
 * format:
 * <? XML Version>
 * <smss>
 * 		<sms>
 * 			<body>hello</body>
 *  		<date>23123241</date>
 *   		<type>1</type>
 *    		<address>5556</address>
 * 		</sms>
 * 		
 * 		...
 * 	
 * </smss>
 * 
 * permissions needed:
 * READ_SMS
 * WRITE_SMS
 */

public class SmsUtils {

	/**
	 * SMS backup interface callback
	 * 
	 * To extract the UI part from logic part. The best way to do this is to expose an interface
	 * Utils class only handle logic not UI
	 */
	public interface BackUpCallBack{
		
		/**
		 * When starting the backup, setting the max value
		 * @param max
		 */
		public void beforeBackup(int max);
		
		/**
		 * backing up, setting progress
		 * @param progress
		 */
		public void onSmsBackup(int progress);
		
	}


	private static final String TAG = "SmsUtils"; 
	
	
	private static List<SMSInfo> smsList;
	private static SMSInfo sms;
	private static int smsCount;
	
	/**
	 * back up users' messages of the phone
	 * @param context
	 * @param backUpCallBack
	 * @throws Exception
	 */
	public static void backupSms(Context context, BackUpCallBack backUpCallBack) throws Exception{ //Exception in utils class better get thrown
		ContentResolver resolver = context.getContentResolver();
		
		File file = new File(Environment.getExternalStorageDirectory(), "backup.xml");
		FileOutputStream fos = new FileOutputStream(file);
		//read user's sms one by one and write according to the format to the file in SD card
		XmlSerializer serializer = Xml.newSerializer(); //serializer: rom -> file
		
		//initialize serializer
		serializer.setOutput(fos, "UTF-8");
		serializer.startDocument("UTF-8", true);
		
		//set "<smss>" tag
		serializer.startTag(null, "smss");
		
		//com.android.providers.telephony -> databases/mmssms.db
		Uri uri = Uri.parse("content://sms/"); //restore all sms (inbox / sent / draft)
		Cursor cursor = resolver.query(uri, new String[]{"body","address","type","date"}, null, null, null);
		
		//when beginning backup, setting the progress bar
		int max = cursor.getCount();
		backUpCallBack.beforeBackup(max);
//		progressDialog.setMax(max);
		
		serializer.attribute(null, "max", max+"");
		int progress = 0;
		while (cursor.moveToNext()) {
			//Thread.sleep(500);  
			String body = cursor.getString(0);
			String address = cursor.getString(1);
			String type = cursor.getString(2);
			String date = cursor.getString(3);
			
			serializer.startTag(null, "sms");
			
			serializer.startTag(null, "body");
			serializer.text(body);
			serializer.endTag(null, "body");
			
			serializer.startTag(null, "address");
			serializer.text(address);
			serializer.endTag(null, "address");
			
			serializer.startTag(null, "type");
			serializer.text(type);
			serializer.endTag(null, "type");
			
			serializer.startTag(null, "date");
			serializer.text(date);
			serializer.endTag(null, "date");
			
			serializer.endTag(null, "sms");
			
			progress++;
			backUpCallBack.onSmsBackup(progress);
//			progressDialog.setProgress(progress);
		}
		
		serializer.endTag(null, "smss");
		
		serializer.endDocument();
		cursor.close();
		fos.close();
	}
	
	
	/**
	 * restore context
	 * @param context
	 * @throws Exception 
	 * 
	 * May not work over 4.4
	 */
	private static int showNumber = 0;
	public static void restoreSMS(Context context, int isSaveOriginal,  BackUpCallBack backUpCallBack) throws Exception{
		
		Uri uri = Uri.parse("content://sms/");
		if (isSaveOriginal == 2) {
			context.getContentResolver().delete(uri, null, null);
		}
		//1.read sms xml file on the SD card
		File file = new File(Environment.getExternalStorageDirectory(), "backup.xml");
		FileInputStream inputStream = new FileInputStream(file);
		
//		if (file.exists()) {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//			reader.readLine();
//		}
		//2.read max value to judge how many messages the phone has
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inputStream, "utf-8");
		
		int type = parser.getEventType();
		//3.parse each message (body,address,type,date)
		while (type != XmlPullParser.END_DOCUMENT) {
			switch (type) {
			case XmlPullParser.START_TAG:
				if ("smss".equals(parser.getName())) {
					//create a sms set to store messages
					smsList = new ArrayList<SMSInfo>();
					String count = (String)parser.getAttributeValue(null, "max");
					smsCount = Integer.parseInt(count);
					
					backUpCallBack.beforeBackup(smsCount);
				}else if ("sms".equals(parser.getName())) {
					sms = new SMSInfo();
				}else if ("body".equals(parser.getName())) {
					String body = parser.nextText();
					sms.setBody(body);
				}else if ("address".equals(parser.getName())) {
					String address = parser.nextText();
					sms.setAddress(address);
				}else if ("date".equals(parser.getName())) {
					String date = parser.nextText();
					sms.setDate(date);
				}else if ("type".equals(parser.getName())) {
					String typee = parser.nextText();
					sms.setType(typee);
				}
				break;
				
			case XmlPullParser.END_TAG:
				if ("sms".equals(parser.getName())) {
					showNumber++;
					backUpCallBack.onSmsBackup(showNumber);
					//add a sms set to the list
					smsList.add(sms);
//					Thread.sleep(500);  
				}
				break;
				

			default:
				break;
			}
			
			//move the pointer to the next
			type = parser.next();
		}
		
		
		//4.insert the parse results the system application
		for(int i = 0; i < smsList.size(); i++){
			//insert sms to the phone
			ContentValues values = new ContentValues();
			values.put("body", smsList.get(i).getBody());
			values.put("address", smsList.get(i).getAddress());
			values.put("type", smsList.get(i).getType());
			values.put("date", smsList.get(i).getDate());
			context.getContentResolver().insert(uri, values);
		}
		
		
		//insert sms demo
//		ContentValues values = new ContentValues();
//		values.put("body", "body");
//		values.put("address", "address");
//		values.put("type", "1");
//		values.put("date", "1452637923799");
//		context.getContentResolver().insert(uri, values);
		
	}
}
