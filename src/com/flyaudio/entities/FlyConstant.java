package com.flyaudio.entities;


public final class FlyConstant {

	public static final int WAHT_STATUS_VALUE = 10;// 信使Message what值
	/** Key模块通信 **/
	public static final int MODE_CONTROL_ID = 196862;// ControlID
	public static final byte MODE_CONTROL_TYPE = (byte) 0XFE;// ControlType
	public static final int FAN_OPEN = 1;// 打开风扇
	public static final int FAN_CLOSE = 2;// 关闭风扇
	public static final int FAN_AUTO = 3;// 关闭风扇

	public static final int LCD_HIGH = 4;
	public static final int LCD_MID = 5;
	public static final int LCD_LOW = 6;
	public static final int LCD_CLOSE = 7;

	public static final int SEEK_INC = 23;
	public static final int SEEK_DEC = 20;
	public static final int VOL_MUTE = 14;// 客戶端 MUTE值
	public static final int VOL_INC = 15;// 客戶端 键VOL+值
	public static final int VOL_DEC = 16;// 客戶端 键VOL-值

	/** UI参数结构 **/
	public static final int NAVI_VOL_CLOSE = 40;
	public static final int NAVI_VOL_PALY = 41;
	public static final int REQUESTVOICE_STOP = 42;
	public static final int REQUESTVOICE_PALY = 43;
	public static final int DORMANCY = 44;
	public static final int RESTART = 45;
	public static final int RESTORESET = 46;
	public static final int UPGRADE = 47;
	public static final int IDLE = 48;
	public static final int RINGING = 49;
	public static final int OFFHOOK = 50;
	public static final int SCREEN_BLACK = 51;
	public static final int SCREEN_DIM = 52;
	public static final int SCREEN_MEDIUM = 53;
	public static final int SCREEN_LIGHT = 54;
	public static final int MEDIA_STOP = 55;
	public static final int MEDIA_PALY = 56;
	public static final int MEDIA_PAUSE = 57;
	public static final int POWER_ON = 58;
	public static final int POWER_OFF = 59;
	public static final int ACC_ON = 60;
	public static final int ACC_OFF = 61;
	public static final int RESERVED_POWER_ON = 62;
	public static final int RESERVED_POWER_OFF = 63;
	public static final int UI_MODE_CONTROL_ID = 0;// ControlID for UI
	public static final byte UI_MODE_CONTROL_TYPE = (byte) 0XFF;// ControlType

	/** MP3 模块 **/
	public static final int MP3_STOP = 30;
	public static final int MP3_PLAY = 31;
	public static final int MP3_PAUSE = 32;
	public static final int MP3_TOTALTIME = 33;
	public static final int MP3_CURRENT_PLAYTIME = 34;
	public static final int MP3_MODE_CONTROL_ID = 721150;// ControlID for MP3
	public static final byte MP3_MODE_CONTROL_TYPE = (byte) 0XFE;// ControlType

	/** GPS模块通信 凯立德导航消息 **/
	public static final int GPS_MODE_CONTROL_ID = 1118462;// ControlID for GPS
	public static final byte GPS_MODE_CONTROL_TYPE = (byte) 0XFE;// ControlType

	/** service模块通信（ControlID为0xFF00FE，UI操作为必须为0xFE） **/
	public static final int SERV_MODE_CONTROL_ID = 16711934;
	public static final byte SERV_MODE_CONTROL_TYPE = (byte) 0XFE;

	/** externalCtrl模块通信 (1) 语音信息 **/
	public static final int EXTER_MODE_CONTROL_ID = 16515073;
	public static final byte EXTER_MODE_CONTROL_TYPE = (byte) 0XFE;
	// 音量具体大小控制
	public static final int VOLUM_MAX = 68;
	public static final int VOLUM_MIN = 69;

	// 收音机控制
	public static final int RADIO_CLOSE = 70;
	public static final int RADIO_OPEN = 71;

