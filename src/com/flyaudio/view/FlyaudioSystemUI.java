package com.flyaudio.view;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import cn.flyaudio.sdk.FlySDKManager;
import cn.flyaudio.sdk.manager.FlySystemManager;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.flyaudio.objinfo.WeatherInfo;
import com.flyaudio.utils.ConstantUtils;
import com.flyaudio.utils.GetWeatherForCityNameUtil;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.flyaudio.utils.SharePreUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;


public class FlyaudioSystemUI implements View.OnClickListener ,RadioGroup.OnCheckedChangeListener {
    private static String TAG = "FlyaudioSystemUI";
    private LinearLayout mNaviLayout;
    private RadioButton mTempBtn, mNavigationBtn, mMediaBtn, mCarinforBtn, mMeBtn, mVoiceBtn;
    private LinearLayout mItemLayout;
    private RadioGroup mTempRgp, mNaviRgp, mMediaRgp, mCarinforRgp, mMeRgp;
    private Context mContext = null;
    private static int pageId;    
    private static int LocadId;
    private NetWorkStateReceiver mNetworkStateReceiver = new NetWorkStateReceiver();
    public static FlyaudioSystemUI instance = null;
    private PhoneStatusBar mPhoneStatusBar = null;
    private Handler mHandler = new Handler();
    //上次点击的时间
    private static long lastClickTime = 0;
    //时间间隔
    private static int spaceTime =400;
    private RadioGroup mRadioGrp = null;
    private int mOncheckId = 0;
    private View mView = null;
    //默认城市名
    private String mCityName = "";
    //默认所在城市区域
    private String mDistrictName = "";
    private SettingsValueChangeContentObserver mContentOb = null;
    private int mLeftindex = 0,mBottomindex = 0;


    public static FlyaudioSystemUI getInstance() {
        if (instance == null) {
            instance = new FlyaudioSystemUI();
        }
        return instance;
    }

