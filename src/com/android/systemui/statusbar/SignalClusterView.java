/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.telephony.SubscriptionInfo;
/// M: Add for CT6M, add data activity icon. @{
import android.telephony.TelephonyManager;
/// @}
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
/// M: Add for CT6M, add data activity icon. @{
import android.view.Gravity;
/// @}
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;

import com.mediatek.systemui.ext.ISignalClusterExt;
import com.mediatek.systemui.statusbar.extcb.FeatureOptionUtils;
import com.mediatek.systemui.statusbar.extcb.ISignalClusterInfo;
import com.mediatek.systemui.statusbar.extcb.PhoneStateExt;
import com.mediatek.systemui.statusbar.extcb.PluginFactory;
import com.mediatek.systemui.statusbar.defaultaccount.DefaultAccountStatus;
import com.mediatek.systemui.statusbar.util.SIMHelper;
import com.mediatek.systemui.statusbar.util.FeatureOptions;

import java.util.ArrayList;
import java.util.List;

// Intimately tied to the design of res/layout/signal_cluster_view.xml
public class SignalClusterView
        extends LinearLayout
        implements NetworkControllerImpl.SignalCallback,
        SecurityController.SecurityControllerCallback, Tunable {

    static final String TAG = "SignalClusterView";
    static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private static final String SLOT_AIRPLANE = "airplane";
    private static final String SLOT_MOBILE = "mobile";
    private static final String SLOT_WIFI = "wifi";
    private static final String SLOT_ETHERNET = "ethernet";

    NetworkControllerImpl mNC;
    SecurityController mSC;

    private boolean mNoSimsVisible = false;
    private boolean mVpnVisible = false;
    private boolean mEthernetVisible = false;
    private int mEthernetIconId = 0;
    private int mLastEthernetIconId = -1;
    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0;
    private int mLastWifiStrengthId = -1;
    private boolean mIsAirplaneMode = false;
    private int mAirplaneIconId = 0;
    private int mLastAirplaneIconId = -1;
    private String mAirplaneContentDescription;
    private String mWifiDescription;
    private String mEthernetDescription;
    private ArrayList<PhoneState> mPhoneStates = new ArrayList<PhoneState>();
    private int mIconTint = Color.WHITE;
    private float mDarkIntensity;

    ViewGroup mEthernetGroup, mWifiGroup;
    View mNoSimsCombo;
    ImageView mVpn, mEthernet, mWifi, mAirplane, mNoSims, mEthernetDark, mWifiDark, mNoSimsDark;
    View mWifiAirplaneSpacer;
    View mWifiSignalSpacer;
    LinearLayout mMobileSignalGroup;

    private int mWideTypeIconStartPadding;
    private int mSecondaryTelephonyPadding;
    private int mEndPadding;
    private int mEndPaddingNothingVisible;

    private boolean mBlockAirplane;
    private boolean mBlockMobile;
    private boolean mBlockWifi;
    private boolean mBlockEthernet;

    /// M: Support "SystemUI HD Voice icon" for op.
    private ImageView mHDVoiceIcon;

    /// M: Support "Operator plugin's ISignalClusterExt".@{
    private ISignalClusterExt mSignalClusterExt = null;
    /// M: Support "Operator plugin's ISignalClusterExt". @}

    /// M: Add For MTK Customized feature
    /// Add For [SIM indicator] @ {
    private ImageView mDefAccountIcon;
    // Add for [VOLTE status icon]
    private ImageView mVolteIcon;
    /// @ }
    public SignalClusterView(Context context) {
        this(context, null);
    }

    public SignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /// M: Support "Operator plugin's ISignalClusterExt".@{
        mSignalClusterExt = PluginFactory.getStatusBarPlugin(this.getContext())
                .customizeSignalCluster();
        mSignalClusterExt.setSignalClusterInfo(new SignalClusterInfo());
        /// M: Support "Operator plugin's ISignalClusterExt". @}
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        if (!StatusBarIconController.ICON_BLACKLIST.equals(key)) {
            return;
        }
        ArraySet<String> blockList = StatusBarIconController.getIconBlacklist(newValue);
        boolean blockAirplane = blockList.contains(SLOT_AIRPLANE);
        boolean blockMobile = blockList.contains(SLOT_MOBILE);
        boolean blockWifi = blockList.contains(SLOT_WIFI);
        boolean blockEthernet = blockList.contains(SLOT_ETHERNET);

        if (blockAirplane != mBlockAirplane || blockMobile != mBlockMobile
                || blockEthernet != mBlockEthernet || blockWifi != mBlockWifi) {
            mBlockAirplane = blockAirplane;
            mBlockMobile = blockMobile;
            mBlockEthernet = blockEthernet;
            mBlockWifi = blockWifi;
            // Re-register to get new callbacks.
            mNC.removeSignalCallback(this);
            mNC.addSignalCallback(this);
        }
    }

    public void setNetworkController(NetworkControllerImpl nc) {
        if (DEBUG) Log.d(TAG, "NetworkController=" + nc);
        mNC = nc;

        /// M: Support "Operator plugin's ISignalClusterExt".@{
        mSignalClusterExt.setNetworkControllerExt(nc.getNetworkControllerExt());
        /// M: Support "Operator plugin's ISignalClusterExt". @}
    }

    public void setSecurityController(SecurityController sc) {
        if (DEBUG) Log.d(TAG, "SecurityController=" + sc);
        mSC = sc;
        mSC.addCallback(this);
        mVpnVisible = mSC.isVpnEnabled();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWideTypeIconStartPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.wide_type_icon_start_padding);
        mSecondaryTelephonyPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.secondary_telephony_padding);
        mEndPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.signal_cluster_battery_padding);
        mEndPaddingNothingVisible = getContext().getResources().getDimensionPixelSize(
                R.dimen.no_signal_cluster_battery_padding);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mVpn            = (ImageView) findViewById(R.id.vpn);
        mEthernetGroup  = (ViewGroup) findViewById(R.id.ethernet_combo);
        mEthernet       = (ImageView) findViewById(R.id.ethernet);
        mEthernetDark   = (ImageView) findViewById(R.id.ethernet_dark);
        mWifiGroup      = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi           = (ImageView) findViewById(R.id.wifi_signal);
        mWifiDark       = (ImageView) findViewById(R.id.wifi_signal_dark);
        mAirplane       = (ImageView) findViewById(R.id.airplane);
        mNoSims         = (ImageView) findViewById(R.id.no_sims);
        mNoSimsDark     = (ImageView) findViewById(R.id.no_sims_dark);
        mNoSimsCombo    =             findViewById(R.id.no_sims_combo);
        mWifiAirplaneSpacer =         findViewById(R.id.wifi_airplane_spacer);
        mWifiSignalSpacer =           findViewById(R.id.wifi_signal_spacer);
        mMobileSignalGroup = (LinearLayout) findViewById(R.id.mobile_signal_group);
        for (PhoneState state : mPhoneStates) {
            mMobileSignalGroup.addView(state.mMobileGroup);
        }
        TunerService.get(mContext).addTunable(this, StatusBarIconController.ICON_BLACKLIST);
        /// M: Support [SIM Indicator] @ {
        mDefAccountIcon = (ImageView) findViewById(R.id.default_sim_type);
        // Support for [VOLTE status icon]
        mVolteIcon = (ImageView) findViewById(R.id.volte_indicator);
        /// @ }

        /// M: Support "SystemUI HD Voice icon" for op. @{
        mHDVoiceIcon      = (ImageView) findViewById(R.id.hd_voice_icon);
        mHDVoiceIcon.setVisibility(View.GONE);
        /// M: Support "SystemUI HD Voice icon" for op. @}

        /// M: Support "Operator plugin's ISignalClusterExt".@{
        mSignalClusterExt.onAttachedToWindow(mMobileSignalGroup, mNoSims);
        /// M: Support "Operator plugin's ISignalClusterExt". @}

        apply();
        applyIconTint();
    }

    @Override
    protected void onDetachedFromWindow() {
        mVpn            = null;
        mEthernetGroup  = null;
        mEthernet       = null;
        mWifiGroup      = null;
        mWifi           = null;
        mAirplane       = null;
        mMobileSignalGroup.removeAllViews();
        mMobileSignalGroup = null;
        TunerService.get(mContext).removeTunable(this);

        /// M: Support "Operator plugin's ISignalClusterExt".@{
        mSignalClusterExt.onDetachedFromWindow();
        /// M: Support "Operator plugin's ISignalClusterExt". @}

        super.onDetachedFromWindow();
    }

    // From SecurityController.
    @Override
    public void onStateChanged() {
        post(new Runnable() {
            @Override
            public void run() {
                mVpnVisible = mSC.isVpnEnabled();
                apply();
            }
        });
    }

    @Override
    public void setWifiIndicators(boolean enabled, IconState statusIcon, IconState qsIcon,
            boolean activityIn, boolean activityOut, String description) {
        mWifiVisible = statusIcon.visible && !mBlockWifi;
        mWifiStrengthId = statusIcon.icon;
        mWifiDescription = statusIcon.contentDescription;

        apply();
    }
    ///M: Support[Network Type on StatusBar]. Add one more parameter networkType.
    @Override
    public void setMobileDataIndicators(IconState statusIcon, IconState qsIcon, int statusType,
            int networkType, int qsType, boolean activityIn, boolean activityOut,
            /// M: Add for CT6M. add activity icon @{
            int dataActivity,
            int primarySimIcon,
            /// @}
            String typeContentDescription, String description, boolean isWide, int subId) {
        PhoneState state = getState(subId);
        if (state == null) {
            return;
        }
        state.mMobileVisible = statusIcon.visible && !mBlockMobile;
        state.mMobileStrengthId = statusIcon.icon;
        state.mMobileTypeId = statusType;
        state.mMobileDescription = statusIcon.contentDescription;
        state.mMobileTypeDescription = typeContentDescription;
        state.mIsMobileTypeIconWide = statusType != 0 && isWide;
        state.mNetworkIcon = networkType;

        /// M: Add for CT6M. add activity icon @{
        state.mDataActivity = dataActivity;
        state.mPrimarySimIconId = primarySimIcon;
        /// @}

        /// M: Support "Operator plugin's ISignalClusterExt". @{
        mSignalClusterExt.setMobileDataIndicators(subId, state.mMobileVisible, state.mMobileGroup,
                state.mNetworkType, (ViewGroup) (state.mMobile.getParent()), state.mMobile,
                state.mMobileType, state.mMobileStrengthId, state.mMobileTypeId,
                state.mMobileDescription, state.mMobileTypeDescription,
                state.mIsMobileTypeIconWide);
        /// M: Support "Operator plugin's ISignalClusterExt". @}

        apply();
    }

    @Override
    public void setEthernetIndicators(IconState state) {
        mEthernetVisible = state.visible && !mBlockEthernet;
        mEthernetIconId = state.icon;
        mEthernetDescription = state.contentDescription;

        apply();
    }

    @Override
    public void setNoSims(boolean show) {
        mNoSimsVisible = show && !mBlockMobile;
        /// M: Support "Operator plugin's ISignalClusterExt". @{
        mNoSimsVisible = PluginFactory.getStatusBarPlugin(mContext).customizeHasNoSims(
                mNoSimsVisible);
        /// M: Support "Operator plugin's ISignalClusterExt". @}
        // M: Bug fix ALPS02302143, in case UI need to be refreshed.
        apply();
    }

    @Override
    public void setSubs(List<SubscriptionInfo> subs) {
        if (hasCorrectSubs(subs)) {
            return;
        }
        // Clear out all old subIds.
        mPhoneStates.clear();
        if (mMobileSignalGroup != null) {
            mMobileSignalGroup.removeAllViews();
        }
        final int n = subs.size();
        for (int i = 0; i < n; i++) {
            inflatePhoneState(subs.get(i).getSubscriptionId());
        }
        if (isAttachedToWindow()) {
            applyIconTint();
        }

        /// M: Support "Operator plugin's ISignalClusterExt". @{
        mSignalClusterExt.setSubs(subs, inflatePhoneStateExt(subs));
        /// M: Support "Operator plugin's ISignalClusterExt". @}
    }

    private boolean hasCorrectSubs(List<SubscriptionInfo> subs) {
        final int N = subs.size();
        if (N != mPhoneStates.size()) {
            return false;
        }
        for (int i = 0; i < N; i++) {
            if (mPhoneStates.get(i).mSubId != subs.get(i).getSubscriptionId()) {
                return false;
            }
        }
        return true;
    }

    private PhoneState getState(int subId) {
        for (PhoneState state : mPhoneStates) {
            if (state.mSubId == subId) {
                return state;
            }
        }
        Log.e(TAG, "Unexpected subscription " + subId);
        return null;
    }

    private PhoneState inflatePhoneState(int subId) {
        PhoneState state = new PhoneState(subId, mContext);
        if (mMobileSignalGroup != null) {
            mMobileSignalGroup.addView(state.mMobileGroup);
        }
        mPhoneStates.add(state);
        return state;
    }

    @Override
    public void setIsAirplaneMode(IconState icon) {
        mIsAirplaneMode = icon.visible && !mBlockAirplane;
        mAirplaneIconId = icon.icon;
        mAirplaneContentDescription = icon.contentDescription;

        apply();
    }

    @Override
    public void setMobileDataEnabled(boolean enabled) {
        // Don't care.
    }

    @Override
    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        // Standard group layout onPopulateAccessibilityEvent() implementations
        // ignore content description, so populate manually
        if (mEthernetVisible && mEthernetGroup != null &&
                mEthernetGroup.getContentDescription() != null)
            event.getText().add(mEthernetGroup.getContentDescription());
        if (mWifiVisible && mWifiGroup != null && mWifiGroup.getContentDescription() != null)
            event.getText().add(mWifiGroup.getContentDescription());
        for (PhoneState state : mPhoneStates) {
            state.populateAccessibilityEvent(event);
        }
        return super.dispatchPopulateAccessibilityEventInternal(event);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        if (mEthernet != null) {
            mEthernet.setImageDrawable(null);
            mEthernetDark.setImageDrawable(null);
            mLastEthernetIconId = -1;
        }

        if (mWifi != null) {
            mWifi.setImageDrawable(null);
            mWifiDark.setImageDrawable(null);
            mLastWifiStrengthId = -1;
        }

        for (PhoneState state : mPhoneStates) {
            if (state.mMobile != null) {
                state.mMobile.setImageDrawable(null);
            }
            if (state.mMobileType != null) {
                state.mMobileType.setImageDrawable(null);
            }
        }

        if (mAirplane != null) {
            mAirplane.setImageDrawable(null);
            mLastAirplaneIconId = -1;
        }

        /// M: Support "Operator plugin's ISignalClusterExt". @{
        mSignalClusterExt.onRtlPropertiesChanged(layoutDirection);
        /// M: Support "Operator plugin's ISignalClusterExt". @}

        apply();
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    // Run after each indicator change.
    private void apply() {
        if (mWifiGroup == null) return;

        mVpn.setVisibility(mVpnVisible ? View.VISIBLE : View.GONE);
        if (DEBUG) Log.d(TAG, String.format("vpn: %s", mVpnVisible ? "VISIBLE" : "GONE"));

        if (mEthernetVisible) {
            if (mLastEthernetIconId != mEthernetIconId) {
                mEthernet.setImageResource(mEthernetIconId);
                mEthernetDark.setImageResource(mEthernetIconId);
                mLastEthernetIconId = mEthernetIconId;
            }
            mEthernetGroup.setContentDescription(mEthernetDescription);
            mEthernetGroup.setVisibility(View.VISIBLE);
        } else {
            mEthernetGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Log.d(TAG,
                String.format("ethernet: %s",
                    (mEthernetVisible ? "VISIBLE" : "GONE")));


        if (mWifiVisible) {
            if (mWifiStrengthId != mLastWifiStrengthId) {
                mWifi.setImageResource(mWifiStrengthId);
                mWifiDark.setImageResource(mWifiStrengthId);
                mLastWifiStrengthId = mWifiStrengthId;
            }
            mWifiGroup.setContentDescription(mWifiDescription);
            mWifiGroup.setVisibility(View.VISIBLE);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Log.d(TAG,
                String.format("wifi: %s sig=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId));

        boolean anyMobileVisible = false;
        /// M: Support for [Network Type on Statusbar]
        /// A spacer is set between networktype and WIFI icon @ {
        if (FeatureOptions.MTK_CTA_SET) {
            anyMobileVisible = true;
        }
        /// @ }
        int firstMobileTypeId = 0;
        for (PhoneState state : mPhoneStates) {
            if (state.apply(anyMobileVisible)) {
                if (!anyMobileVisible) {
                    firstMobileTypeId = state.mMobileTypeId;
                    anyMobileVisible = true;
                }
            }
        }

        if (mIsAirplaneMode) {
            if (mLastAirplaneIconId != mAirplaneIconId) {
                mAirplane.setImageResource(mAirplaneIconId);
                mLastAirplaneIconId = mAirplaneIconId;
            }
            mAirplane.setContentDescription(mAirplaneContentDescription);
            mAirplane.setVisibility(View.VISIBLE);
            /// M: Bug fix when airplane mode is on go to hide volte icon @ {
            mVolteIcon.setVisibility(View.GONE);
            /// @ }
        } else {
            mAirplane.setVisibility(View.GONE);
        }

        /// M: Support "SystemUI HD Voice icon" for op. @{
        mSignalClusterExt.setHDVoiceIcon(mHDVoiceIcon);
        /// M: Support "SystemUI HD Voice icon" for op. @}

        // flyaudio status_bar 取消wifi出现时出现间距
       /* if (mIsAirplaneMode && mWifiVisible) {
            mWifiAirplaneSpacer.setVisibility(View.VISIBLE);
        } else {
            mWifiAirplaneSpacer.setVisibility(View.GONE);
        } */
        //flyaudio end

        if (((anyMobileVisible && firstMobileTypeId != 0) || mNoSimsVisible) && mWifiVisible) {
            mWifiSignalSpacer.setVisibility(View.VISIBLE);
        } else {
            mWifiSignalSpacer.setVisibility(View.GONE);
        }

        mNoSimsCombo.setVisibility(mNoSimsVisible ? View.VISIBLE : View.GONE);
        /// M: Add for CT6M. hide the no sim icon on airplane mode @{
        if (FeatureOptionUtils.isMTK_CT6M_SUPPORT()) {
            if (mIsAirplaneMode) {
                mNoSimsCombo.setVisibility(View.GONE);
            }
        }
        /// @}

        boolean anythingVisible = mNoSimsVisible || mWifiVisible || mIsAirplaneMode
                || anyMobileVisible || mVpnVisible || mEthernetVisible;
        setPaddingRelative(0, 0, anythingVisible ? mEndPadding : mEndPaddingNothingVisible, 0);

        /// M: Support "Operator plugin's ISignalClusterExt". @{
        mSignalClusterExt.apply();
        /// M: Support "Operator plugin's ISignalClusterExt". @}
    }

    public void setIconTint(int tint, float darkIntensity) {
        boolean changed = tint != mIconTint || darkIntensity != mDarkIntensity;
        mIconTint = tint;
        mDarkIntensity = darkIntensity;
        if (changed && isAttachedToWindow()) {
            applyIconTint();
        }
    }

    private void applyIconTint() {
        setTint(mVpn, mIconTint);
        setTint(mAirplane, mIconTint);
        applyDarkIntensity(mDarkIntensity, mNoSims, mNoSimsDark);
        applyDarkIntensity(mDarkIntensity, mWifi, mWifiDark);
        applyDarkIntensity(mDarkIntensity, mEthernet, mEthernetDark);
        for (int i = 0; i < mPhoneStates.size(); i++) {
            mPhoneStates.get(i).setIconTint(mIconTint, mDarkIntensity);
        }
    }

    private void applyDarkIntensity(float darkIntensity, View lightIcon, View darkIcon) {
        lightIcon.setAlpha(1 - darkIntensity);
        darkIcon.setAlpha(darkIntensity);
    }

    private void setTint(ImageView v, int tint) {
        v.setImageTintList(ColorStateList.valueOf(tint));
    }

    private class PhoneState {
        private final int mSubId;
        private boolean mMobileVisible = false;
        private int mMobileStrengthId = 0, mMobileTypeId = 0, mNetworkIcon = 0;
        /// M: Add for CT6M. add activity icon @{
        private int mDataActivity;
        private int mPrimarySimIconId = 0;
        /// @}
        private boolean mIsMobileTypeIconWide;
        private String mMobileDescription, mMobileTypeDescription;

        private ViewGroup mMobileGroup;

        private ImageView mMobile, mMobileDark, mMobileType;

        /// M: Add for new features @ {
        // Add for [Network Type on Statusbar]
        private ImageView mNetworkType;
        /// Support [SIM Indicator]
        private int mSignalBackgroundIconId;

        /// @ }

        /// M: Add for CT 6M. @ {
        private ImageView mMobileDataActivity;
        private FrameLayout mMobileNetworkDataGroup;
        private ImageView mPrimarySimCard;
        /// @}

        public PhoneState(int subId, Context context) {
            /// M: Add for CT 6M. @ {
            if (FeatureOptionUtils.isMTK_CT6M_SUPPORT()) {
                mMobileDataActivity = new ImageView(context);
                mPrimarySimCard = new ImageView(context);

                mMobileNetworkDataGroup = new FrameLayout(context);
                mMobileNetworkDataGroup.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            /// @}
            ViewGroup root = (ViewGroup) LayoutInflater.from(context)
                    .inflate(R.layout.mobile_signal_group_ext, null);
            setViews(root);
            mSubId = subId;
        }

        public void setViews(ViewGroup root) {
            mMobileGroup    = root;
            mMobile         = (ImageView) root.findViewById(R.id.mobile_signal);
            mMobileDark     = (ImageView) root.findViewById(R.id.mobile_signal_dark);
            mMobileType     = (ImageView) root.findViewById(R.id.mobile_type);
            mNetworkType    = (ImageView) root.findViewById(R.id.network_type);
            /// M: Add for CT 6M. adjust data and activity icon. @{
            if (FeatureOptionUtils.isMTK_CT6M_SUPPORT()) {
               // add primary sim card
                if (mMobileType.getParent() != null) {
                    ((ViewGroup) mMobileType.getParent()).addView(mPrimarySimCard,
                            new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                // Add views to mMobileGroup
                // 1. DataType
                if (mMobileType.getParent() != null) {
                    ((ViewGroup) mMobileType.getParent()).removeView(mMobileType);
                }
                mMobileNetworkDataGroup.addView(mMobileType, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER));

                // 2. DataActivity
                mMobileNetworkDataGroup.addView(mMobileDataActivity,
                        new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER));

                final int addViewIndex = mMobileGroup.indexOfChild(mNetworkType);
                if (addViewIndex >= 0) {
                    mMobileGroup.addView(mMobileNetworkDataGroup, addViewIndex);
                }
            }
            /// @}
        }

        public boolean apply(boolean isSecondaryIcon) {
            if (mMobileVisible && !mIsAirplaneMode) {
                mMobile.setImageResource(mMobileStrengthId);
                Drawable mobileDrawable = mMobile.getDrawable();
                if (mobileDrawable instanceof Animatable) {
                    Animatable ad = (Animatable) mobileDrawable;
                    if (!ad.isRunning()) {
                        ad.start();
                    }
                }

                mMobileDark.setImageResource(mMobileStrengthId);
                Drawable mobileDarkDrawable = mMobileDark.getDrawable();
                if (mobileDarkDrawable instanceof Animatable) {
                    Animatable ad = (Animatable) mobileDarkDrawable;
                    if (!ad.isRunning()) {
                        ad.start();
                    }
                }
                /// M: Add for CT6M. add activity icon @{
                if (FeatureOptionUtils.isMTK_CT6M_SUPPORT()) {
                    mMobileDataActivity.setImageResource(
                            getDataActivityIcon(mDataActivity));
                    mPrimarySimCard.setImageResource(mPrimarySimIconId);
                }
                /// @}
                mMobileType.setImageResource(mMobileTypeId);
                mMobileGroup.setContentDescription(mMobileTypeDescription
                        + " " + mMobileDescription);
                mMobileGroup.setVisibility(View.VISIBLE);
            } else {
                mMobileGroup.setVisibility(View.GONE);
            }

            /// M: Set all added or customised view. @ {
            setCustomizeViewProperty();
            /// @ }

            // When this isn't next to wifi, give it some extra padding between the signals.
            mMobileGroup.setPaddingRelative(isSecondaryIcon ? mSecondaryTelephonyPadding : 0,
                    0, 0, 0);
            mMobile.setPaddingRelative(mIsMobileTypeIconWide ? mWideTypeIconStartPadding : 0,
                    0, 0, 0);
            mMobileDark.setPaddingRelative(mIsMobileTypeIconWide ? mWideTypeIconStartPadding : 0,
                    0, 0, 0);

            if (DEBUG) Log.d(TAG, String.format("mobile: %s sig=%d typ=%d",
                        (mMobileVisible ? "VISIBLE" : "GONE"), mMobileStrengthId, mMobileTypeId));

            mMobileType.setVisibility(mMobileTypeId != 0 ? View.VISIBLE : View.GONE);
            /// M: Add for CT6M. set data activity and primary sim icon visibility. @{
            if (FeatureOptionUtils.isMTK_CT6M_SUPPORT()) {
                if (mDataActivity != TelephonyManager.DATA_ACTIVITY_NONE
                        && mMobileType.getVisibility() == View.VISIBLE) {
                    mMobileDataActivity.setVisibility(View.VISIBLE);
                } else {
                    mMobileDataActivity.setVisibility(View.GONE);
                }

                mPrimarySimCard.setVisibility(mPrimarySimIconId != 0 ? View.VISIBLE : View.GONE);
            }
            /// @}
            return mMobileVisible;
        }

        public void populateAccessibilityEvent(AccessibilityEvent event) {
            if (mMobileVisible && mMobileGroup != null
                    && mMobileGroup.getContentDescription() != null) {
                event.getText().add(mMobileGroup.getContentDescription());
            }
        }

        public void setIconTint(int tint, float darkIntensity) {
            applyDarkIntensity(darkIntensity, mMobile, mMobileDark);
            setTint(mMobileType, tint);
        }

        /// M: Set all added or customised view. @ {
        private void setCustomizeViewProperty() {
            // Add for [Network Type on Statusbar], the place to set network type icon.
            setNetworkIcon();

            // Support [SIM Indicator]
            setIndicatorUnderSignalIcon(mSignalBackgroundIconId);
        }

        /// M : Add for [Network Type on Statusbar]
        private void setNetworkIcon() {
            // Network type is CTA feature, so non CTA project should not set this.
            if (!FeatureOptions.MTK_CTA_SET) {
                return;
            }
            if (mNetworkIcon == 0) {
                mNetworkType.setVisibility(View.GONE);
            } else {
                mNetworkType.setImageResource(mNetworkIcon);
                mNetworkType.setVisibility(View.VISIBLE);
            }
        }

        /// M: Support [SIM Indicator]
        private void setIndicatorUnderSignalIcon(int iconId) {
            if (iconId == 0) {
                mMobileGroup.setBackgroundDrawable(null);
            } else {
                mMobileGroup.setBackgroundResource(iconId);
            }
        }
        /// @ }
    }


    /// M: Support [SIM Indicator] @ {
    @Override
    public void setDefaultAccountStatus(DefaultAccountStatus status) {
        if (status == null) {
            hideAccountStatus();
            return;
        }
        int iconId = status.getAccountStatusIconId();
        if (iconId == 0) {
            // Hide default account status icon.
            mDefAccountIcon.setVisibility(View.GONE);
            setIndicatorUnderSignalIcon(status);
        } else {
            setDefaultAccountStatusIcon(iconId);
            //Then hide under indicator icon.
            hideSignalIconIndicator();
        }
    }
    private void setIndicatorUnderSignalIcon(DefaultAccountStatus status) {
        for (PhoneState state : mPhoneStates) {
            // To set indicator for one sim, also need to hide the other ones,
            // and to hide other ones to set iconId = 0
            int iconId = state.mSubId == status.getSubId() ?
                    status.getDefSignalBackgroundIconId() : 0;
            state.mSignalBackgroundIconId = iconId;
            state.apply(false);
        }
    }
    private void setDefaultAccountStatusIcon(int iconId) {
        mDefAccountIcon.setImageResource(iconId);
        if (!mIsAirplaneMode) {
            mDefAccountIcon.setVisibility(View.VISIBLE);
        } else {
            mDefAccountIcon.setVisibility(View.GONE);
        }
    }
    private void hideAccountStatus() {
        hideSignalIconIndicator();
        mDefAccountIcon.setVisibility(View.GONE);
    }

    private void hideSignalIconIndicator() {
        for (PhoneState state : mPhoneStates) {
            state.mSignalBackgroundIconId = 0;
            state.apply(false);
        }
    }
    @Override
    public void setVolteStatusIcon(final int iconId) {
        if (iconId > 0) {
            /// M: customize VoLTE icon. @{
            mSignalClusterExt.customizeVoLTEIcon(mVolteIcon, iconId);
            /// M: customize VoLTE icon. @}
        }
        mVolteIcon.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
    }

   /// M: Support "Operator plugin's ISignalClusterExt". @{
    private final PhoneStateExt[] inflatePhoneStateExt(List<SubscriptionInfo> subs) {
        final int slotCount = SIMHelper.getSlotCount();
        final PhoneStateExt[] phoneStateExts = new PhoneStateExt[slotCount];
        for (int i = SIMHelper.SLOT_INDEX_DEFAULT; i < slotCount; i++) {
            for (SubscriptionInfo subInfo : subs) {
                if (subInfo.getSimSlotIndex() == i) {
                    phoneStateExts[i] = inflatePhoneStateExt(subInfo);
                    break;
                }
            }
        }
        return phoneStateExts;
    }

    private final PhoneStateExt inflatePhoneStateExt(SubscriptionInfo subInfo) {
        final int slotId = subInfo.getSimSlotIndex();
        final int subId = subInfo.getSubscriptionId();
        final PhoneState state = getOrInflateState(subId);
        final PhoneStateExt phoneStateExt = new PhoneStateExt(slotId, subId);
        phoneStateExt.setViews(state.mMobileGroup, state.mNetworkType,
                (ViewGroup) (state.mMobile.getParent()), state.mMobile,
                state.mMobileType);
        return phoneStateExt;
    }

    private PhoneState getOrInflateState(int subId) {
        for (PhoneState state : mPhoneStates) {
            if (state.mSubId == subId) {
                return state;
            }
        }
        return inflatePhoneState(subId);
    }

    /**
     * SignalCluster Info Support "Operator plugin's ISignalClusterExt.
     */
    private class SignalClusterInfo implements ISignalClusterInfo {

        @Override
        public boolean isWifiIndicatorsVisible() {
            return mWifiVisible;
        }

        @Override
        public boolean isNoSimsVisible() {
            return mNoSimsVisible;
        }

        @Override
        public boolean isAirplaneMode() {
            return mIsAirplaneMode;
        }

        @Override
        public int getWideTypeIconStartPadding() {
            return mWideTypeIconStartPadding;
        }

        @Override
        public int getSecondaryTelephonyPadding() {
            return mSecondaryTelephonyPadding;
        }
    }
    /// M: Support "Operator plugin's ISignalClusterExt". @}

    /// M: Add for CT6M. add activity icon and primary sim icon @{
    static final int DATA_ACTIVITY_NONE = R.drawable.ct_stat_sys_signal_not_inout;
    static final int DATA_ACTIVITY_IN = R.drawable.ct_stat_sys_signal_in;
    static final int DATA_ACTIVITY_OUT = R.drawable.ct_stat_sys_signal_out;
    static final int DATA_ACTIVITY_INOUT = R.drawable.ct_stat_sys_signal_inout;

    /**
     * M: getDataActivityIcon: Get DataActivity icon by dataActivity type.
     * @param dataActivity : dataActivity Type
     * @return  dataActivity icon ID
     */
    static public int getDataActivityIcon(int dataActivity) {
        int icon = DATA_ACTIVITY_NONE;

        switch(dataActivity) {
        case TelephonyManager.DATA_ACTIVITY_IN:
            icon = DATA_ACTIVITY_IN;
            break;
        case TelephonyManager.DATA_ACTIVITY_OUT:
            icon = DATA_ACTIVITY_OUT;
            break;
        case TelephonyManager.DATA_ACTIVITY_INOUT:
            icon = DATA_ACTIVITY_INOUT;
            break;
        default:
            break;
        }
        return icon;
    }
    /// M: Add for CT6M. add activity icon and primary sim icon @{
}

