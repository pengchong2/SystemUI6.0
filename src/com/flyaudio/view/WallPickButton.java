package com.flyaudio.view;

import android.content.Context;
import android.content.Intent;

import android.view.View;

import com.flyaudio.entities.FlyNavigator;

public class WallPickButton extends PowerButton {
    private FlyNavigator mFlyNavigator;

    public WallPickButton() {
        mType = BUTTON_WALLPICK;
    }

    @Override
    protected void setupButton(View view) {
        view.setId(20);
        super.setupButton(view);
    }

    @Override
    protected void updateState(Context context) {
        setUI("systemui_wallpick_off", "wallpick_button", "power_button_text_color_d", false);
	mState = STATE_DISABLED;
    }

    @Override
    protected void toggleState(Context context) {
		context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Intent i = new Intent("action.systemui.request");
        i.putExtra("SYSTEMUI_REQUEST","wallpaper_set");
        context.sendBroadcast(i);
    }

    @Override
    protected boolean handleLongClick(Context context) {
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Intent i = new Intent("action.systemui.request");
        i.putExtra("SYSTEMUI_REQUEST","wallpaper_set");
        context.sendBroadcast(i);
        
        return true;
    }

}
