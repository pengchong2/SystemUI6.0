package com.flyaudio.utils;

/**
 * @author pengchong
 * @des 需要用到的一些常量
 */
public class ConstantUtils {

    //记录当滑动音量进度条的时候，如果滑动到0的时候，静音图标需要改变状态，不是0的时候，静音图标也要改变
    public static String MUTE_ACTION = "com.flyaudio.volume.ismute";
    //检测系统页面发生改变的广播
    public static String PAGE_CHANGE = "cn.flyaudio.launcher.senddata";
    //从保养跳转到4s的界面
    public static String TO4S_ACTION = "com.flyaudio.action.to4s";

    //跳转到4s店的tag
    public static String TO4S_TAG = "broadcast:AUTONAVI_STANDARD_BROADCAST_RECV|KEY_TYPE:10036;KEYWORDS:4s;SOURCE_APP:Third App";

    //电话
    public static final int BLUETOOTHPHONE_PAGE1 = 2304;
    public static final int BLUETOOTHPHONE_PAGE2 = 2306;
    public static final int BLUETOOTHPHONE_PAGE3 = 2309;
    public static final int BLUETOOTHPHONE_PAGE4 = 2311;
    public static final int BLUETOOTHPHONE_PAGE5 = 2314;
    public static final int BLUETOOTHPHONE_PAGE6 = 2315;
    public static final int BLUETOOTHPHONE_PAGE7 = 2318;
    public static final int BLUETOOTHPHONE_PAGE8 = 2320;
    //车机设置
    public static final int CARSET_PAGE1 = 513;
    public static final int CARSET_PAGE2 = 514;
    public static final int CARSET_PAGE3 = 515;
    public static final int CARSET_PAGE4 = 516;
    public static final int CARSET_PAGE5 = 528;
    //播放器
    public static final int MEDIA_PAGE = 2816;
    //蓝牙音乐
    public static final int BLUETOOTHMUSIC_PAGE1 = 2307;
    public static final int BLUETOOTHMUSIC_PAGE2 = 2308;
    //收音机
    public static final int RADIO_PAGE1 = 1024;
    public static final int RADIO_PAGE2 = 1025;

    //天气应用
    public static final String WEATHER_PACKAGENAME = "cn.flyaudio.Weather";
    public static final String WEATHER_CLASSNAME = "cn.flyaudio.weather.activity.WeatherDetailsActivity";

    //云音乐
    public static final String CLOUDMUSIC_PACKAGENAME = "cn.kuwo.kwmusiccar";
    public static final String CLOUDMUSIC_CLASSNAME = "cn.kuwo.kwmusiccar.MainActivity";

    //网络电台
    public static final String NETWORKRADIO_PACKAGENAME = "com.flyaudio.flyradioonline";
    public static final String NETWORKRADIO_CLASSNAME = "com.flyaudio.flyradioonline.task.main.MainActivity";

    //车控
    public static final String CARCONTROL_PACKAGENAME = "cn.flyaudio.launcher.carinfor";
    public static final String CARCONTROL_CLASSNAME = "cn.flyaudio.launcher.carinfor.CarInforActivity";

    //智能附件
    public static final String INTELLIGENTACCESSORT_PACKAGENAME =  "com.flyaudio.intelligentaccessory";
    public static final String INTELLIGENTACCESSORT_CLASSNAME =  "com.flyaudio.intelligentaccessory.AccessoryActivity";

    //保养
    public static final String MAINTAIN_PACKAGENAME = "cn.flyaudio.vehiclemaintenance";
    public static final String MAINTAIN_CLASSNAME = "cn.flyaudio.vehiclemaintenance.activity.NetWorkVersionActivity";

    //救援
    public static final String RESCUE_PACKAGENAME = "com.flyaudio.flymine";
    public static final String RESCUE_CLASSNAME = "com.flyaudio.flymine.activity.RescueActivity";

    //用户反馈
    public static final String USERFEEDBACK_PACKAGENAME = "cn.flyaudio.flyremotediagnose" ;
    public static final String USERFEEDBACK_CLASSNAME = "cn.flyaudio.flyremotediagnose.ui.GuideActivity";

    //个人中心
    public static final String PERSONL_PACKAGENAME = "cn.flyaudio.accountmanager";
    public static final String PERSONL_CLASSNAME = "cn.flyaudio.accountmanager.ui.AccountActivity";

    //第三方应用
    public static final String MOREAPPLICATION_PACKAGENAME = "cn.flyaudio.moreapplication";
    public static final String MOREAPPLICATION_CLASSNAME = "cn.flyaudio.moreapplication.ui.MainActivity";

    //高德地图
    public static final String GAODE_PACKAGENAME = "com.autonavi.amapauto";
    public static final String GAODE_CLASSNAME = "com.autonavi.amapauto.MainMapActivity";

    //同行者语音
    public static final String VOICE_PACKAGENAME = "com.txznet.adapter";
    public static final String VOICE_CLASSNAME = "com.txznet.adapter.ui.TipsActivity";

    //本地音乐播放器
    public static final String MEDIA_PACKAGENAME1 = "cn.flyaudio.media";
    public static final String MEDIA_CLASSNAME1 = "cn.flyaudio.media.view.activity.MusicPlaybackActivity";

    public static final String MEDIA_PACKAGENAME2 = "cn.flyaudio.media";
    public static final String MEDIA_CLASSNAME2 = "cn.flyaudio.media.view.activity.MediaBrowserActivity";
}
