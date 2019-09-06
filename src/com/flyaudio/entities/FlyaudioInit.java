package com.flyaudio.entities;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.systemui.SystemUIApplication;
import com.flyaudio.utils.Flog;
import com.flyaudio.utils.SkinResource;
import com.android.keyguard.KeyguardUpdateMonitor;

/**
 *
 * Created by yaoyuqing on 17-6-23.
 */

public class FlyaudioInit {

    private static FlyaudioInit flyaudioInit=null;
    public static String isColorSkin;
    private static final String skinPackageName="com.flyaudio.flyaudioskinproj";/*"com.flyaudio.flyaudioskinproj";*/
    private Context context;
    private KeyguardUpdateMonitor mUpdateMonitor;


    public static synchronized FlyaudioInit getInstance(){
        if (flyaudioInit==null) {
            flyaudioInit = new FlyaudioInit(SystemUIApplication.applicationContext);
        }
        return flyaudioInit;
    }

    public FlyaudioInit(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        // register logcat recevier
        mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        if (!shouldWaitForProvisioning()) {
            Log.d("agreepage", "init: startService(intent);");
            Intent intent = new Intent();
            intent.setAction("com.flyaudio.welcome.agreepage.start.service");
            intent.setPackage("com.flyaudio.welcome.agreepage");
            context.startService(intent);
        }
        Flog flog = new Flog();
        flog.registerDebugBrocastReceiver(context, "cn.flyaudio.systemui");

        try {
            if ("isSystemUIColorSkin".equals(SkinResource.getSkinStringByName("isSystemUIColorSkin"))) {
                isColorSkin = "isSystemUIColorSkin";
            }else {
                isColorSkin = "noSystemUIColorSkin";
            }
        }catch (Exception e){
            isColorSkin = null;
        }
    }

    private boolean shouldWaitForProvisioning() {
        Log.d("agreepage", "-agree page- the provision is:"+!mUpdateMonitor.isDeviceProvisioned());
        return !mUpdateMonitor.isDeviceProvisioned();
    }
}
