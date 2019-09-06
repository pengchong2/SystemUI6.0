
package com.flyaudio.view;

import android.content.Context;
import android.view.View;
import android.util.Log;
import com.flyaudio.entities.FlyNavigator;

import cn.flyaudio.sdk.manager.FlySystemManager;

public class OffScreenButton extends PowerButton {

    private FlyNavigator mFlyNavigator;
    public OffScreenButton() { mType = BUTTON_OFFSCREEN; }
    @Override
    protected void setupButton(View view) {
        view.setId(10);
        super.setupButton(view);
    }

    @Override
    protected void updateState(Context context) {
        //setUI("systemui_quick_off","offscreen_button","power_button_text_color",false);
        setUI("flyaudio_systemui_bottom_screenoff_bg_u","flyaudio_closescreen_tv","flyaudio_systemui_button_textcolor_u",false);
        mState = STATE_ENABLED;
    }

    @Override
    protected void toggleState(Context context) {
      //  mFlyNavigator = FlyNavigator.getInstance(context);
      //  mFlyNavigator.new AdapterCenter().setControlScreen(0);
        Log.d("closescreen","closescreen");
        FlySystemManager.getInstance().closeScreen();


//        Intent intent = new Intent("cn.flyaudio.systemui.changebrightness");
//        intent.putExtra("value", 0);
//        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
//        context.sendBroadcast(intent);		
    }

    @Override
    protected boolean handleLongClick(Context context) {
        return false;
    }

}

