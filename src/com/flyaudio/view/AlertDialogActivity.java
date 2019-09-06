package com.flyaudio.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.android.systemui.R;
import com.flyaudio.utils.SkinResource;


public class AlertDialogActivity extends Activity{

	 Button button_Cancel;
	 Button button_Ok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View reset = SkinResource.getSkinLayoutViewByName("reset");
        setContentView(reset);
        FrameLayout framelayout_reset = (FrameLayout) reset.findViewById(
                SkinResource.getSkinResourceId("framelayout_reset","id"));
        framelayout_reset.setBackground(
                SkinResource.getSkinDrawableByName("tip_bg"));
        button_Ok=(Button)reset.findViewById(
				 SkinResource.getSkinResourceId("okLabel", "id"));
        button_Ok.setBackground(
                SkinResource.getSkinDrawableByName("bt_tip_ok_ibt"));
        button_Ok.setTextColor(
                SkinResource.getSkinResourceId("button_color","color"));
        
       
        button_Cancel = (Button) findViewById(
				 SkinResource.getSkinResourceId("cancelLabel", "id"));
        button_Cancel.setBackground(
                SkinResource.getSkinDrawableByName("bt_tip_ok_ibt"));
        button_Cancel.setTextColor(
                SkinResource.getSkinResourceId("button_color","color"));
       
        button_Ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                 Intent i = new Intent("com.android.flyaudioui.LOCALE_CHANGED");
                 sendBroadcast(i);//重启机器的广播
                 finish();  
            }
        });
        
        button_Cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                finish();  
            }
        });
    }

}
