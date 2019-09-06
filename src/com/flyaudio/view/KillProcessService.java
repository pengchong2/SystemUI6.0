package com.flyaudio.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.os.UserHandle;

import com.flyaudio.entities.FlyAudioProcess;
import com.flyaudio.utils.Flog;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class KillProcessService extends Service {
	private final String TAG = "KillProcessService";
	private Context mContext = null;
	private boolean showWaring = false;
	private Object object = new Object();
	private String fly_car_status = null;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
	}

	@Override
	public void onDestroy() {
		Flog.d(TAG, "KillProcessService onDestroy ");
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Flog.d(TAG, "KillProcessService onStartCommand ");
		fly_car_status = intent.getStringExtra("fly_car_status");
		showWaring = isNeedShowWaring();
		if ("driving".equals(fly_car_status)) {
			if (showWaring)
				toHome(mContext);
			Timer timer = new Timer();
			timer.schedule(KillTimerTask(fly_car_status, showWaring), 200);
		}
		StopService();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public TimerTask KillTimerTask(String status, final boolean show) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				killVideoProcess(fly_car_status, show);
			}
		};
		return task;
	}

	public void killVideoProcess(String status, boolean showWaring) {
		Flog.d(TAG, "killVideoProcess===start");
		boolean flag = false;
		List<String> applicationPackList = getApplicationPackName();
		if (!applicationPackList.isEmpty() && applicationPackList != null) {
			ActivityManager mActivityManager = (ActivityManager) mContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (String applicationPack : applicationPackList) {

				mActivityManager.forceStopPackage(applicationPack);
				Log.d(TAG, "killVideoProcess: kill>>>>>:"+applicationPack);
				if (showWaring && !flag) {
					Flog.d(TAG, "show diaFlog");
					//FlyAudioProcess.showWaringDialog(mContext);
					flag = true;
				}
			}
		}
	}

	private void toHome(Context context) {
		Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
		mHomeIntent.addCategory(Intent.CATEGORY_HOME);
		mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		context.startActivity(mHomeIntent);
	}

	public boolean isNeedShowWaring() {
		if (!isInAppWhiteList(getCurrentTask().getPackageName()))
			return true;
		else
			return false;
	}

	public ComponentName getCurrentTask() {
		ActivityManager manager = (ActivityManager) mContext
				.getSystemService(Service.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
		if (runningTaskInfos != null&&runningTaskInfos.size()>0) {
			return runningTaskInfos.get(0).topActivity;
		}
		return null;
	}

	public static boolean isInAppWhiteList(String packageName) {
		Log.d("yaoyuqing", "isInAppWhiteList: FlyAudioProcess.mLists:"+FlyAudioProcess.mLists.size());

		for (String string : FlyAudioProcess.mLists) {
			if (packageName.equals(string)) {
				Log.d("yaoyuqing", "isInAppWhiteList:>> "+string);
				return true;
			}
		}

		return false;
	}
    private String getDefaultName(){
    	return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
    }
	
	public List<String> getApplicationPackName() {
		final PackageManager mPm = mContext.getPackageManager();
		final int mRetrieveFlags;
		if (UserHandle.myUserId() == 0) {
			mRetrieveFlags = PackageManager.GET_UNINSTALLED_PACKAGES
					| PackageManager.GET_DISABLED_COMPONENTS
					| PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS;
		} else {
			mRetrieveFlags = PackageManager.GET_DISABLED_COMPONENTS
					| PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS;
		}

		List<ApplicationInfo> mApplications = new ArrayList<ApplicationInfo>();
		List<String> applicationPackNameList = new ArrayList<String>();
		mApplications = mPm.getInstalledApplications(mRetrieveFlags);
		List<ApplicationInfo> apps;
		String mInputMethod=getDefaultName();
		Flog.d(TAG, "getApplicationPackName mInputMethod = " + mInputMethod);
		String mDefaultInputMethod = null;
		try {
			if (TextUtils.isEmpty(mInputMethod)) {
				mDefaultInputMethod = mInputMethod.substring(0, mInputMethod.lastIndexOf("/"));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		synchronized (object) {
			apps = new ArrayList<ApplicationInfo>(mApplications);
		}
		for (int i = 0; i < apps.size(); i++) {
			ApplicationInfo info = apps.get(i);
			String packageName = info.packageName;
			Flog.d(TAG, "getApplicationPackName packageName : "
					+ packageName+"/>");
			if (!FlyAudioProcess.mLists.contains(packageName)) {
				
				if (!TextUtils.isEmpty(mDefaultInputMethod) && mDefaultInputMethod.equals(packageName)) {
					Flog.d(TAG, " system mDefaultInputMethod= : " + packageName);
				}else {
					Flog.d(TAG, "force killed packageName= : " + packageName);
					applicationPackNameList.add(packageName);
				}
			}
		}

		return applicationPackNameList;
		// end
	}
	
	private void StopService() {
		KillProcessService.this.stopSelf();
    }

}

