package com.flyaudio.entities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.android.systemui.recents.model.Task;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.flyaudio.utils.AppBlackListXMLParser;
import com.flyaudio.utils.Flog;
import com.flyaudio.utils.FlyStorageManager;
import com.flyaudio.utils.SkinResource;
import com.flyaudio.view.KillProcessService;
import com.flyaudio.view.VideoWaringActivity;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.android.systemui.R;

import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

/**
 *
 * Created by yaoyuqing on 17-6-23.
 */

public class FlyAudioProcess {

    private static FlyAudioProcess flyaudioprocess=null;
    private String TAG = "FlyAudioProcess";
    private String fly_car_status;
    public static Set<String> mLists = new HashSet<String>();
    private List<String> mList = new ArrayList<String>();
    private AppBlackListXMLParser mAppBlackListXMLParser;
    public boolean isNeedNotification = false;
    public static Set<String> mRecentLists = new HashSet<String>();
    private ImageView btIcon;
    public static String keyScreenOFF = "KEY_SCREEN_OFF";
    public static String PowerBundle = "POWERBUNDLE";
    public static String KeyBootState = "KEYBOOTSTATE";
    private boolean isLSRALE = false;//是否是以色列版本

    public static synchronized FlyAudioProcess getInstance(){
        if (flyaudioprocess==null) {
            flyaudioprocess = new FlyAudioProcess();
        }
        return flyaudioprocess;
    }

