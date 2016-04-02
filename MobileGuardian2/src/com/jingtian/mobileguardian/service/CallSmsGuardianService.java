package com.jingtian.mobileguardian.service;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.jingtian.mobileguardian.db.dao.BlackNumberDAO;
import com.jingtian.mobileguardian.receiver.SMSReceiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


/*
 * permissions needed:
 * CALL_PHONE
 * READ_CALL_LOG
 * WRITE_CALL_LOG
 * WRITE_CONTACTS
 */
public class CallSmsGuardianService extends Service {

	public static final String TAG = "CallSmsGuardianService";
	private InnerSmsReceiver receiver;
	private BlackNumberDAO dao;
	private TelephonyManager telephonyManager;
	private MyPhoneListener listener;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class InnerSmsReceiver extends BroadcastReceiver{

		/**
		 * SMS received
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			//obtaining the SMS
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for (Object object : objs) {
				SmsMessage message = SmsMessage.createFromPdu((byte[])object);
				String sender = message.getOriginatingAddress(); //get coming number
				String mode = dao.searchBlackNumberMode(sender);
				if ("2".equals(mode) || "3".equals(mode)) {
					abortBroadcast(); //USELESS OVER 4.4
					Log.i(TAG, "SMS BLOCKADE!!!!!!!!!!!!");
				}
				
				//Demo code: auto-blockade 
				//(separate words + search through the database)
				String body = message.getMessageBody();
				if (body.contains("money")||body.contains("drug")||body.contains("sex")) {
					Log.i(TAG, "Fraud SMS");
					abortBroadcast();
				}
				
			}
		}
	}
	
	@Override
	public void onCreate() {
		dao = new BlackNumberDAO(CallSmsGuardianService.this);
		
		// using listener to monitor phone call
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyPhoneListener();
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		
		//-------------------
		
		//using code to define a receiver (to monitor SMS)
		receiver = new InnerSmsReceiver();
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(receiver, filter);
		//-------------------
		Log.i(TAG, "SMS BLOCKADE CREATE!!!!!!!!!!!!");
		
		super.onCreate();
		
	}
	
	/*
	 * need declaration first, monitoring call state
	 */
	private class MyPhoneListener extends PhoneStateListener{

		//source -> override/implement method
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:   //telephone ring(coming)
				String mode = dao.searchBlackNumberMode(incomingNumber);
				if ("1".equals(mode) || "3".equals(mode)) {
					
					//first, observe the time when the database has changed
					Uri uri = Uri.parse("content://call_log/calls");
					getContentResolver().registerContentObserver(uri, true, new CallLogObserver( new Handler(),incomingNumber));
					
					//third, ending phone call
					Log.i(TAG, "Hang out the call");
					endCall();
				}
				break;

			default:
				break;
			}
		}
		
	}
	
	private class CallLogObserver extends ContentObserver{
		private String incomingNumber;
		public CallLogObserver(Handler handler, String incomingNumber) {
			super(handler);
			this.incomingNumber = incomingNumber;
		}
		
		@Override
		public void onChange(boolean selfChange) {
			Log.i(TAG, "call database changed");
			//second,
			//once the content has changed, stop monitoring(Observing)
			getContentResolver().unregisterContentObserver(this);
			
			//delete the records
			deleteCallLog(incomingNumber);
			
			super.onChange(selfChange);
			
		}
		
	}
	
	
	@Override
	public void onDestroy() {
		//cancel SMS monitor
		unregisterReceiver(receiver);
		receiver = null;
		
		//cancel phone monitor
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
		
		super.onDestroy();
	}

	/**
	 * using content provider to delete the records
	 * (content provider is used to obtain database access from other process)
	 * @param incomingNumber
	 */
	public void deleteCallLog(String incomingNumber) {
		ContentResolver resolver = getContentResolver();
		Uri uri = Uri.parse("content://call_log/calls");
		resolver.delete(uri, "number = ?", new String[]{incomingNumber});
	}

	/**
	 * shut down the call, IBinder would run on ANOTHER thread
	 * need CALL_PHONE user-permission
	 */
	public void endCall() {
		try {
			//using reflect to get the ServiceManager, using ServiceManager to get IBinder
			//using IBinder to directly control phone state
			Class clazz = CallSmsGuardianService.class.getClassLoader().loadClass("android.os.ServiceManager");
			Method method = clazz.getDeclaredMethod("getService", String.class);
			IBinder iBinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE); //null means the method is static
			
			ITelephony.Stub.asInterface(iBinder).endCall(); //end calling
			
			Toast.makeText(CallSmsGuardianService.this, "blacklist number", 1).show();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
