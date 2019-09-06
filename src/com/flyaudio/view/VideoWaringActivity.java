package com.flyaudio.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.flyaudio.utils.Flog;
import com.flyaudio.utils.SkinResource;

public class VideoWaringActivity extends Activity {
	protected static final String TAG = "VideoWaringActivity";
	private final String FLY_CAR_ACTION = "com.flyaudio.killVideoProcess";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Flog.d(TAG, "onCreate");
		View view = SkinResource.getSkinLayoutViewByName("video_waring");
		setContentView(view);

		FrameLayout fl_video_waring_bg = (FrameLayout) view.findViewById(
				SkinResource.getSkinResourceId("fl_video_waring_bg", "id"));

		Button button_Ok = (Button) view.findViewById(
				SkinResource.getSkinResourceId("video_okLabel", "id"));

		fl_video_waring_bg.setBackground(
				SkinResource.getSkinDrawableByName("set_delete_tip"));

		button_Ok.setBackground(
				SkinResource.getSkinDrawableByName("bt_tip_ok_ibt"));

		button_Ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
//				Intent i = new Intent(VideoWaringActivity.this, RecentsActivity.class);
//				startActivity(i);
				finish();
			}
		});

		registerReceiver();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Flog.d(TAG, "onStop");
	}
	
	
	@Override
	protected void onDestroy() {
		unregisterReceiver();
		super.onDestroy();
		Flog.d(TAG, "onDestory");
	}
	
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(FLY_CAR_ACTION);
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	private void unregisterReceiver(){
		try {
			unregisterReceiver(mBroadcastReceiver);
		} catch (Exception e) {
			Flog.d(TAG, "unregisterReceiver Exception = " + e.toString());
		}
	}
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(FLY_CAR_ACTION)) {
				String fly_car_status = intent.getStringExtra("fly_brake_car");
				Flog.d(TAG," ==fly_brake_car=="+ fly_car_status);
				if(fly_car_status != null && fly_car_status.equals("parked")){
					Flog.d(TAG, "onReceive parked,finishActivity!");
					finish();
				}
			}
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Flog.d(TAG, "onKeyDown keyCode ==" + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			Flog.d(TAG, "onKeyDown back or home, finish activity!");
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}

