package com.jingtian.mobileguardian.ui;

import com.jingtian.mobileguardian.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

/*
 * self-defined controller, including textview and checkbox
 * just like a javabean, this can be called as a view bean
 */
public class SettingClickView extends RelativeLayout {

	private TextView tv_title_bg;
	private TextView tv_desc_bg;
	private String desc_bg_on;
	private String desc_bg_off;
	
	public void initView(Context context) {
		
		// carry the layout to the current class
		View.inflate(context, R.layout.setting_click_view, this);
		tv_title_bg = (TextView) this.findViewById(R.id.tv_title_bg);
		tv_desc_bg = (TextView) this.findViewById(R.id.tv_desc_bg);
	}
	
	public SettingClickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	//by default, two-parameter constructor is invoked
	public SettingClickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		
		String title = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.jingtian.mobileguardian", "title_bg");
		desc_bg_on = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.jingtian.mobileguardian", "desc_bg_on");
		desc_bg_off = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.jingtian.mobileguardian", "desc_bg_off");
//		tv_title_bg.setText(title);
		setTitle(title);
		setDesc(desc_bg_off);
	
	}

	public SettingClickView(Context context) {
		super(context);
		initView(context);
	}
	

	/**
	 * set textview's content
	 * @param checked
	 */
	public void setDesc(String desc){
		tv_desc_bg.setText(desc);
	}
	
	/**
	 * get textview's content
	 * @param checked
	 */
	public String getDesc(){
		return (String) tv_desc_bg.getText();
	}
	
	/**
	 * set textview's title
	 * @param checked
	 */
	public void setTitle(String title){
		tv_title_bg.setText(title);
	}
	
	/**
	 * get textview's title
	 * @param checked
	 */
	public String getTitle(){
		return (String) tv_title_bg.getText();
	}
}
