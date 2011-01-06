package com.bjdodson.bedsidebuddy;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class BedsideBuddy extends PreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getPreferenceManager().setSharedPreferencesName("main");
		addPreferencesFromResource(R.layout.preferences);	
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
    		Preference preference) {
    	if ("trigger".equals(preference.getKey())) {
    		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if ("time_event".equals(newValue)) {
						setTimers(BedsideBuddy.this);
					} else {
						clearTimers(BedsideBuddy.this);
					}
					return true;
				}
			});
    	}
    	
    	else if ("condition_screen".equals(preferenceScreen.getKey())) {
    		if ("time_event".equals(getSharedPreferences("main", 0).getString("trigger", ""))) {
    			preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						setTimers(BedsideBuddy.this, preference, newValue);
						return true;
					}
				});
    		}
    	}
    	
    	return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    private static void setTimers(Context ctx) {
    	setTimers(ctx, null, null);
    }
    
    private static void setTimers(Context ctx, Preference justChanged, Object newValue) {
    	AlarmManager alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
    	String startTimeString = ctx.getSharedPreferences("main", 0).getString("starttime", "12:00"); 
    	String endTimeString = ctx.getSharedPreferences("main", 0).getString("endtime", "12:00");
    	
    	if (justChanged != null) {
    		if ("starttime".equals(justChanged.getKey())) {
    			startTimeString = (String)newValue;
    		} else if ("endtime".equals(justChanged.getKey())) {
    			endTimeString = (String)newValue;
    		}
    	}

    	alarmManager.set(AlarmManager.RTC_WAKEUP, stringTimeToMs(startTimeString), getStartTimeIntent(ctx));
    	alarmManager.set(AlarmManager.RTC_WAKEUP, stringTimeToMs(endTimeString), getStopTimeIntent(ctx));
    }
    
    private static void clearTimers(Context c) {
    	AlarmManager alarmManager = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    	alarmManager.cancel(getStartTimeIntent(c));
    	alarmManager.cancel(getStopTimeIntent(c));
    }
    
    public static void resetTimers(Context c) {
    	if ("time_event".equals(c.getSharedPreferences("main", 0).getString("trigger", ""))) {
    		clearTimers(c);
    		setTimers(c, null, null);
    	}
    }
    
    public static PendingIntent getStartTimeIntent(Context ctx) {
    	Intent wakeup = new Intent(ctx, IntentCatcher.class);
		wakeup.setAction(IntentCatcher.ACTION_START_TIME);
		return PendingIntent.getBroadcast(ctx, 0, wakeup, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    public static PendingIntent getStopTimeIntent(Context ctx) {
    	Intent wakeup = new Intent(ctx, IntentCatcher.class);
		wakeup.setAction(IntentCatcher.ACTION_STOP_TIME);
		return PendingIntent.getBroadcast(ctx, 0, wakeup, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    public static long stringTimeToMs(String rep) {
    	String[] parts = rep.split(":");
    	Calendar alarm = Calendar.getInstance();
    	alarm.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
    	alarm.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
    	
    	Calendar current = Calendar.getInstance();
    	if (alarm.before(current)) {
    		alarm.set(Calendar.DAY_OF_YEAR, alarm.get(Calendar.DAY_OF_YEAR)+1);
    	}
    	return alarm.getTimeInMillis();
    }
}