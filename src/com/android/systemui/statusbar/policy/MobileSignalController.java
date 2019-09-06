/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkCapabilities;
import android.os.Looper;
import android.telephony.PhoneStateListener;
/// M: add for op. fix issue: ALPS02093201. @{
import android.telephony.PreciseDataConnectionState;
/// @}
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.cdma.EriInfo;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkControllerImpl.Config;
import com.android.systemui.statusbar.policy.NetworkControllerImpl.SubscriptionDefaults;

import com.mediatek.systemui.ext.IMobileIconExt;
import com.mediatek.systemui.ext.IStatusBarPlugin;
import com.mediatek.systemui.PluginManager;
/// M: add for op. fix issue: ALPS02093201. @{
import com.mediatek.systemui.statusbar.extcb.BehaviorSet;
import com.mediatek.systemui.statusbar.extcb.FeatureOptionUtils;
import com.mediatek.systemui.statusbar.extcb.PluginFactory;
/// @}
import com.mediatek.systemui.statusbar.extcb.SvLteController;
import com.mediatek.systemui.statusbar.networktype.NetworkTypeUtils;

import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Objects;


public class MobileSignalController extends SignalController<
        MobileSignalController.MobileState, MobileSignalController.MobileIconGroup> {
    private static final String TAG = "MobileSignalController";

    private final TelephonyManager mPhone;
    private final SubscriptionDefaults mDefaults;
    private final String mNetworkNameDefault;
    private final String mNetworkNameSeparator;
    @VisibleForTesting
    final PhoneStateListener mPhoneStateListener;
    // Save entire info for logging, we only use the id.
    /// M: Fix bug ALPS02416794
    /*final*/ SubscriptionInfo mSubscriptionInfo;

    // @VisibleForDemoMode
    final SparseArray<MobileIconGroup> mNetworkToIconLookup;

    // Since some pieces of the phone state are interdependent we store it locally,
    // this could potentially become part of MobileState for simplification/complication
    // of code.
    private int mDataNetType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
    private int mDataState = TelephonyManager.DATA_DISCONNECTED;
    private ServiceState mServiceState;
    private SignalStrength mSignalStrength;
    private MobileIconGroup mDefaultIcons;
    private Config mConfig;

    /// M: Support SVLTE. @{
    public SvLteController mSvLteController;
    private IMobileIconExt mMobileIconExt;
    /// @}
    /// M: For plugin.
    private IStatusBarPlugin mStatusBarExt;

    ///M: Support voLTE
    private int mImsRegState = -1;

    // TODO: Reduce number of vars passed in, if we have the NetworkController, probably don't
    // need listener lists anymore.
    public MobileSignalController(Context context, Config config, boolean hasMobileData,
            TelephonyManager phone, CallbackHandler callbackHandler,
            NetworkControllerImpl networkController, SubscriptionInfo info,
            SubscriptionDefaults defaults, Looper receiverLooper) {
        super("MobileSignalController(" + info.getSubscriptionId() + ")", context,
                NetworkCapabilities.TRANSPORT_CELLULAR, callbackHandler,
                networkController);
        mNetworkToIconLookup = new SparseArray<>();
        mConfig = config;
        mPhone = phone;
        mDefaults = defaults;
        mSubscriptionInfo = info;

        /// M: Init plugin @ {
        mMobileIconExt = PluginManager.getMobileIconExt(context);
        mStatusBarExt = PluginManager.getSystemUIStatusBarExt(context);
        /// @ }

        mPhoneStateListener = new MobilePhoneStateListener(info.getSubscriptionId(),
                receiverLooper);
        mNetworkNameSeparator = getStringIfExists(R.string.status_bar_network_name_separator);
        mNetworkNameDefault = getStringIfExists(
                com.android.internal.R.string.lockscreen_carrier_default);

        ///M: Support SVLTE. @{
        mSvLteController = new SvLteController(mContext, info);
        ///M: Support SVLTE. @}

        mapIconSets();

        String networkName = info.getCarrierName() != null ? info.getCarrierName().toString()
                : mNetworkNameDefault;
        mLastState.networkName = mCurrentState.networkName = networkName;
        mLastState.networkNameData = mCurrentState.networkNameData = networkName;
        mLastState.enabled = mCurrentState.enabled = hasMobileData;
        mLastState.iconGroup = mCurrentState.iconGroup = mDefaultIcons;
        // Get initial data sim state.
        updateDataSim();
    }

    public void setConfiguration(Config config) {
        mConfig = config;
        mapIconSets();
        updateTelephony();
    }

    public int getDataContentDescription() {
        return getIcons().mDataContentDescription;
    }

    public void setAirplaneMode(boolean airplaneMode) {
        mCurrentState.airplaneMode = airplaneMode;
        notifyListenersIfNecessary();
    }

    @Override
    public void updateConnectivity(BitSet connectedTransports, BitSet validatedTransports) {
        boolean isValidated = validatedTransports.get(mTransportType);
        mCurrentState.isDefault = connectedTransports.get(mTransportType);
        // Only show this as not having connectivity if we are default.
        mCurrentState.inetCondition = (isValidated || !mCurrentState.isDefault) ? 1 : 0;
        Log.d(mTag,"mCurrentState.inetCondition = " + mCurrentState.inetCondition);
        /// M: Disable inetCondition check as this condition is not sufficient in some cases.
        /// So always set it is in net with value 1. @ {
        mCurrentState.inetCondition =
                mMobileIconExt.customizeMobileNetCondition(mCurrentState.inetCondition);
        /// @}
        notifyListenersIfNecessary();
    }

    public void setCarrierNetworkChangeMode(boolean carrierNetworkChangeMode) {
        mCurrentState.carrierNetworkChangeMode = carrierNetworkChangeMode;
        updateTelephony();
    }

    /**
     * Start listening for phone state changes.
     */
    public void registerListener() {
        mPhone.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_CALL_STATE
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                        /// M: add for op. fix issue: ALPS02093201. @{
                        | PhoneStateListener.LISTEN_PRECISE_DATA_CONNECTION_STATE
                        /// @}
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY
                        | PhoneStateListener.LISTEN_CARRIER_NETWORK_CHANGE);
    }

    /**
     * Stop listening for phone state changes.
     */
    public void unregisterListener() {
        mPhone.listen(mPhoneStateListener, 0);
    }

    /**
     * Produce a mapping of data network types to icon groups for simple and quick use in
     * updateTelephony.
     */
    private void mapIconSets() {
        mNetworkToIconLookup.clear();

        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyIcons.THREE_G);
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyIcons.THREE_G);
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyIcons.THREE_G);
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyIcons.THREE_G);
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_UMTS, TelephonyIcons.THREE_G);

        if (!mConfig.showAtLeast3G) {
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_UNKNOWN,
                    TelephonyIcons.UNKNOWN);
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_EDGE, TelephonyIcons.E);
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_CDMA, TelephonyIcons.ONE_X);
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyIcons.ONE_X);

            mDefaultIcons = TelephonyIcons.G;
        } else {
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_UNKNOWN,
                    TelephonyIcons.THREE_G);
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyIcons.THREE_G);
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyIcons.THREE_G);
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyIcons.THREE_G);
            mDefaultIcons = TelephonyIcons.THREE_G;
        }

        MobileIconGroup hGroup = TelephonyIcons.THREE_G;
        if (mConfig.hspaDataDistinguishable) {
            hGroup = TelephonyIcons.H;
        }
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_HSDPA, hGroup);
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_HSUPA, hGroup);
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_HSPA, hGroup);
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_HSPAP, hGroup);

        if (mConfig.show4gForLte) {
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_LTE, TelephonyIcons.FOUR_G);
        } else {
            mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_LTE, TelephonyIcons.LTE);
        }
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_IWLAN, TelephonyIcons.WFC);
        /// M: Support 4G+ icon
        mNetworkToIconLookup.put(TelephonyManager.NETWORK_TYPE_LTEA, TelephonyIcons.FOUR_GA);
    }

    @Override
    public void notifyListeners() {
        MobileIconGroup icons = getIcons();

        String contentDescription = getStringIfExists(getContentDescription());
        String dataContentDescription = getStringIfExists(icons.mDataContentDescription);

        // Show icon in QS when we are connected or need to show roaming.
        boolean showDataIcon = mCurrentState.dataConnected
                || mCurrentState.iconGroup == TelephonyIcons.ROAMING;
        //add by flyaudio 信号加强
        if (mCurrentState.connected && mCurrentState.level < 4) {
			mCurrentState.level += 1;
		}
        //flyaudio end
        IconState statusIcon = new IconState(mCurrentState.enabled && !mCurrentState.airplaneMode,
                getCurrentIconId(), contentDescription);

        int qsTypeIcon = 0;
        IconState qsIcon = null;
        String description = null;
        // Only send data sim callbacks to QS.
        if (mCurrentState.dataSim) {
            qsTypeIcon = showDataIcon ? icons.mQsDataType : 0;
            qsIcon = new IconState(mCurrentState.enabled
                    && !mCurrentState.isEmergency, getQsCurrentIconId(), contentDescription);
            description = mCurrentState.isEmergency ? null : mCurrentState.networkName;
        }
        boolean activityIn = mCurrentState.dataConnected
                        && !mCurrentState.carrierNetworkChangeMode
                        && mCurrentState.activityIn;
        boolean activityOut = mCurrentState.dataConnected
                        && !mCurrentState.carrierNetworkChangeMode
                        && mCurrentState.activityOut;
        showDataIcon &= mCurrentState.isDefault
                || mCurrentState.iconGroup == TelephonyIcons.ROAMING;
        int typeIcon = showDataIcon ? icons.mDataType : 0;

        /** M: Support [Network Type on StatusBar], change the implement methods.
          * Get the network icon base on service state.
          * Add one more parameter for network type.
          * @ { **/
        int networkIcon = mCurrentState.networkIcon;

        /// M: Add for CT6M, add data activity icon & primary simcard icon. @{
        int dataActivity = TelephonyManager.DATA_ACTIVITY_NONE;
        if (mCurrentState.dataConnected) {
            if (mCurrentState.activityIn
                    && mCurrentState.activityOut) {
                dataActivity = TelephonyManager.DATA_ACTIVITY_INOUT;
            } else if (mCurrentState.activityIn) {
                dataActivity = TelephonyManager.DATA_ACTIVITY_IN;
            } else if (mCurrentState.activityOut) {
                dataActivity = TelephonyManager.DATA_ACTIVITY_OUT;
            }
        }
        int primarySimIcon = TelephonyIcons.getPrimarySimIcon(
                isRoaming(), mSubscriptionInfo.getSubscriptionId());

        if (FeatureOptionUtils.isMTK_CT6M_SUPPORT() &&
                mCurrentState.iconGroup == TelephonyIcons.ROAMING) {
            if (mCurrentState.dataConnected) {
                typeIcon = mNetworkToIconLookup.get(mDataNetType).mDataType;
            } else {
                typeIcon = 0;
            }
        }
        /// @}

        mCallbackHandler.setMobileDataIndicators(statusIcon, qsIcon, typeIcon, networkIcon,
                qsTypeIcon, activityIn, activityOut, 
                /// M: Add for CT6M. add activity icon @{
                dataActivity,
                primarySimIcon,
                /// @}
                dataContentDescription, description,
                icons.mIsWide, mSubscriptionInfo.getSubscriptionId());
        /** @ }*/
        /// M: Support voLTE
        updateVolte();

        /// M: update plmn label @{
        mNetworkController.refreshPlmnCarrierLabel();
        /// @}
    }

    @Override
    protected MobileState cleanState() {
        return new MobileState();
    }

    private boolean hasService() {
        ///M: Support SVLTE. @{
        if (SvLteController.isMediatekSVLteDcSupport(mSubscriptionInfo)) {
            return mSvLteController.hasService();
        }
        ///M: Support SVLTE. @}
        if (mServiceState != null) {
            // Consider the device to be in service if either voice or data
            // service is available. Some SIM cards are marketed as data-only
            // and do not support voice service, and on these SIM cards, we
            // want to show signal bars for data service as well as the "no
            // service" or "emergency calls only" text that indicates that voice
            // is not available.
            switch (mServiceState.getVoiceRegState()) {
                case ServiceState.STATE_POWER_OFF:
                    return false;
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_EMERGENCY_ONLY:
                    return mServiceState.getDataRegState() == ServiceState.STATE_IN_SERVICE;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

    private boolean isCdma() {
        return (mSignalStrength != null) && !mSignalStrength.isGsm();
    }

    public boolean isEmergencyOnly() {
        ///M: Support SVLTE. @{
        if (SvLteController.isMediatekSVLteDcSupport(mSubscriptionInfo)) {
            return mSvLteController.isEmergencyOnly();
        }
        ///M: Support SVLTE. @}
        return (mServiceState != null && mServiceState.isEmergencyOnly());
    }

    private boolean isRoaming() {
        if (isCdma()) {
            final int iconMode = mServiceState.getCdmaEriIconMode();
            return mServiceState.getCdmaEriIconIndex() != EriInfo.ROAMING_INDICATOR_OFF
                    && (iconMode == EriInfo.ROAMING_ICON_MODE_NORMAL
                        || iconMode == EriInfo.ROAMING_ICON_MODE_FLASH);
        } else {
            return mServiceState != null && mServiceState.getRoaming();
        }
    }

    /// M: Support VoLte @{
    private void updateVolte() {
        if (mImsRegState == -1) {
            return;
        }
        int slotId = mSubscriptionInfo.getSimSlotIndex();
        int iconId = mImsRegState == ServiceState.STATE_IN_SERVICE && isLteNetWork()
            ? mNetworkController.getVolteIconId(slotId) : 0;
        Log.d(mTag, "updateVolte: slotId: " + slotId + " iconId: " + iconId);
        mCallbackHandler.setVolteStatusIcon(iconId);
    }

    public boolean isLteNetWork() {
        return (mDataNetType == TelephonyManager.NETWORK_TYPE_LTE
            || mDataNetType == TelephonyManager.NETWORK_TYPE_LTEA);
    }

    public void setImsRegState(int regState) {
        mImsRegState = regState;
        Log.d(mTag,"setImsRegState mImsRegState = " + mImsRegState);
    }
    /// M: @}

    private boolean isCarrierNetworkChangeActive() {
        return mCurrentState.carrierNetworkChangeMode;
    }

    public void handleBroadcast(Intent intent) {
        String action = intent.getAction();
        if (action.equals(TelephonyIntents.SPN_STRINGS_UPDATED_ACTION)) {
            updateNetworkName(intent.getBooleanExtra(TelephonyIntents.EXTRA_SHOW_SPN, false),
                    intent.getStringExtra(TelephonyIntents.EXTRA_SPN),
                    intent.getStringExtra(TelephonyIntents.EXTRA_DATA_SPN),
                    intent.getBooleanExtra(TelephonyIntents.EXTRA_SHOW_PLMN, false),
                    intent.getStringExtra(TelephonyIntents.EXTRA_PLMN));
            notifyListenersIfNecessary();
        } else if (action.equals(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
            updateDataSim();
            notifyListenersIfNecessary();
        }
    }

    private void updateDataSim() {
        int defaultDataSub = mDefaults.getDefaultDataSubId();
        if (SubscriptionManager.isValidSubscriptionId(defaultDataSub)) {
            mCurrentState.dataSim = defaultDataSub == mSubscriptionInfo.getSubscriptionId();
        } else {
            // There doesn't seem to be a data sim selected, however if
            // there isn't a MobileSignalController with dataSim set, then
            // QS won't get any callbacks and will be blank.  Instead
            // lets just assume we are the data sim (which will basically
            // show one at random) in QS until one is selected.  The user
            // should pick one soon after, so we shouldn't be in this state
            // for long.
            mCurrentState.dataSim = true;
        }
    }

    /**
     * Updates the network's name based on incoming spn and plmn.
     */
    void updateNetworkName(boolean showSpn, String spn, String dataSpn,
            boolean showPlmn, String plmn) {
        if (CHATTY) {
            Log.d("CarrierLabel", "updateNetworkName showSpn=" + showSpn
                    + " spn=" + spn + " dataSpn=" + dataSpn
                    + " showPlmn=" + showPlmn + " plmn=" + plmn);
        }
        StringBuilder str = new StringBuilder();
        StringBuilder strData = new StringBuilder();
        if (showPlmn && plmn != null) {
            str.append(plmn);
            strData.append(plmn);
        }
        if (showSpn && spn != null) {
            if (str.length() != 0) {
                str.append(mNetworkNameSeparator);
            }
            str.append(spn);
        }
        if (str.length() != 0) {
            mCurrentState.networkName = str.toString();
        } else {
            mCurrentState.networkName = mNetworkNameDefault;
        }
        if (showSpn && dataSpn != null) {
            if (strData.length() != 0) {
                strData.append(mNetworkNameSeparator);
            }
            strData.append(dataSpn);
        }
        if (strData.length() != 0) {
            mCurrentState.networkNameData = strData.toString();
        } else {
            mCurrentState.networkNameData = mNetworkNameDefault;
        }
    }

    /**
     * Updates the current state based on mServiceState, mSignalStrength, mDataNetType,
     * mDataState, and mSimState.  It should be called any time one of these is updated.
     * This will call listeners if necessary.
     */
    private final void updateTelephony() {
        if (DEBUG) {
            Log.d(mTag, "updateTelephonySignalStrength: hasService=" + hasService()
                    + " ss=" + mSignalStrength);
        }
        mCurrentState.connected = hasService() && mSignalStrength != null;
        /// M: Add for plugin wifi-only mode.
        mCurrentState.connected = mStatusBarExt.updateSignalStrengthWifiOnlyMode(
            mServiceState, mCurrentState.connected);
        if (mCurrentState.connected) {
            if (!mSignalStrength.isGsm() && mConfig.alwaysShowCdmaRssi) {
                mCurrentState.level = mSignalStrength.getCdmaLevel();
            } else {
                mCurrentState.level = mSignalStrength.getLevel();
            }
        }
        if (mNetworkToIconLookup.indexOfKey(mDataNetType) >= 0) {
            mCurrentState.iconGroup = mNetworkToIconLookup.get(mDataNetType);
        } else {
            mCurrentState.iconGroup = mDefaultIcons;
        }
        mCurrentState.dataConnected = mCurrentState.connected
                && mDataState == TelephonyManager.DATA_CONNECTED;
        /// M: Add for op network tower type.
        mCurrentState.customizedState = PluginFactory.getStatusBarPlugin(mContext).
            customizeMobileState(mServiceState, mCurrentState.customizedState);

        if (isCarrierNetworkChangeActive()) {
            mCurrentState.iconGroup = TelephonyIcons.CARRIER_NETWORK_CHANGE;
        } else if (isRoaming()) {
            mCurrentState.iconGroup = TelephonyIcons.ROAMING;
        }
        if (isEmergencyOnly() != mCurrentState.isEmergency) {
            mCurrentState.isEmergency = isEmergencyOnly();
            mNetworkController.recalculateEmergency();
        }
        // Fill in the network name if we think we have it.
        if (mCurrentState.networkName == mNetworkNameDefault && mServiceState != null
                && !TextUtils.isEmpty(mServiceState.getOperatorAlphaShort())) {
            mCurrentState.networkName = mServiceState.getOperatorAlphaShort();
        }
        /// M: For network type big icon.
        mCurrentState.networkIcon =
            NetworkTypeUtils.getNetworkTypeIcon(mServiceState, mConfig, hasService());

        notifyListenersIfNecessary();
    }

    @VisibleForTesting
    void setActivity(int activity) {
        mCurrentState.activityIn = activity == TelephonyManager.DATA_ACTIVITY_INOUT
                || activity == TelephonyManager.DATA_ACTIVITY_IN;
        mCurrentState.activityOut = activity == TelephonyManager.DATA_ACTIVITY_INOUT
                || activity == TelephonyManager.DATA_ACTIVITY_OUT;
        notifyListenersIfNecessary();
    }

    @Override
    public void dump(PrintWriter pw) {
        super.dump(pw);
        pw.println("  mSubscription=" + mSubscriptionInfo + ",");
        pw.println("  mServiceState=" + mServiceState + ",");
        pw.println("  mSignalStrength=" + mSignalStrength + ",");
        pw.println("  mDataState=" + mDataState + ",");
        pw.println("  mDataNetType=" + mDataNetType + ",");
    }

    class MobilePhoneStateListener extends PhoneStateListener {
        public MobilePhoneStateListener(int subId, Looper looper) {
            super(subId, looper);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (DEBUG) {
                Log.d(mTag, "onSignalStrengthsChanged signalStrength=" + signalStrength +
                        ((signalStrength == null) ? "" : (" level=" + signalStrength.getLevel())));
            }
            mSignalStrength = signalStrength;
            ///M: Support SVLTE. @{
            mSvLteController.onSignalStrengthsChanged(signalStrength);
            ///M: Support SVLTE. @}
            updateTelephony();
        }

        @Override
        public void onServiceStateChanged(ServiceState state) {
            if (DEBUG) {
                Log.d(mTag, "onServiceStateChanged voiceState=" + state.getVoiceRegState()
                        + " dataState=" + state.getDataRegState());
            }
            mServiceState = state;
            ///M: Support SVLTE. @{
            mSvLteController.onServiceStateChanged(state);
            ///M: Support SVLTE. @}
            /// M: Support 4G+ icon.
            mDataNetType =
                NetworkTypeUtils.getDataNetTypeFromServiceState(mDataNetType, mServiceState);
            updateTelephony();
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            if (DEBUG) {
                Log.d(mTag, "onDataConnectionStateChanged: state=" + state
                        + " type=" + networkType);
            }
            mDataState = state;
            mDataNetType = networkType;
            /// M: Support 4G+ icon.
            mDataNetType =
                NetworkTypeUtils.getDataNetTypeFromServiceState(mDataNetType, mServiceState);
            ///M: Support SVLTE. @{
            mSvLteController.onDataConnectionStateChanged(state, networkType);
            ///M: Support SVLTE. @}
            updateTelephony();
        }

        /// M: add for op. fix issue: ALPS02093201. @{
        @Override
        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState state) {
            /// M: Support onPreciseDataConnectionStateChanged.  @{
            if (PluginFactory.getStatusBarPlugin(mContext).customizeBehaviorSet()
                    == BehaviorSet.OP01_BS) {
                mSvLteController.onPreciseDataConnectionStateChanged(state);
                mCurrentState.mShowDataActivityIcon = mSvLteController.isShowDataActivityIcon();
                notifyListenersIfNecessary();
            }
        }
        ///  @}

        @Override
        public void onDataActivity(int direction) {
            if (DEBUG) {
                Log.d(mTag, "onDataActivity: direction=" + direction);
            }
            ///M: Support SVLTE. @{
            mSvLteController.onDataActivity(direction);
            ///M: Support SVLTE. @}
            setActivity(direction);
        }

        @Override
        public void onCarrierNetworkChange(boolean active) {
            if (DEBUG) {
                Log.d(mTag, "onCarrierNetworkChange: active=" + active);
            }
            mCurrentState.carrierNetworkChangeMode = active;

            updateTelephony();
        }
    };

    static class MobileIconGroup extends SignalController.IconGroup {
        final int mDataContentDescription; // mContentDescriptionDataType
        final int mDataType;
        final boolean mIsWide;
        final int mQsDataType;

        public MobileIconGroup(String name, int[][] sbIcons, int[][] qsIcons, int[] contentDesc,
                int sbNullState, int qsNullState, int sbDiscState, int qsDiscState,
                int discContentDesc, int dataContentDesc, int dataType, boolean isWide,
                int qsDataType) {
            super(name, sbIcons, qsIcons, contentDesc, sbNullState, qsNullState, sbDiscState,
                    qsDiscState, discContentDesc);
            mDataContentDescription = dataContentDesc;
            mDataType = dataType;
            mIsWide = isWide;
            mQsDataType = qsDataType;
        }
    }

    static class MobileState extends SignalController.State {
        String networkName;
        String networkNameData;
        boolean dataSim;
        boolean dataConnected;
        boolean isEmergency;
        boolean airplaneMode;
        boolean carrierNetworkChangeMode;
        boolean isDefault;
        /// M: For network type big icon.
        int networkIcon;
        /// M: Add for op network tower type.
        int customizedState;

        @Override
        public void copyFrom(State s) {
            super.copyFrom(s);
            MobileState state = (MobileState) s;
            dataSim = state.dataSim;
            networkName = state.networkName;
            networkNameData = state.networkNameData;
            dataConnected = state.dataConnected;
            isDefault = state.isDefault;
            isEmergency = state.isEmergency;
            airplaneMode = state.airplaneMode;
            carrierNetworkChangeMode = state.carrierNetworkChangeMode;
            /// M: For network type big icon.
            networkIcon = state.networkIcon;
            /// M: Add for op network tower type.
            customizedState = state.customizedState;
        }

        @Override
        protected void toString(StringBuilder builder) {
            super.toString(builder);
            builder.append(',');
            builder.append("dataSim=").append(dataSim).append(',');
            builder.append("networkName=").append(networkName).append(',');
            builder.append("networkNameData=").append(networkNameData).append(',');
            builder.append("dataConnected=").append(dataConnected).append(',');
            builder.append("isDefault=").append(isDefault).append(',');
            builder.append("isEmergency=").append(isEmergency).append(',');
            builder.append("airplaneMode=").append(airplaneMode).append(',');
            /// M: For network type big icon.
            builder.append("networkIcon").append(networkIcon).append(',');
            /// M: Add for op network tower type.
            builder.append("customizedState=").append(customizedState).append(',');
            builder.append("carrierNetworkChangeMode=").append(carrierNetworkChangeMode);
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o)
                    && Objects.equals(((MobileState) o).networkName, networkName)
                    && Objects.equals(((MobileState) o).networkNameData, networkNameData)
                    && ((MobileState) o).dataSim == dataSim
                    && ((MobileState) o).dataConnected == dataConnected
                    && ((MobileState) o).isEmergency == isEmergency
                    && ((MobileState) o).airplaneMode == airplaneMode
                    && ((MobileState) o).carrierNetworkChangeMode == carrierNetworkChangeMode
                    /// M: For network type big icon.
                    && ((MobileState) o).networkIcon == networkIcon
                    /// M: Add for op network tower type.
                    && ((MobileState) o).customizedState == customizedState
                    && ((MobileState) o).isDefault == isDefault;
        }
    }

    /// M: Support "Operator plugin - Data activity/type, strength icon". @{
    public SubscriptionInfo getControllerSubInfo() {
        return mSubscriptionInfo;
    }

    public boolean getControllserHasService() {
        return hasService();
    }

    public boolean getControllserIsRoaming() {
        return isRoaming();
    }

    public boolean getControllserIsCdma() {
        return isCdma();
    }

    public int getControllserDataNetType() {
        return mDataNetType;
    }

    public ServiceState getControllserServiceState() {
        return mServiceState;
    }

    public int getControllserDataState() {
        return mDataState;
    }
    /// M: Support "Operator plugin - Data activity/type, strength icon". @}
}
