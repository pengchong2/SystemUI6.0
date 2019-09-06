package com.flyaudio.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

public class FlyStorageManager {
	
	private static final String TAG = "FlyStorageManager";
	private static Context mContext;
	
	public static void init(Context context){
		mContext = context;
	}
	
	/**
	 * 获取系统中已经挂载的所有存储设备路径
	 * @return
	 */
	public static List<String> getMountedVolumePaths(){
		ArrayList<String> mountedVolumes = new ArrayList<String>();
		List<String> volumes = getVolumePaths();
		for(String volume : volumes){
			if(getVolumeState(volume).equals("mounted")){
				mountedVolumes.add(volume);
			}
		}
		return mountedVolumes;
	}
	
	
	public static String getMountedUsbVolumePath(){
		ArrayList<String> mountedVolumes = new ArrayList<String>();
		List<String> volumes = getVolumePaths();
		for(String volume : volumes){
			if(getVolumeState(volume).equals("mounted")
					&&!volume.equals("/storage/emulated/0")
					&&!volume.equals("/storage/sdcard1")
					&&!volume.equals("/storage/ext_sdcard1")){
				return volume;
			}
		}
		return "";
	}
	
	/**
	 * 获取系统中的存储设备路径
	 * @return
	 */
	public static List<String> getVolumePaths() {
		boolean bException = false;
		StorageManager mStorageManager;
		Method mMethod;

		try {
			mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
			mMethod = mStorageManager.getClass().getMethod("getVolumePaths");
			return Arrays.asList((String[]) mMethod.invoke(mStorageManager));

		} catch (NoSuchMethodException e) {
			bException = true;
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			bException = true;
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			bException =true;
			e.printStackTrace();
			
		} catch (InvocationTargetException e) {
			bException =true;
			e.printStackTrace();
		} finally{
			if(bException){
				return new ArrayList<String>();
			}
		}
		return new ArrayList<String>();
	}
	
	/**
	 * 获取存储设备的挂载状态
	 * @param mountPoint 设备路径
	 * @return 字符串：mounted，unmounted等。
	 */
	public static String getVolumeState(String mountPoint) {
		boolean bException = false;
		StorageManager storageManager = (StorageManager) mContext.getSystemService(Activity.STORAGE_SERVICE);
		try {
			Class<?> pTypes = Class.forName("java.lang.String");
			return (String) storageManager.getClass().getMethod("getVolumeState",pTypes).invoke(storageManager, mountPoint);
			
		} catch (NoSuchMethodException e) {
			bException = true;
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			bException = true;
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			bException = true;
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			bException = true;
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			bException = true;
			e.printStackTrace();
		} finally{
			if(bException){
				return "";
			}
		}
		return "";
	}
}
