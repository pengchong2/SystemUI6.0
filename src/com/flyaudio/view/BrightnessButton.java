
package com.flyaudio.view;

//import com.android.server.power.PowerManagerService;
import com.android.systemui.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.flyaudio.sdk.listener.SystemListener;
import cn.flyaudio.sdk.manager.FlySystemManager;

public class BrightnessButton extends PowerButton {

    private static final String TAG = "BrightnessButton";


    // Auto-backlight level
    private static final int CLOSE_BACKLIGHT = 0; // 关屏
    private static final int LOW_BACKLIGHT = 1;// 暗淡
    private static final int MID_BACKLIGHT = 2;// 中等亮度
    private static final int HIGH_BACKLIGHT = 3;// 高亮

    private static final int[] BACKLIGHTS = new int[] {
        CLOSE_BACKLIGHT, LOW_BACKLIGHT, MID_BACKLIGHT, HIGH_BACKLIGHT
    };

    // 屏幕的背光亮度　，在0到255之间
    private static final Uri BRIGHTNESS_URI = Settings.System
            .getUriFor(Settings.System.SCREEN_BRIGHTNESS);

    // 控制是否启用自动亮度模式。
    private static final Uri BRIGHTNESS_MODE_URI = Settings.System
            .getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE);

    private static final List<Uri> OBSERVED_URIS = new ArrayList<Uri>();

    static {
        OBSERVED_URIS.add(BRIGHTNESS_URI);
        OBSERVED_URIS.add(BRIGHTNESS_MODE_URI);
    }

    private boolean mAutoBrightnessSupported = false;

    private boolean mAutoBrightness = false;

    private int mCurrentBrightness;

    private int mCurrentBacklightIndex = 0;

    private int[] mBacklightValues = new int[] {
            0, 1, 2, 3
    };

    public BrightnessButton() {
        mType = BUTTON_BRIGHTNESS; // 切换亮度
    }

    @Override
    protected void setupButton(View view) {
        super.setupButton(view);
        if (mView != null) {
            Context context = mView.getContext();
            mAutoBrightnessSupported = context.getResources().getBoolean(
                    com.android.internal.R.bool.config_automatic_brightness_available);
            // updateSettings(context.getContentResolver());
        }

        FlySystemManager.getInstance().setSystemListener(new SystemListener() {
            @Override
            public void onVolumeStatus(int i, int i1, int i2) {
                Log.d("bright","onVolumeStatus i = "+i+" i1 = "+i1+" i2 = "+i2);
            }

            @Override
            public void onMediaVolumeStatus(int i) {

            }

            @Override
            public void onPhoneVolumeStatus(int i) {

            }

            @Override
            public void onScreenLightLevelStatus(int i) {

                Log.d("test_systemui", "onScreenLightLevelStatus:                  i:"+i);
                Log.d("test_systemui", "onScreenLightLevelStatus: mCurrentBrightness:"+mCurrentBrightness);

                //updateBrightnessUI(mCurrentBrightness);
                int i1 = i + 1;
                if (i1==mCurrentBrightness)return;
                if (mCurrentBrightness==0)return;
                updateBrightnessUI(i1);


            }

            @Override
            public void onScreenBrightness(int i) {

            }

            @Override
            public void onCarRadar(int i, int i1, int i2, int i3, int i4) {

            }

            @Override
            public void onDefaultNaviChanged() {

            }

            @Override
            public void onVolumeChannel(int i) {

            }

            @Override
            public void onDayNightMode(int i) {

            }

            @Override
            public void onCurrentPageChanged(int i) {

            }

            @Override
            public void onScreenBrightnessAutoStatus(int i) {

            }
        });
    }

    @Override
    protected void updateState(Context context) {
       
        updateBrightnessUI(mCurrentBrightness);

    }

    
    
    // 切换状态，控制屏幕亮度
    int brightness = 0;

    @Override
    protected void toggleState(Context context) {
        Intent intent = new Intent("cn.flyaudio.systemui.changebrightness");
        mCurrentBacklightIndex++;
        ContentResolver resolver = context.getContentResolver();
        if (mCurrentBacklightIndex > mBacklightValues.length - 1) {
            mCurrentBacklightIndex = 0;
        }
        brightness = BACKLIGHTS[mCurrentBacklightIndex];
        if (brightness == CLOSE_BACKLIGHT) {
            intent.putExtra("value", 0);
            context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        } else {
            if (brightness == LOW_BACKLIGHT) {
                intent.putExtra("value", 1);

            } else if (brightness == MID_BACKLIGHT) {
                intent.putExtra("value", 2);

            } else if (brightness == HIGH_BACKLIGHT) {
                intent.putExtra("value", 3);
            }
        }
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        context.sendBroadcast(intent);
        Log.d("test_systemui", "  bright whitch level:" + brightness + "     mCurrentBacklightIndex :" + mCurrentBacklightIndex);

    }

    // 长按进入setting的显示设置页
    @Override
    protected boolean handleLongClick(Context context) {
        /*
         * context.sendBroadcast(new
         * Intent().setAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)); Intent
         * intent = new Intent("android.settings.DISPLAY_SETTINGS");
         * intent.addCategory(Intent.CATEGORY_DEFAULT);
         * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         * context.startActivity(intent);
         */
        return true;
    }

    @Override
    protected List<Uri> getObservedUris() {
        return OBSERVED_URIS;
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        int i = intent.getIntExtra("value", 0);
        // updateState(context);
        mCurrentBrightness = i;

        updateBrightnessUI(mCurrentBrightness);

        Log.d("test_systemui"," bright onreceive updateBrightnessUI: " +mCurrentBrightness);
        
    }

    @Override
    protected IntentFilter getBroadcastIntentFilter() {
        // TODO Auto-generated method stub
        IntentFilter filter = new IntentFilter();
        filter.addAction("cn.flyaudio.systemui.changebrightness");  
	    filter.addAction("com.android.systemui.updatebrightnessstate");
        return filter;
    }
    
   private void updateBrightnessUI(int i){
       switch (i)
       {
           case 0:
              setUI("systemui_stat_brightness_hight","bright_on_button","power_button_text_color",true);
               mCurrentBacklightIndex = 0;
               mState = STATE_ENABLED;
               break;
           case 1:
              setUI("systemui_stat_brightness_low","night_bright_button","power_button_text_color",true);
               mState = STATE_ENABLED;
               mCurrentBacklightIndex = 1;
               break;
           case 2:
              setUI("systemui_stat_brightness_mid","mid_bright_button","power_button_text_color",true);
               mCurrentBacklightIndex = 2;
               mState = STATE_INTERMEDIATE;
               break;
           case 3:
              setUI("systemui_stat_brightness_hight","bright_on_button","power_button_text_color",true);
               mCurrentBacklightIndex = 3;
               mState = STATE_INTERMEDIATE;
               break;
               default:break;
       }
      
               
     
   }

}

