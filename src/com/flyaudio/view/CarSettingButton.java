package com.flyaudio.view;

import android.content.Context;
import android.util.Log;
import android.content.Intent;
import com.flyaudio.entities.FlyConstant;
import com.flyaudio.entities.FlyNavigator;
import android.view.View;

public class CarSettingButton extends PowerButton {
    private FlyNavigator mFlyNavigator;

    public CarSettingButton() {
        mType = BUTTON_CARSETTING;
    }

    @Override
    protected void setupButton(View view) {
        view.setId(11);
        super.setupButton(view);
    }

    @Override
    protected void updateState(Context context) {
        setUI("systemui_carsetting_off", "carsetting_button", "power_button_text_color_d", false);
	mState = STATE_DISABLED;
    }

    @Override
    protected void toggleState(Context context) {
        mFlyNavigator = FlyNavigator.getInstance(context);
        Log.d("resbt", " sendFlyAppManger(126, FlyConstant.ON) ");
	context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        mFlyNavigator.new AdapterCenter().sendFlyAppManger(120, FlyConstant.ON);	
    }

    @Override
    protected boolean handleLongClick(Context context) {
        mFlyNavigator = FlyNavigator.getInstance(context);
        Log.d("resbt", " sendFlyAppManger(126, FlyConstant.ON) ");
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        mFlyNavigator.new AdapterCenter().sendFlyAppManger(120, FlyConstant.ON);
        return true;
    }

}