	public static final int RADIO_SCAN = 72;
	public static final int RADIO_SCAN_PLUS = 73;
	public static final int RADIO_SCAN_MINUS = 74;
	public static final int RADIO_SCAN_STOP = 75;
	public static final int RADIO_SCAN_REPEAT = 76;
	public static final int RADIO_SCAN_PLUS_REPEAT = 77;
	public static final int RADIO_SCAN_MINUS_REPEAT = 78;
	// 碟机播放控制
	public static final int DVD_STOP = 80;
	public static final int DVD_PAUSE = 81;
	public static final int DVD_PLAY = 82;
	public static final int DVD_REWIND = 83;
	public static final int DVD_FORWARD = 84;
	public static final int DVD_ORDER_CYCLE = 85;
	public static final int DVD_SHUFFLE = 86;
	public static final int DVD_SINGLE_CYCLE = 87;
	public static final int DVD_SCAN_CONTROL = 88;
	// 车身控制
	public static final int DORMER = 90;
	public static final int TRUNK = 91;
	public static final int NEAR_LIGHTS = 92;
	public static final int FAR_LIGHTS = 93;
	public static final int FOG_LIGHTS = 94;
	public static final int FRONT_FOG_LIGHTS = 95;
	public static final int BEHIND_FOG_LIGHTS = 96;
	public static final int GALLERY_LIGHTS = 97;
	public static final int WARNING_LIGHTS = 98;
	public static final int CAR_WINDOWS = 99;

	public static final int ON = 100;
	public static final int OFF = 101;
	// 飞歌应用管理
	public static final int NAVI = 110;
	public static final int DVD = 111;
	public static final int RADIO = 112;
	public static final int MEDIA = 113;
	public static final int BLUETOOTH = 114;
	public static final int SYNC = 115;
	public static final int DRIVING_RECORD = 116;
	public static final int TV = 117;
	public static final int TIRE = 118;
	public static final int BLUETOOTH_MUSIC = 119;
	public static final int SYSTEM_SET = 120;
	public static final int ENTERTAINMENT = 121;
	public static final int AUXI_INPUT = 122;
	public static final int IPOD = 123;
	public static final int AIR_CINDITION = 124;
	public static final int CAR_INFO = 125;
	// 空调控制
	public static final int INC_TEMP = 130;
	public static final int DEC_TEMP = 131;

	public static final int CRYOGEN = 132;
	public static final int HEATING = 133;
	public static final int DEHUMIDIFICATION = 134;
	public static final int DEFROST = 135;

	public static final int UP_BLOW = 136;
	public static final int DOWN_BLOW = 137;
	public static final int SWEPT = 138;

	public static final int MAX_WIND = 139;
	public static final int MIN_WIND = 140;
	public static final int HIGH_WIND = 141;
	public static final int LOW_WIND = 142;
	public static final int PLUS_WIND = 143;
	public static final int MINUS_WIND = 144;
	// 模拟面板KEY功能控制
	public static final int PANEL_PWR_VO = 149;
	public static final int PANEL_VOL_MUTE = 150;
	public static final int PANEL_VOL_OPEN = 151;
	public static final int PANEL_VOL_INC = 152;
	public static final int PANEL_VOL_DEC = 153;

	public static final int PANEL_PREVIOUS = 160;
	public static final int PANEL_NEXT = 161;

	public static final int PANEL_SEEK = 154;
	public static final int PANEL_NAVI = 155;
	public static final int PANEL_TUNE_AUDIO = 156;
	public static final int PANEL_DVD = 157;
	// 降噪模块模块切换
	public static final int NOISE_REDUCTION = 170;// 降噪
	public static final int ECHO_CANCEL = 171;// 回声消除
	public static final int WAKE = 172;// 唤醒
	public static final int DIRECT_RECORD = 173;// 关闭所用功能直接录音
}
