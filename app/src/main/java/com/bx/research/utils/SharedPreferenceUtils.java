package com.bx.research.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bx.research.MainApplication;
import com.bx.research.constants.ConstantsData;

import java.util.Map;

public class SharedPreferenceUtils {

	private static SharedPreferences mPreferences;

	private static SharedPreferences getSharedPreferences(String fileName) {
		if (mPreferences == null) {
			mPreferences = MainApplication.getInstance().getSharedPreferences(fileName, Context.MODE_PRIVATE);
		}
		return mPreferences;
	}

	/**
	 * 存储单条数据到默认SP文件
	 * 
	 * @param key
	 * @param value
	 */
	public static void putString(String key, String value) {
		getSharedPreferences(ConstantsData.SP_FILE_NAME).edit().putString(key, value).commit();
	}

	/**
	 * 从默认SP文件中获取单条数据
	 * 
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		return getSharedPreferences(ConstantsData.SP_FILE_NAME).getString(key, "");
	}

	/**
	 * 存储整个map数据到指定SP文件
	 * 
	 * @param context
	 * @param map
	 * @param fileName
	 * @return
	 */
	public static boolean saveDataToPreference(Context context, Map<String, Object> map, String fileName) {
		boolean flag = false;
		SharedPreferences.Editor editor = getSharedPreferences(fileName).edit();
		if (map != null && !map.isEmpty()) {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				Object object = entry.getValue();
				if (object instanceof Integer) {
					Integer integer = (Integer) object;
					editor.putInt(entry.getKey(), integer);
				} else if (object instanceof Long) {
					Long l = (Long) object;
					editor.putLong(entry.getKey(), l);
				} else if (object instanceof String) {
					String str = (String) object;
					editor.putString(entry.getKey(), str);
				} else if (object instanceof Float) {
					Float f = (Float) object;
					editor.putFloat(entry.getKey(), f);
				} else if (object instanceof Boolean) {
					Boolean bool = (Boolean) object;
					editor.putBoolean(entry.getKey(), bool);
				}
			}
		}
		flag = editor.commit();
		return flag;
	}

	/**
	 * 获取指定SP文件中所有数据
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Map<String, ?> getDataFromPreference(Context context, String fileName) {
		Map<String, ?> map = null;
		SharedPreferences preferences = getSharedPreferences(fileName);
		map = preferences.getAll();
		return map;
	}

	/**
	 * 指定SP文件中获取单条数据
	 * 
	 * @param context
	 * @param fileName
	 * @param key
	 * @return
	 */

	public static String getSingleStringDataFromPreference(Context context, String fileName, String key) {
		String valueString = null;
		SharedPreferences preferences = getSharedPreferences(fileName);
		valueString = preferences.getString(key, "");
		return valueString;
	}

	/**
	 * 存储单条数据到指定SP文件
	 * 
	 * @param context
	 * @param fileName
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean saveSingleStringDatatoPreference(Context context, String fileName, String key, String value) {
		boolean flag = false;
		SharedPreferences preferences = getSharedPreferences(fileName);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		flag = editor.commit();
		return flag;
	}

	/**
	 * 清除指定SP文件中所有内容
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Boolean clearFileContent(Context context, String fileName) {
		boolean flag = false;
		SharedPreferences preferences = getSharedPreferences(fileName);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		flag = editor.commit();
		return flag;
	}

	/**
	 * 清除指定SP文件中某条数据
	 * 
	 * @param context
	 * @param fileName
	 * @param key
	 * @return
	 */
	public static Boolean removeOneData(Context context, String fileName, String key) {
		boolean flag = false;
		SharedPreferences preferences = getSharedPreferences(fileName);
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove(key);
		flag = editor.commit();
		return flag;
	}
}
