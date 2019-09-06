LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    src/com/android/systemui/EventLogTags.logtags
#flyaudio：添加通信接口 IProxyConnet.aidl
LOCAL_SRC_FILES += \
    src/com/flyaudio/proxyservice/aidl/IProxyConnet.aidl
#flyaudio：LOCAL_STATIC_JAVA_LIBRARIES中添加v4包
LOCAL_STATIC_JAVA_LIBRARIES := Keyguard android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.systemui.ext
LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_JAVA_LIBRARIES += ims-common

LOCAL_PACKAGE_NAME := SystemUI
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_STATIC_JAVA_LIBRARIES += flysdks
LOCAL_STATIC_JAVA_LIBRARIES += flyweather 

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_RESOURCE_DIR := \
    frameworks/base/packages/Keyguard/res \
    frameworks/base/packages/Keyguard/res_ext \
    $(LOCAL_PATH)/res \
    $(LOCAL_PATH)/res_ext
LOCAL_AAPT_FLAGS := --auto-add-overlay --extra-packages com.android.keyguard

ifneq ($(SYSTEM_UI_INCREMENTAL_BUILDS),)
    LOCAL_PROGUARD_ENABLED := disabled
    LOCAL_JACK_ENABLED := incremental
endif

include frameworks/base/packages/SettingsLib/common.mk

LOCAL_JACK_ENABLED := disabled

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := flysdks:libs/flysdk.jar \
flyweather:libs/FlyWeather_Location.jar \

include $(BUILD_MULTI_PREBUILT) 


