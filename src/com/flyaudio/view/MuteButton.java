package com.flyaudio.view;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

import com.flyaudio.utils.ConstantUtils;

import cn.flyaudio.sdk.manager.FlySystemManager;

public class MuteButton extends PowerButton {

    private static String TAG = "MuteButton";

    public void MuteButton() {
        mType = BUTTON_MUTE;
    }


    @Override
    protected void setupButton(View view) {
        super.setupButton(view);
        mState = STATE_DISABLED;
    }

    /**
     * 改变图标
     */
    @Override
    protected void updateState(Context context) {

        if (getDataState(context) == STATE_ENABLED) {
            setUI("flyaudio_systemui_mute_d", "flyaudio_mute_tv", "flyaudio_systemui_button_textcolor_d",false);
          } else if(getDataState(context) == STATE_DISABLED) {
            setUI("flyaudio_systemui_mute_u", "flyaudio_mute_tv", "flyaudio_systemui_button_textcolor_u", false);

        }
    }

    /**
     * 改变状态
     */
    @Override
    protected void toggleState(Context context) {

        int mLocationState = getDataState(context);
        if (mLocationState == STATE_ENABLED) {
            mState = STATE_DISABLED;
            FlySystemManager.getInstance().mute(false);

        } else if(mLocationState == STATE_DISABLED){
            mState = STATE_ENABLED;
            FlySystemManager.getInstance().mute(true);
        }


    }



    /**
     * 长按事件
     */
    @Override
    protected boolean handleLongClick(Context context) {
        return false;
    }


    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        IntentFilter fileter = new IntentFilter();
        fileter.addAction("com.flyaudio.volume.ismute");
        return fileter;
    }


    @Override
    protected void onReceive(Context context, Intent intent) {
        if(intent!=null&& ConstantUtils.MUTE_ACTION.equals(intent.getAction())){
            if(intent.getBooleanExtra("ismute",false)){
                Log.d(TAG,"mute true");
                setUI("flyaudio_systemui_mute_d", "flyaudio_mute_tv", "flyaudio_systemui_button_textcolor_d",false);
                updateView();
            }else{
                Log.d(TAG,"mute false");
                setUI("flyaudio_systemui_mute_u", "flyaudio_mute_tv", "flyaudio_systemui_button_textcolor_u", false);
                updateView();
            }
        }
    }

    private int getDataState(Context context) {
         return mState;
    }
}
