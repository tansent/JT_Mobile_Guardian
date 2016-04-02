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
public class SettingItemView extends RelativeLayout {

	private CheckBox cb_status;
	private TextView tv_title;
	private TextView tv_desc;
	private String desc_on;
	private String desc_off;
	private String title;
	
	public void initView(Context context) {
		
		// carry the layout to the current class
		View.inflate(context, R.layout.setting_item_view, this);
		cb_status = (CheckBox) this.findViewById(R.id.cb_status);
		tv_title = (TextView) this.findViewById(R.id.tv_title);
		tv_desc = (TextView) this.findViewById(R.id.tv_desc);
	}
	
	public SettingItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	//by default, two-parameter constructor is invoked
	public SettingItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		
		title = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.jingtian.mobileguardian", "title");
		desc_on = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.jingtian.mobileguardian", "desc_on");
		desc_off = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.jingtian.mobileguardian", "desc_off");
		tv_title.setText(title);
		setDesc(desc_off);
	
	}

	public SettingItemView(Context context) {
		super(context);
		initView(context);
	}
	
	/**
	 * if the controller is selected
	 * @return
	 */
	public boolean isChecked() {
		return cb_status.isChecked();
	}
	
	/**
	 * set checkbox's status
	 * @param checked
	 */
	public void setChecked(boolean checked){
		if (checked) {
			setDesc(desc_on);
		}else {
			setDesc(desc_off);
		}
		cb_status.setChecked(checked);
	}

	/**
	 * set textview's content
	 * @param checked
	 */
	public void setDesc(String desc){
		tv_desc.setText(desc);
	}
	
	/**
	 * get textview's content
	 * @param checked
	 */
	public String getDesc(){
		return (String) tv_desc.getText();
	}
}
