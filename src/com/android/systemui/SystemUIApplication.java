/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui;

import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.util.Log;

import com.flyaudio.entities.FlyaudioInit;
import com.flyaudio.entities.FlyaudioServices;
import com.flyaudio.utils.FlyaudioServicesXMLParser;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.flyaudio.utils.GetWeatherForCityNameUtil;
import com.flyaudio.utils.SkinResource;
import com.flyaudio.view.FlyaudioSystemUI;

/**
 * Application class for SystemUI.
 */
public class SystemUIApplication extends Application {

    private static final String TAG = "SystemUIService";
    private static final boolean DEBUG = false;
    public static Context applicationContext;

    /**
     * The classes of the stuff to start.
     */
    private final Class<?>[] SERVICES = new Class[] {
            com.android.systemui.tuner.TunerService.class,
            com.android.systemui.keyguard.KeyguardViewMediator.class,
            com.android.systemui.recents.Recents.class,
            com.android.systemui.volume.VolumeUI.class,
            com.android.systemui.statusbar.SystemBars.class,
            com.android.systemui.usb.StorageNotification.class,
            com.android.systemui.power.PowerUI.class,
            com.android.systemui.media.RingtonePlayer.class,
    };

    /**
     * Hold a reference on the stuff we start.
     */
    private final SystemUI[] mServices = new SystemUI[SERVICES.length];
    private boolean mServicesStarted;
    private boolean mBootCompleted;
    private final Map<Class<?>, Object> mComponents = new HashMap<Class<?>, Object>();
    public static List<FlyaudioServices> flyaudioServices;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext=getApplicationContext();
        // Set the application theme that is inherited by all services. Note that setting the
        // application theme in the manifest does only work for activities. Keep this in sync with
        // the theme set there.
        setTheme(R.style.systemui_theme);

        /*if (!shouldWaitForProvisioning()) {
            Intent intent = new Intent();
            intent.setAction("com.flyaudio.welcome.agreepage.start.service");
            intent.setPackage("com.flyaudio.welcome.agreepage");
            startService(intent);
        }*/
        SkinResource.initSkinResource(applicationContext,"com.flyaudio.flyaudioskinproj");
        try {
            flyaudioServices = FlyaudioServicesXMLParser.getFlyaudioServices(
                    new FileInputStream("/flysystem/flytheme/config/flyaudioserviceslist.xml"));
            Log.d("SystemUI", "onCreate: flyaudioServices:"+ flyaudioServices.size());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("SystemUI", "onCreate: not found :flyaudioserviceslist.xml");
        }


        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mBootCompleted) return;

                if (DEBUG) Log.v(TAG, "BOOT_COMPLETED received");
                unregisterReceiver(this);
                mBootCompleted = true;
                if (mServicesStarted) {
                    final int N = mServices.length;
                    for (int i = 0; i < N; i++) {
                        mServices[i].onBootCompleted();
                    }
                }
            }
        }, filter);

        //设置系统属性，初始下拉是关闭的，可以显示类似音量的弹窗
        SystemProperties.set("fly.persist.statusbar.show", "0");
        String processName = getProcessName(this);
        if (processName.equals("com.android.systemui")) {
            Log.d("systemui", "onCreate: FlyaudioInit.getInstance() ");
            FlyaudioInit.getInstance();
        }else {
            SkinResource.initSkinResource(this,"com.flyaudio.flyaudioskinproj");
        }
       //初始化获取天气的接口
        GetWeatherForCityNameUtil.GetInstance().init(this);

        FlyaudioSystemUI.getInstance().initContext(this);
        Log.d("version","1903191502");
    }



    public void startFlyaudioServices(){
        if (flyaudioServices!=null&&flyaudioServices.size()>0) {
            Log.d("qwer", "startServicesIfNeeded: 启动flyaudio server");
            for (FlyaudioServices flyaudioService : flyaudioServices) {
                if (flyaudioService.getClass_name() != null &&
                        !"".equals(flyaudioService.getClass_name())) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(flyaudioService.getPackage_name(),
                            flyaudioService.getClass_name()));
                    startService(intent);
                }else
                if (!"".equals(flyaudioService.getAciton_name())
                        &&flyaudioService.getAciton_name()!=null) {
                    Intent intent = new Intent();
                    intent.setAction(flyaudioService.getAciton_name());
                    intent.setPackage(flyaudioService.getPackage_name());
                    startService(intent);
                }
            }
        }
    }

    /**
     * Makes sure that all the SystemUI services are running. If they are already running, this is a
     * no-op. This is needed to conditinally start all the services, as we only need to have it in
     * the main process.
     *
     * <p>This method must only be called from the main thread.</p>
     */
    public void startServicesIfNeeded() {
        if (mServicesStarted) {
            return;
        }

        if (!mBootCompleted) {
            // check to see if maybe it was already completed long before we began
            // see ActivityManagerService.finishBooting()
            if ("1".equals(SystemProperties.get("sys.boot_completed"))) {
                mBootCompleted = true;
                if (DEBUG) Log.v(TAG, "BOOT_COMPLETED was already sent");
            }
        }

        Log.v(TAG, "Starting SystemUI services.");
        final int N = SERVICES.length;
        for (int i=0; i<N; i++) {
            Class<?> cl = SERVICES[i];
            if (DEBUG) Log.d(TAG, "loading: " + cl);
            try {
                mServices[i] = (SystemUI)cl.newInstance();
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            }
            mServices[i].mContext = this;
            mServices[i].mComponents = mComponents;
            if (DEBUG) Log.d(TAG, "running: " + mServices[i]);
            mServices[i].start();

            if (mBootCompleted) {
                mServices[i].onBootCompleted();
            }
        }
        mServicesStarted = true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mServicesStarted) {
            int len = mServices.length;
            for (int i = 0; i < len; i++) {
                mServices[i].onConfigurationChanged(newConfig);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getComponent(Class<T> interfaceType) {
        return (T) mComponents.get(interfaceType);
    }

    public SystemUI[] getServices() {
        return mServices;
    }

    private String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }
}
