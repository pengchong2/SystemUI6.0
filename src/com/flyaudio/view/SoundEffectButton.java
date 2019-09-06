package com.flyaudio.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.flyaudio.entities.FlyNavigator;
import com.flyaudio.utils.Flog;

import java.util.ArrayList;
import java.util.List;

import cn.flyaudio.sdk.FlySDKManager;

public class SoundEffectButton extends PowerButton {


    public SoundEffectButton() { mType = BUTTON_SOUNDEFFECT; }

    @Override
    protected void updateState(Context context) {
    	//setUI("systemui_settings_off","settings_button","power_button_text_color_d",false);
        setUI("flyaudio_systemui_audio_bg_u","flyaudio_audio_tv","flyaudio_systemui_button_textcolor_u",false);
        mState = STATE_DISABLED;
    }  
    @Override
    protected void setupButton(View view) {
        // TODO Auto-generated method stub
        view.setId(22);
        super.setupButton(view);
    }
    
    
    @Override
    protected void toggleState(Context context) {  
        Flog.d("SystemUI-SettingsButton","toggleState");
        context.sendBroadcast(new Intent().setAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        context.sendBroadcast(new Intent().setAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        int pageID = 257;
        byte[] seekbar_increase = { 0x10, (byte) 0x82,(byte) ((pageID & 0xff00) >> 8),(byte) (pageID & 0xff)};
        int id = 0xFC0001;
        Log.d("wfllog", "(byte)(pageID&0xff) : "
                + (byte) (pageID & 0xff)
                + "(byte)((pageID&0xff00)>>8) : "
                + (byte) ((pageID & 0xff00) >> 8));
        FlyNavigator.getInstance(context).new AdapterCenter().setZControl(id, (byte) 0xFE,
                seekbar_increase);
    }

    @Override
    protected boolean handleLongClick(Context context) {
       // context.sendBroadcast(new Intent().setAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
      //  context.startActivity(new Intent(Settings.ACTION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return true;
    }
}
