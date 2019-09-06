
package com.flyaudio.view;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.content.IntentFilter;
import android.location.LocationManager;

public class LocationButton extends PowerButton {

    public void LocationButton() {
        mType = BUTTON_LOCATION;
    }

    /**
     * 改变图标
     */
    @Override
    protected void updateState(Context context) {
        Log.d("LocationButton", " updateState getDataState(context):" + getDataState(context));
        if (getDataState(context)) {
            setUI("systemui_gps_on", "location_button", "power_button_text_color",true);
            mState = STATE_ENABLED;
        } else {
            setUI("systemui_gps_off", "location_button", "power_button_text_color_d", false);
            mState = STATE_DISABLED;
        }
    }

    /**
     * 改变状态
     */
    @Override
    protected void toggleState(Context context) {
        boolean mLocationState = getDataState(context);
        final ContentResolver cr = context.getContentResolver();
        if (mLocationState) {
            Settings.Secure.putInt(cr, Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);

        } else {
            Settings.Secure.putInt(cr, Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
        }
    }

    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        return filter;
    }

    /**
     * 长按事件
     */
    @Override
    protected boolean handleLongClick(Context context) {
        return false;
    }

    /**
     * 得到目前gps的状态
     */
    private boolean getDataState(Context context) {
        // true表示的是开
        ContentResolver cr = context.getContentResolver();
        return Settings.Secure.getInt(cr, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) != 0;

    }
}
