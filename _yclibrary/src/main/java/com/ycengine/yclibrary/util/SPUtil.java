package com.ycengine.yclibrary.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

public class SPUtil {
	public static void setSharedPreference(Context context, String key, String value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static void setSharedPreference(Context context, String key, boolean value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        /*SharedPreferences prefs = context.getSharedPreferences(Constants.SERVICE_TAG, Context.MODE_PRIVATE);*/
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

    public static void setSharedPreference(Context context, String key, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setSharedPreference(Context context, String key, float value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static void setSharedPreferencesStringSet(Context context, String key, Set<String> val) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, val);
        editor.commit();
    }

    public static String getSharedPreference(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(key, "");
	}

	public static String getSharedPreference(Context context, String key, String defaultVal) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(key, defaultVal);
	}

	public static boolean getBooleanSharedPreference(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, false);
	}

	public static boolean getBooleanSharedPreference(Context context, String key, boolean isDefault) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, isDefault);
	}

    public static int getIntSharedPreference(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(key, 0);
    }

    public static int getIntSharedPreference(Context context, String key, int isDefault) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(key, isDefault);
    }

    public static float getFloatSharedPreference(Context context, String key, float isDefault) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getFloat(key, isDefault);
    }

    public static Set<String> getSharedPreferencesStringSet(Context context, String key, Set<String> defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet(key, defaultValue);
    }

    public static void actRemoveSharedPreferences(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.commit();
	}
	
	public static void actRemoveAllSharedPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
	}
}