    public void initContext(Context context) {
        if (mContext == null) {
            mContext = context;
        }
        IntentFilter filter = new IntentFilter();
        //检测开机完成的广播
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        //检测系统配置改变的广播(例如语言，角度)
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        //检测系统页面发生改变的广播
        filter.addAction(ConstantUtils.PAGE_CHANGE);
        //跳转到4S店
        filter.addAction(ConstantUtils.TO4S_ACTION);
        mContext.registerReceiver(mBroadcastRecevier, filter);

        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mNetworkStateReceiver, netFilter);
    }

    @Override
    public void onClick(View v) {
        mView = v;
        if (isFastClick()) {
           // itemChecked(mLeftindex,mBottomindex);
            mHandler.removeCallbacks(leftFastClickRunnable);
            mHandler.postDelayed(leftFastClickRunnable,1000);
            return;
        }
            Log.d(TAG, "tag = " + v.getTag());

        leftClickAction(v);
    }


    /**
     * 左侧导航栏快速点击
     */
    Runnable leftFastClickRunnable = new Runnable() {
        @Override
        public void run() {
            leftClickAction(mView);
        }
    };

    /**
     * 响应左侧按钮的点击事件
     * @param v
     */
    private void leftClickAction(View v){
        switch (v.getId()) {
            case R.id.tempbtn:
                //mPhoneStatusBar.removeNavigationBar();
                // goToPage((RadioButton) mTempRgp.getChildAt(1));
                mItemLayout.removeAllViews();
                mItemLayout.addView(mTempRgp);
                startActivity(ConstantUtils.WEATHER_PACKAGENAME, ConstantUtils.WEATHER_CLASSNAME);
                break;
            case R.id.navigationbtn:
                // mPhoneStatusBar.addNavigationBarAgain();
                mItemLayout.removeAllViews();
                mItemLayout.addView(mNaviRgp);
                gotoPageWhenBtnClick(mNaviRgp);
                break;
            case R.id.mediabtn:
                // mPhoneStatusBar.addNavigationBarAgain();
                mItemLayout.removeAllViews();
                mItemLayout.addView(mMediaRgp);
                gotoPageWhenBtnClick(mMediaRgp);
                break;
            case R.id.carinforbtn:
                //mPhoneStatusBar.addNavigationBarAgain();
                mItemLayout.removeAllViews();
                mItemLayout.addView(mCarinforRgp);
                gotoPageWhenBtnClick(mCarinforRgp);
                break;
            case R.id.mebtn:
                // mPhoneStatusBar.addNavigationBarAgain();
                mItemLayout.removeAllViews();
                mItemLayout.addView(mMeRgp);
                gotoPageWhenBtnClick(mMeRgp);
                break;
            case R.id.voicebtn:
                // mPhoneStatusBar.removeNavigationBar();
                startVoice();
                break;
            default:
                break;
        }
    }


    /**
     * 点击左侧的导航栏进入到对应的选项
     *
     * @param rg
     */
    private void gotoPageWhenBtnClick(RadioGroup rg) {
        int childCount = rg.getChildCount();
        for (int i = 0; i < childCount; i++) {
            RadioButton childAt = (RadioButton) rg.getChildAt(i);
            if (childAt.isChecked()) {
                //设置字体加粗
                childAt.getPaint().setFakeBoldText(true);
                goToPage(childAt);
                break;
            }
        }
    }

    private void goToPage(RadioButton childAt) {

        Intent intent = new Intent();
        String tag = (String) childAt.getTag();
        Log.d(TAG, "goToPage tag = " + tag);
        if (tag.startsWith("broadcast:")) {
            sendBroadcast2App(intent, tag);
        } else if (tag.startsWith("flyaudiosdk:")) {
            String[] split = tag.split(":");
            String s = split[1];
            switch (s) {
                case "radio":
                    mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    FlySystemManager.getInstance().gotoPage(FlySystemManager.RADIO_PAGE);
                    break;
                case "media":
                    mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    FlySystemManager.getInstance().gotoPage(FlySystemManager.MEDIA_PAGE);
                    break;
                case "bluetoothmusic":
                    mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    FlySystemManager.getInstance().gotoPage(FlySystemManager.BT_MUSIC_PAGE);
                    break;
                default:
                    break;
            }
        } else {
            String[] split = tag.split(";");
            String mPackage = split[0];
            String mClass = split[1];
            try {
                if (("com.android.launcher.Air").equals(mClass)) {
                    Log.d(TAG, "mClass = " + mClass);
                    //暂时屏蔽该功能
                    //  mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    // FlySystemManager.getInstance().gotoPage(FlySystemManager.AIR_PAGE);
                    return;
                } else if (("com.android.launcher.Bluetooth").equals(mClass)) {
                    Log.d(TAG, "mClass = " + mClass);
                    mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    FlySystemManager.getInstance().gotoPage(FlySystemManager.BT_PAGE);
                    return;
                } else if (("com.android.launcher.Setup").equals(mClass)) {
                    Log.d(TAG, "mClass = " + mClass);
                    mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                    FlySystemManager.getInstance().gotoPage(FlySystemManager.CAR_SETTING_PAGE);
                    return;
                }
            } catch (Exception e) {
                Log.d(TAG, "exception = " + e.getMessage());
            }
            if (("com.netease.nim.demo").equals(mPackage)) {
                return;
            }
            startActivity(mPackage, mClass);

        }
    }


    private void sendBroadcast2App(Intent intent, String tag1) {

        String replace = tag1.replace("broadcast:", "");
        String[] split = replace.split("\\|");
        int length = split.length;
        String action = split[0];
        intent.setAction(action);
        Log.d(TAG, "action: " + action);
        if (length >= 2) {
            String s = split[1];
            String[] split1 = s.split(";");
            int length1 = split1.length;
            for (int j = 0; j < length1; j++) {
                String s1 = split1[j];
                String[] split2 = s1.split(":");
                Log.d(TAG, "key:" + split2[0] + " value:" + split2[1]);
                if (split2[0].equals("KEY_TYPE")) {
                    intent.putExtra(split2[0], Integer.parseInt(split2[1]));
                } else {
                    intent.putExtra(split2[0], split2[1]);
                }
            }
        }
        Log.d(TAG, "-sendBroadcast-");
        mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        mContext.sendBroadcast(intent);
    }


    private void startActivity(String packagename, String classname) {
        mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Intent intent = new Intent();
        intent.setClassName(packagename, classname);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, "startActivity: packagename = " + packagename + " classname = " + classname);
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
        }
    }


    protected void startVoice() {
        if (mContext == null) {
            return;
        }
        Log.d(TAG, "start voice");
        Intent DuerOSIntent = new Intent("flyaudio.intent.action.CONTROL_VOICE");
        DuerOSIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        DuerOSIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        DuerOSIntent.putExtra("ENABLE_VOICE", "open_voice");
        mContext.sendBroadcast(DuerOSIntent);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Log.d(TAG, "onCheckChanged group = "+group+" checkId = "+checkedId);
        //如果是很快的点击的话
        if(isFastClick()){
           // itemChecked(mLeftindex,mBottomindex);
            mOncheckId = checkedId;
            mRadioGrp = group;
            mHandler.removeCallbacks(fastClickRunnable);
            mHandler.postDelayed(fastClickRunnable,1000);

            return;
        }
        mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        RadioButton checkView = (RadioButton) group.findViewById(checkedId);
        goToPage(checkView);
    }

    /**
     * 如果是快速点击底部的选项栏，以最后一次点击的item有效
     */
    Runnable fastClickRunnable = new Runnable() {
        @Override
        public void run() {
            mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            RadioButton checkView = (RadioButton) mRadioGrp.findViewById(mOncheckId);
            goToPage(checkView);
        }
    };


    /**
     * 初始化左侧导航栏的布局
     */
    public void initNaviLayout() {
        mTempBtn = (RadioButton) mNaviLayout.findViewById(R.id.tempbtn);
        //设置天气按钮默认的温度为20
        mTempBtn.setText(SharePreUtils.getString("temp",mContext,"20"));
        mNavigationBtn = (RadioButton) mNaviLayout.findViewById(R.id.navigationbtn);
        mMediaBtn = (RadioButton) mNaviLayout.findViewById(R.id.mediabtn);
        mCarinforBtn = (RadioButton) mNaviLayout.findViewById(R.id.carinforbtn);
        mMeBtn = (RadioButton) mNaviLayout.findViewById(R.id.mebtn);
        mVoiceBtn = (RadioButton) mNaviLayout.findViewById(R.id.voicebtn);
        mTempBtn.setOnClickListener(this);
        mNavigationBtn.setOnClickListener(this);
        mMediaBtn.setOnClickListener(this);
        mCarinforBtn.setOnClickListener(this);
        mMeBtn.setOnClickListener(this);
        mVoiceBtn.setOnClickListener(this);

    }

    private BroadcastReceiver mBroadcastRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context,final Intent intent) {
            Log.d(TAG, "action = " + intent.getAction() );
            if (intent != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//                if (KeyguardUpdateMonitor.getInstance(context).isDeviceProvisioned()) {
//                    startActivity("cn.flyaudio.launcher.carinfor", "cn.flyaudio.launcher.carinfor.CarInforActivity");
//                }
                Log.d(TAG,"onComplete");

            } else if (intent != null && Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
                Log.d(TAG,"seekbar size = "+mContext.getResources().getDimension(R.dimen.flyausio_seekbar_width));
                setTextConfigurationChange();
            }else if(intent!=null && ConstantUtils.TO4S_ACTION.equals(intent.getAction())){
//                itemChecked(1,4);
//                startActivity(ConstantUtils.GAODE_PACKAGENAME,ConstantUtils.GAODE_CLASSNAME);
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        sendBroadcast2App(intent, ConstantUtils.TO4S_TAG);
//                    }
//                },2000);

                startActivity(ConstantUtils.GAODE_PACKAGENAME,ConstantUtils.GAODE_CLASSNAME);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                  if( !((RadioButton)mNaviRgp.getChildAt(4)).isChecked()){
                        ((RadioButton)mNaviRgp.getChildAt(4)).setChecked(true);
                    }
                   // onCheckedChanged(mNaviRgp,R.id.rb5);
                    }
                },3000);

            }else if(intent != null && ConstantUtils.PAGE_CHANGE.equals(intent.getAction())){
                ActivityManager mAm = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                String activity_name = mAm.getRunningTasks(1).get(0).topActivity.getPackageName();
                String className = mAm.getRunningTasks(1).get(0).topActivity.getClassName();

                Log.d(TAG,"activity_name = "+activity_name+" className = "+className);
                setItemChecked(activity_name,className);
            }
        }
    };


    /**
     * 系统语言改变后，改变页面上的文字
     */
    private void setTextConfigurationChange() {
        ((RadioButton) mNaviRgp.getChildAt(0)).setText(R.string.flyaudio_Map);
        ((RadioButton) mNaviRgp.getChildAt(1)).setText(R.string.flyaudio_Electric);
        ((RadioButton) mNaviRgp.getChildAt(2)).setText(R.string.flyaudio_Cate);
        ((RadioButton) mNaviRgp.getChildAt(3)).setText(R.string.flyaudio_Park);
        ((RadioButton) mNaviRgp.getChildAt(4)).setText(R.string.flyaudio_4S);

        ((RadioButton) mMediaRgp.getChildAt(0)).setText(R.string.flyaudio_CloudMusic);
        ((RadioButton) mMediaRgp.getChildAt(1)).setText(R.string.flyaudio_media);
        ((RadioButton) mMediaRgp.getChildAt(2)).setText(R.string.flyaudio_bluetoothmusic);
        ((RadioButton) mMediaRgp.getChildAt(3)).setText(R.string.flyaudio_radio);
        ((RadioButton) mMediaRgp.getChildAt(4)).setText(R.string.flyaudio_NetworkRadio);

        ((RadioButton) mCarinforRgp.getChildAt(0)).setText(R.string.flyaudio_CarInfor);
       // ((RadioButton) mCarinforRgp.getChildAt(1)).setText(R.string.flyaudio_AirConditioner);
        ((RadioButton) mCarinforRgp.getChildAt(1)).setText(R.string.flyaudio_Phone);
        ((RadioButton) mCarinforRgp.getChildAt(2)).setText(R.string.flyaudio_CarSettings);
        ((RadioButton) mCarinforRgp.getChildAt(3)).setText(R.string.flyaudio_Smart);

        ((RadioButton) mMeRgp.getChildAt(0)).setText(R.string.flyaudio_Maintain);
        ((RadioButton) mMeRgp.getChildAt(1)).setText(R.string.flyaudio_Rescue);
        ((RadioButton) mMeRgp.getChildAt(2)).setText(R.string.flyaudio_UserFeedback);
        ((RadioButton) mMeRgp.getChildAt(3)).setText(R.string.flyaudio_PersonalCenter);
        ((RadioButton) mMeRgp.getChildAt(4)).setText(R.string.flyaudio_AllApp);
    }

    /**
     * 初始化选项的布局
     */
    public void initItemLayout() {
        mTempRgp = (RadioGroup) LayoutInflater.from(mContext).inflate(R.layout.flyaudio_systemui_rb_01, null);
        mNaviRgp = (RadioGroup) LayoutInflater.from(mContext).inflate(R.layout.flyaudio_systemui_rb_02, null);
        mMediaRgp = (RadioGroup) LayoutInflater.from(mContext).inflate(R.layout.flyaudio_systemui_rb_03, null);
        mCarinforRgp = (RadioGroup) LayoutInflater.from(mContext).inflate(R.layout.flyaudio_systemui_rb_04, null);
        mMeRgp = (RadioGroup) LayoutInflater.from(mContext).inflate(R.layout.flyaudio_systemui_rb_05, null);

        mTempRgp.setOnCheckedChangeListener(this);
        mNaviRgp.setOnCheckedChangeListener(this);
        mMediaRgp.setOnCheckedChangeListener(this);
        mCarinforRgp.setOnCheckedChangeListener(this);
        mMeRgp.setOnCheckedChangeListener(this);

        //设置车控默认选中
        ((RadioButton) mCarinforRgp.getChildAt(0)).setChecked(true);
    }


    public void initNaviView(View naviview) {

        mNaviLayout = (LinearLayout) naviview;
    }

    /**
     * 初始化systemui的布局
     */
    public void initView(View flyitemlayout, PhoneStatusBar statubar) {
        this.mPhoneStatusBar = statubar;
        mItemLayout = (LinearLayout) flyitemlayout;
        initNaviLayout();
        initItemLayout();
        //添加默认选项布局
        addItemlayout();
        //设置左侧按钮点击监听器
        mTempBtn.setOnClickListener(this);
        mNavigationBtn.setOnClickListener(this);
        mMediaBtn.setOnClickListener(this);
        mCarinforBtn.setOnClickListener(this);
        mMeBtn.setOnClickListener(this);
        mVoiceBtn.setOnClickListener(this);
        mContentOb = new SettingsValueChangeContentObserver();
        //注册监听
        mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.Global.DEVICE_PROVISIONED), true, mContentOb);

    }


    /**
     * 当点击天气和地图按钮的时候，会隐藏底部的导航栏，这个时候如果在点击其他的页面
     * 需要显示其他页面的导航栏
     */
    public void initViewAgain(View flyitemlayout, PhoneStatusBar statubar) {
        this.mPhoneStatusBar = statubar;
        mItemLayout = (LinearLayout) flyitemlayout;
        // initItemLayout();
    }

    /**
     * 添加默认的选项布局，默认的是车机设置的布局，默认选中车控
     */
    private void addItemlayout() {
        Log.d(TAG, "addItemlayout");
        mItemLayout.addView(mCarinforRgp);
    }


    public static boolean isFastClick() {
        //当前系统时间
        long currentTime = System.currentTimeMillis();
        //是否允许点击
        boolean isfastClick;
        if (currentTime - lastClickTime > spaceTime) {
            isfastClick = false;
        } else {
            isfastClick = true;
        }
        lastClickTime = currentTime;
        Log.d(TAG, "isfastClick = " + isfastClick);
        return isfastClick;
    }


    private class NetWorkStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                //获取联网状态的NetworkInfo对象
                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    //如果当前的网络连接成功并且网络连接可用
                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI
                                || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                            Log.i(TAG, "network connected");
                            String cityname = SharePreUtils.getString("cityname",mContext,"广州");
                            Log.d(TAG,"cityname = "+cityname);
                            if( !TextUtils.isEmpty(cityname)){
                                //获取天气
                                try{
                                    getWeather(cityname);
                                }catch (Exception e){
                                    Log.d(TAG,"weather exception");
                                }

                            }
                            //获取地理位置
                            try{
                                 getLocation();
                            }catch(Exception e){
                                Log.d(TAG,"location exception");
                            }

                        }
                    } else {
                        Log.i(TAG, "network disconnected");
                    }
                }
            }
        }
    }


    /**
     * 根据城市名 获取天气
     * @param cityname
     */
    private void getWeather(String cityname){
        Log.d(TAG, "cityname = " + cityname);
        //根据城市名返回未来三天的天气，目前没有自动获取位置的功能，暂时先获取广州的天气温度
        GetWeatherForCityNameUtil.GetInstance().GetOneCityWeatherForCityName(cityname, new GetWeatherForCityNameUtil.CallBackOneCityWeather() {
            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                Log.d(TAG, "onSuccess weatherInfo cityname = " + weatherInfo.getCityName());
                if (weatherInfo != null && weatherInfo.getWInfo() != null && weatherInfo.getWInfo().size() > 0) {
                    Log.d(TAG, "current temp = " + weatherInfo.getWInfo().get(0).getCurtemp());
                    //将温度保存到share里
                    SharePreUtils.putString("temp",weatherInfo.getWInfo().get(0).getCurtemp(),mContext);
                    //在天气按钮上显示温度
                    mTempBtn.setText(weatherInfo.getWInfo().get(0).getCurtemp());
                }
            }

            @Override
            public void onError(String s) {
                Log.d(TAG, "onError weather s = " + s);
            }
        });
    }


    /**
     * 获取定位信息
     */
    private void getLocation(){
        GetWeatherForCityNameUtil.GetInstance().GetLocation(new GetWeatherForCityNameUtil.LocationCallback() {
            @Override
            public void onSuccess(String s, String s1, String s2, String s3) {
                Log.d(TAG, "onSuccess  s = " + s + " s1 = " + s1 + " s2 = " + s2 + " s3 = " + s3);
                if (s != null && s1 != null) {
                    if (s.endsWith("市")) {
                        mCityName = s.replace("市", "");
                    } else if (s.endsWith("地区")) {
                        mCityName = s.replace("地区", "");
                    } else if (s.endsWith("自治州")) {
                        mCityName = s.replace("自治州", "");
                    }
                    Log.d(TAG, "mCityName = " + mCityName);
                    if (!TextUtils.isEmpty(mCityName)) {
                        if(!mCityName.equals(SharePreUtils.getString("cityname",mContext,"广州"))){
                            SharePreUtils.putString("cityname",mCityName,mContext);
                            try{
                                getWeather(mCityName);
                            }catch (Exception e){

                            }

                        }
                    }
                    try {
                        JSONObject object = new JSONObject(s1);
                        if (object != null && !object.isNull("district")) {
                            mDistrictName = object.getString("district");
                            SharePreUtils.putString("districtname",mDistrictName,mContext);
                            Log.d(TAG, "mDistrictName = " + mDistrictName);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String s) {
                Log.d(TAG, "onError location s = " + s);
            }
        });

    }



        private class SettingsValueChangeContentObserver extends ContentObserver {

            public SettingsValueChangeContentObserver() {
                super(new Handler());
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Log.d(TAG, "provision onchanged = " + KeyguardUpdateMonitor.getInstance(mContext).isDeviceProvisioned());
                if (KeyguardUpdateMonitor.getInstance(mContext).isDeviceProvisioned()) {
                    startActivity("cn.flyaudio.launcher.carinfor", "cn.flyaudio.launcher.carinfor.CarInforActivity");
                }
            }
        }


    /**
     * 根据传递过来的页面id设置底部和左侧item的选中，主要是通过flysdk进行跳页的才会有一个pageid
     * @param page
     */
    public void setItemChecked(int page){
        Log.d(TAG,"set ItemChecked page = "+page );
        switch (page){
            case ConstantUtils.BLUETOOTHPHONE_PAGE1:
            case ConstantUtils.BLUETOOTHPHONE_PAGE2:
            case ConstantUtils.BLUETOOTHPHONE_PAGE3:
            case ConstantUtils.BLUETOOTHPHONE_PAGE4:
            case ConstantUtils.BLUETOOTHPHONE_PAGE5:
            case ConstantUtils.BLUETOOTHPHONE_PAGE6:
            case ConstantUtils.BLUETOOTHPHONE_PAGE7:
            case ConstantUtils.BLUETOOTHPHONE_PAGE8:
                itemChecked(3,1);
                break;
            case ConstantUtils.CARSET_PAGE1:
            case ConstantUtils.CARSET_PAGE2:
            case ConstantUtils.CARSET_PAGE3:
            case ConstantUtils.CARSET_PAGE4:
            case ConstantUtils.CARSET_PAGE5:
                itemChecked(3,2);
                break;
            case ConstantUtils.MEDIA_PAGE:
                itemChecked(2,1);
                break;
            case ConstantUtils.BLUETOOTHMUSIC_PAGE1:
            case ConstantUtils.BLUETOOTHMUSIC_PAGE2:
                itemChecked(2,2);
                break;
            case ConstantUtils.RADIO_PAGE1:
            case ConstantUtils.RADIO_PAGE2:
                itemChecked(2,3);
                break;
            default:break;

        }
   }


    /**
     * 根据跳转后获取到的顶层activity来判断当前选中的底部item和左侧item
     * @param packagename
     * @param activityname
     */
   public void setItemChecked(String packagename,String activityname){
       Log.d(TAG,"setItemChecked packagename = "+packagename+" activityname = "+activityname);
        if(!TextUtils.isEmpty(packagename)&&!TextUtils.isEmpty(activityname)){

            if(ConstantUtils.WEATHER_PACKAGENAME.equals(packagename) && ConstantUtils.WEATHER_CLASSNAME.equals(activityname)){
                //如果展示的是天气页面
                itemChecked(0,0);
            }else if(ConstantUtils.CLOUDMUSIC_PACKAGENAME.equals(packagename) && ConstantUtils.CLOUDMUSIC_CLASSNAME.equals(activityname)){
                //如果展示的云音乐页面
                itemChecked(2,0);
            }else if(ConstantUtils.NETWORKRADIO_PACKAGENAME.equals(packagename)&&ConstantUtils.NETWORKRADIO_CLASSNAME.equals(activityname)){
                //如果是网络电台页面
                itemChecked(2,4);
            }else if(ConstantUtils.CARCONTROL_PACKAGENAME.equals(packagename)&&ConstantUtils.CARCONTROL_CLASSNAME.equals(activityname)){
                //如果是车控页面
                itemChecked(3,0);
            }else if(ConstantUtils.INTELLIGENTACCESSORT_PACKAGENAME.equals(packagename)&&ConstantUtils.INTELLIGENTACCESSORT_CLASSNAME.equals(activityname)){
                //智能附件
                itemChecked(3,3);
            }else if(ConstantUtils.MAINTAIN_PACKAGENAME.equals(packagename)&&ConstantUtils.MAINTAIN_CLASSNAME.equals(activityname)){
                //保养
                itemChecked(4,0);
            }else if(ConstantUtils.RESCUE_PACKAGENAME.equals(packagename)&&ConstantUtils.RESCUE_CLASSNAME.equals(activityname)){
                //救援
                itemChecked(4,1);
            }else if(ConstantUtils.USERFEEDBACK_PACKAGENAME.equals(packagename)&&ConstantUtils.USERFEEDBACK_CLASSNAME.equals(activityname)){
                //用户反馈
                itemChecked(4,2);
            }else if(ConstantUtils.PERSONL_PACKAGENAME.equals(packagename)&&ConstantUtils.PERSONL_CLASSNAME.equals(activityname)){
                //个人中心
                itemChecked(4,3);
            }else if(ConstantUtils.MOREAPPLICATION_PACKAGENAME.equals(packagename)&&ConstantUtils.MOREAPPLICATION_CLASSNAME.equals(activityname)){
                //第三方应用
                itemChecked(4,4);
            }else if(ConstantUtils.GAODE_PACKAGENAME.equals(packagename) && ConstantUtils.GAODE_CLASSNAME.equals(activityname)){
                Log.d(TAG,"setItemChecked gaode");
                itemChecked(1,0);
            }else if(ConstantUtils.VOICE_PACKAGENAME.equals(packagename) && ConstantUtils.VOICE_CLASSNAME.equals(activityname)){
                itemChecked(5,0);
            }else if(ConstantUtils.MEDIA_PACKAGENAME2.equals(packagename) && ConstantUtils.MEDIA_CLASSNAME2.equals(activityname)){
                itemChecked(2,1);
            }
        }
   }


    /**
     * 根据左下标和底部下标，设置item的选中状态
     * @param leftindex
     * @param bottomindex
     */
   public void itemChecked(int leftindex,int bottomindex){
       Log.d(TAG,"itemChecked leftindex = "+leftindex+" bottomindex = "+bottomindex);
       mLeftindex = leftindex;
       mBottomindex = bottomindex;
       if(leftindex == 0){
           //此时选中天气
          // mPhoneStatusBar.removeNavigationBar();
           mItemLayout.removeAllViews();
           mItemLayout.addView(mTempRgp);
           if(!mTempBtn.isChecked()) {
               mTempBtn.setChecked(true);
           }

       }else if(leftindex == 1){
           //地图
          // mPhoneStatusBar.addNavigationBarAgain();
           mItemLayout.removeAllViews();
           mItemLayout.addView(mNaviRgp);
           if(!mNavigationBtn.isChecked()){
               mNavigationBtn.setChecked(true);
           }
//           if(bottomindex == 0){
//               if( !((RadioButton)mNaviRgp.getChildAt(0)).isChecked()){
//                   ((RadioButton)mNaviRgp.getChildAt(0)).setChecked(true);
//               }
//           }else if(bottomindex == 1){
//               if( !((RadioButton)mNaviRgp.getChildAt(1)).isChecked()){
//                   ((RadioButton)mNaviRgp.getChildAt(1)).setChecked(true);
//               }
//           }else if(bottomindex == 2){
//               if( !((RadioButton)mNaviRgp.getChildAt(2)).isChecked()){
//                   ((RadioButton)mNaviRgp.getChildAt(2)).setChecked(true);
//               }
//           }else if(bottomindex == 3){
//               if( !((RadioButton)mNaviRgp.getChildAt(3)).isChecked()){
//                   ((RadioButton)mNaviRgp.getChildAt(3)).setChecked(true);
//               }
//           }else
//            if(bottomindex == 4){
//               if( !((RadioButton)mNaviRgp.getChildAt(4)).isChecked()){
//                   ((RadioButton)mNaviRgp.getChildAt(4)).setChecked(true);
//               }
//           }

       }else if(leftindex == 2){
           //媒体
          // mPhoneStatusBar.addNavigationBarAgain();
           mItemLayout.removeAllViews();
           mItemLayout.addView(mMediaRgp);
           if(!mMediaBtn.isChecked()){
               mMediaBtn.setChecked(true);
           }
           if(bottomindex == 0){
               //云音乐
               if( !((RadioButton)mMediaRgp.getChildAt(0)).isChecked()){
                   ((RadioButton)mMediaRgp.getChildAt(0)).setChecked(true);
               }
           }else if(bottomindex == 1){
               //播放器
               if(!((RadioButton)mMediaRgp.getChildAt(1)).isChecked()){
                   ((RadioButton)mMediaRgp.getChildAt(1)).setChecked(true);
               }
           }else if(bottomindex == 2){
               //蓝牙音乐
               if(!((RadioButton)mMediaRgp.getChildAt(2)).isChecked()){
                   ((RadioButton)mMediaRgp.getChildAt(2)).setChecked(true);
               }
           }else if(bottomindex == 3){
               //收音机
               if(!((RadioButton)mMediaRgp.getChildAt(3)).isChecked()){
                   ((RadioButton)mMediaRgp.getChildAt(3)).setChecked(true);
               }
           }else if(bottomindex == 4){
               //网络电台
               if(!((RadioButton)mMediaRgp.getChildAt(4)).isChecked()){
                   ((RadioButton)mMediaRgp.getChildAt(4)).setChecked(true);
               }
           }
       }else if(leftindex ==  3){
           //车辆信息
          // mPhoneStatusBar.addNavigationBarAgain();
           mItemLayout.removeAllViews();
           mItemLayout.addView(mCarinforRgp);
           if(!mCarinforBtn.isChecked()){
               mCarinforBtn.setChecked(true);
           }
           if(bottomindex == 0){
               //车控
               if(!((RadioButton)mCarinforRgp.getChildAt(0)).isChecked()){
                   ((RadioButton)mCarinforRgp.getChildAt(0)).setChecked(true);
               }
           }
//           else if(bottomindex == 1){
//               //空调
//               if(!((RadioButton)mCarinforRgp.getChildAt(1)).isChecked()){
//                   ((RadioButton)mCarinforRgp.getChildAt(1)).setChecked(true);
//               }
//           }
           else if(bottomindex == 1){
               //电话
               if(!((RadioButton)mCarinforRgp.getChildAt(1)).isChecked()){
                   ((RadioButton)mCarinforRgp.getChildAt(1)).setChecked(true);
               }
           }else if(bottomindex == 2){
               //车机设置
               if(!((RadioButton)mCarinforRgp.getChildAt(2)).isChecked()) {
                   ((RadioButton) mCarinforRgp.getChildAt(2)).setChecked(true);
               }
           }else if(bottomindex == 3){
               //智能附件
               if(!((RadioButton)mCarinforRgp.getChildAt(3)).isChecked()){
                   ((RadioButton)mCarinforRgp.getChildAt(3)).setChecked(true);
               }
           }
       }else if(leftindex == 4){
           //我的
          // mPhoneStatusBar.addNavigationBarAgain();
           mItemLayout.removeAllViews();
           mItemLayout.addView(mMeRgp);
           if(!mMeBtn.isChecked()){
               mMeBtn.setChecked(true);
           }
           if(bottomindex == 0){
               //保养
               if(!((RadioButton)mMeRgp.getChildAt(0)).isChecked()){
                   ((RadioButton)mMeRgp.getChildAt(0)).setChecked(true);
               }
           }else if(bottomindex == 1){
               //救援
               if( !((RadioButton)mMeRgp.getChildAt(1)).isChecked()){
                   ((RadioButton)mMeRgp.getChildAt(1)).setChecked(true);
               }
           }else if(bottomindex == 2){
               //用户反馈
               if( !((RadioButton)mMeRgp.getChildAt(2)).isChecked()) {
                   ((RadioButton) mMeRgp.getChildAt(2)).setChecked(true);
               }
           }else if(bottomindex == 3){
               //个人中心
               if(!((RadioButton)mMeRgp.getChildAt(3)).isChecked()){
                   ((RadioButton)mMeRgp.getChildAt(3)).setChecked(true);
               }
           }else if(bottomindex == 4){
               //第三方应用
               if(!((RadioButton)mMeRgp.getChildAt(4)).isChecked()){
                   ((RadioButton)mMeRgp.getChildAt(4)).setChecked(true);
               }
           }
       }else if(leftindex == 5){
           //选中语音
          // mPhoneStatusBar.removeNavigationBar();
           if(!mVoiceBtn.isChecked()){
               mVoiceBtn.setChecked(true);
           }
       }
   }



    }

