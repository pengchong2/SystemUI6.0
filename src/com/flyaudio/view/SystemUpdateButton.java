package com.flyaudio.view;

import android.content.Context;

import android.content.Intent;

import android.view.View;

import com.flyaudio.entities.FlyNavigator;

public class SystemUpdateButton extends PowerButton {
    private FlyNavigator mFlyNavigator;

    public SystemUpdateButton() {
        mType = BUTTON_SYSTEM_UPDATE;
    }

    @Override
    protected void setupButton(View view) {
        view.setId(21);
        super.setupButton(view);
    }

    @Override
    protected void updateState(Context context) {
        setUI("systemui_system_update_off", "system_update_button", "power_button_text_color_d", false);
	mState = STATE_DISABLED;
    }

    @Override
    protected void toggleState(Context context) {
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Intent i = new Intent("action.systemui.request");
        i.putExtra("SYSTEMUI_REQUEST","system_update");
        context.sendBroadcast(i);	
    }

    @Override
    protected boolean handleLongClick(Context context) {
        
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Intent i = new Intent("action.systemui.request");
        i.putExtra("SYSTEMUI_REQUEST","system_update");
        context.sendBroadcast(i);
        return true;
    }

}
