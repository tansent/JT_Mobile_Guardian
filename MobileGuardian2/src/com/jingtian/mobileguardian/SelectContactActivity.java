package com.jingtian.mobileguardian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SelectContactActivity extends Activity {

	private ListView select_contact;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_contact);
		
		select_contact = (ListView) findViewById(R.id.list_select_contact);
		
		final List<Map<String, String>> data = getContactInfo();
		//adapter: data -> layout
		select_contact.setAdapter(new SimpleAdapter(this, 
				data, 
				R.layout.activity_contact_item,
				new String[]{"name","phone_number"},  //must be the same name corresponding with the names stored in data
				new int[]{R.id.tv_name,R.id.tv_phone_number}));
		
		//monitor events when clicking on the layout
		select_contact.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				String phone_number = data.get(position).get("phone_number"); 
				
				Intent resultData = new Intent();
				resultData.putExtra("phone_number", phone_number);
				setResult(0, resultData);
				
				finish(); //finish current page
			}
		});
	}
	
	
	
	/**
	 * get contacts info from the phone 
	 * 
	 * read three tables: raw_contacts, data, mimetypes
	 * @return
	 */
	private List<Map<String, String>> getContactInfo(){
		
		//store contacts
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		
		//get a resolver
		ContentResolver resolver = getContentResolver();
		//read info from raw_contacts table
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts"); //path of raw_contacts table
		Uri uriData = Uri.parse("content://com.android.contacts/data"); //path of data table
		Cursor cursor = resolver.query(uri, 
				new String[]{"contact_id"}, //fields in the table that we concern
				null, null, null);
		
		while (cursor.moveToNext()) {
			String contact_id = cursor.getString(0);//getString(0) means get contact_id
			
			if (contact_id != null) {
				//a new contact here
				Map<String, String> map = new HashMap<String, String>();
				
				Cursor dataCursor = resolver.query(uriData, 
						new String[]{"data1","mimetype"}, 
						"contact_id=?",  //query condition
						new String[]{contact_id}, //query parameters
						null);
				
				while (dataCursor.moveToNext()) {
					String data1 = dataCursor.getString(0);
					String mimetype = dataCursor.getString(1);
					System.out.println("data1="+data1+" mimetype="+mimetype);
					
					if ("vnd.android.cursor.item/name".equals(mimetype)) {
						//contacts' name
						map.put("name", data1);
						
					}else if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
						//contacts' phone number
						map.put("phone_number", data1);
					}
				}
				
				list.add(map);
				dataCursor.close();
			}
		}
		
		
		cursor.close();
		return list;
	}	
	
}
