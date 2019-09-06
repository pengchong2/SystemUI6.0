package com.flyaudio.view;

import com.android.systemui.R;
import com.flyaudio.utils.Flog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.app.ActivityManagerNative;

import java.util.ArrayList;
import java.util.List;

public class SettingsButton extends PowerButton {
  
    private static final List<Uri> OBSERVED_URIS = new ArrayList<Uri>();
    static {
        OBSERVED_URIS.add(Settings.Secure.getUriFor(Settings.Secure.LOCATION_PROVIDERS_ALLOWED));
    }

    public SettingsButton() { mType = BUTTON_SETTINGS; }

    @Override
    protected void updateState(Context context) {
    	setUI("systemui_settings_off","settings_button","power_button_text_color_d",false);
            mState = STATE_DISABLED;
    }  
    @Override
    protected void setupButton(View view) {
        // TODO Auto-generated method stub
        view.setId(7);
        super.setupButton(view);
    }
    
    
    @Override
    protected void toggleState(Context context) {  
        Flog.d("SystemUI-SettingsButton","toggleState");
        context.sendBroadcast(new Intent().setAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        context.startActivity(new Intent(Settings.ACTION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected boolean handleLongClick(Context context) {
        context.sendBroadcast(new Intent().setAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        context.startActivity(new Intent(Settings.ACTION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return true;
    }
}