    public static void showWaringDialog(Context context) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setComponent(new ComponentName(context, VideoWaringActivity.class));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(i);
    }

    public File FlyScreenshotDir(Context context, String SCREENSHOTS_DIR_NAME){
        FlyStorageManager.init(context);
        if (!TextUtils.isEmpty(FlyStorageManager.getMountedUsbVolumePath())) {
            return new File(FlyStorageManager.getMountedUsbVolumePath(), SCREENSHOTS_DIR_NAME);
        } else {
            return  null;/*new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), SCREENSHOTS_DIR_NAME);*/
        }
    }

    public static boolean displayShowWaringDialog(Task task ,Context context){
        Log.d("yaoyuqing", "displayShowWaringDialog: ");
        String strBreakCarStatus = SystemProperties.get(
                "persist.fly.car.status", "");
        if (strBreakCarStatus != null) {
            if ("driving".equals(strBreakCarStatus)) {
                if (!KillProcessService.isInAppWhiteList(task.key.baseIntent
                        .getComponent().getPackageName())) {
                    Log.d("yaoyuqing", "displayShowWaringDialog: open");
                    showWaringDialog(context);
                    return true;
                }
            }
        }
        return false;
    }

    public Set<String> getWhiteAPP(List<String> paths) {

        File mFile = new File(paths.get(0));
        File mFile2 = new File(paths.get(1));
        Log.d("yaoyuqing", "getWhiteAPP: paths.get(0):"+paths.get(0));
        Log.d("yaoyuqing", "getWhiteAPP: paths.get(1):"+paths.get(1));
        List<String> list = new ArrayList<String>();
        try {
            if (mFile.exists()) {
                list = mAppBlackListXMLParser.getBlackList(new FileInputStream(
                        mFile));
                Log.d("yaoyuqing", "getWhiteAPP: 1");
                mLists.addAll(list);

            }
            if (mFile2.exists()) {
                list = mAppBlackListXMLParser.getBlackList(new FileInputStream(
                        mFile2));
                Log.d("yaoyuqing", "getWhiteAPP: 2");
                mLists.addAll(list);
            }

            if (isLSRALE){
                Log.d("yaoyuqing", "getWhiteAPP: paths.get(2):"+paths.get(2));
                File mFile3 = new File(paths.get(2));
                if (mFile3.exists()) {
                    list = mAppBlackListXMLParser.getBlackList(new FileInputStream(
                            mFile3));
                    Log.d("yaoyuqing", "getWhiteAPP: is lsrale");
                    mLists.addAll(list);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String s : mList) {
            Log.d("yaoyuqing", "getWhiteAPP: pa:"+s);

        }

        return mLists;
    }

    public Set<String> getRecentsProtectedAPP(String path) {
        File mFile = new File(path);
        List<String> list = new ArrayList<String>();
        try {
            if (mFile.exists()) {
                list = mAppBlackListXMLParser.getBlackList(new FileInputStream(mFile));
                mRecentLists.addAll(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mRecentLists;
    }

    public List<String> addAppPageName(String path, String pathString) {
        List<String> mList = new ArrayList<String>();
        String brefore_path = SystemProperties.get("persist.fly.car.select",
                "default");
        path = path + brefore_path + "/uiconfig/appwhitelist.xml";
        mList.add(path);
        mList.add(pathString);
        return mList;
    }

    	//add by hlc
            	private List<String> addAppPageName(String path, String systemPath,String thirdPartyPath) {
        		List<String> mList = new ArrayList<String>();
        		String brefore_path = SystemProperties.get("persist.fly.car.select",
                				"default");
        		path = path + brefore_path + "/uiconfig/wl.tmp";
        		mList.add(path);
        		mList.add(systemPath);
        		mList.add(thirdPartyPath);
        		return mList;
        	}

        	private void isLSRALE(){
                if (SkinResource.getSkinBooleanFromSkin("is_isr",false)){
            			isLSRALE = true;
            		}else {
            			isLSRALE = false;
            		}
        	}



    public void flyBrakeCar(Intent intent,Context context){
        fly_car_status = intent.getStringExtra("fly_brake_car");

        if (fly_car_status != null && fly_car_status.equals("driving")) {
            Intent killProcessIntent = new Intent(context,
                    KillProcessService.class);
            killProcessIntent
                    .putExtra("fly_car_status", fly_car_status);
            context.startService(killProcessIntent);
        }
    }

    /*获取最近列表白名单*/
    public void initFlyCarStatus(Context mContext){

        // flycarstatus
        mAppBlackListXMLParser = new AppBlackListXMLParser();
       /* mList = addAppPageName(
                SkinResource.getSkinStringByName("default_path"),
                SkinResource.getSkinStringByName("add_path"));*/
        isLSRALE();//是否是以色列版本
        if (isLSRALE){
            mList = addAppPageName(
                    SkinResource.getSkinStringByName("default_path"),
                    SkinResource.getSkinStringByName("lsrale_flysystem_app_whitelist"),
                    SkinResource.getSkinStringByName("lsrale_thirdparty_app_whitelist"));
        }else {
            mList = addAppPageName(
                    SkinResource.getSkinStringByName("string.default_path"),
                    SkinResource.getSkinStringByName("add_path"));
        }
        mLists = getWhiteAPP(mList);
        mRecentLists = getRecentsProtectedAPP(
                SkinResource.getSkinStringByName("recents_protected_path"));
        isNeedNotification = "gone".equals(SkinResource.getSkinStringByName("isNeedNotification"));
        // end
    }

    /*初始化状态栏蓝牙图标*/
    public void setStatusBarBtIcon(StatusBarWindowView mStatusBarWindow){
        btIcon = (ImageView) mStatusBarWindow
                .findViewById(SkinResource.getSkinResourceId("btcnonected","id"));
    }
    /*设置BT状态背景颜色*/
    public void refreshFlyaudioUI(Intent intent) {

        if (btIcon==null) return;

        switch (intent.getIntExtra("btstate", 1000)) {
            case 0:
                btIcon.setBackground(
                        SkinResource.
                                getSkinDrawableByName(
                                        "stat_flyaudio_sys_data_bluetooth"));
                break;
            case 1:
                btIcon.setBackground(
                        SkinResource.
                                getSkinDrawableByName(
                                        "stat_flyaudio_sys_data_bluetooth_connected"));
                break;
            default:
                break;
        }
    }

    public void screenOffCloseNoti(Intent intent,Context mContext){
        String bootState = null;
        Bundle bundle = intent.getBundleExtra(PowerBundle);
        bootState = bundle.getString(KeyBootState);
        if (bootState.equals(keyScreenOFF) && mContext != null) {
            mContext.sendBroadcast(new Intent().setAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
    }

    public boolean baiduOSDisPlayNotification(NotificationData.Entry entry) {
        if ("com.baidu.che.codriver".equals(entry.notification
                .getPackageName())) {
            return true;
        }
        if ("com.baidu.car.radio".equals(entry.notification
                .getPackageName())) {
            return true;
        }
        if ("com.baidu.che.codriverlauncher".equals(entry.notification
                .getPackageName())) {
            return true;
        }
        if ("com.android.server.telecom".equals(entry.notification
                .getPackageName())) {
            return true;
        }
        return false;
    }


}
