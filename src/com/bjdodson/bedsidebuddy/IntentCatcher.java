package com.bjdodson.bedsidebuddy;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.Preference;
import android.preference.TimePickerPreference;
import android.provider.Settings;
import android.util.Log;

public class IntentCatcher extends BroadcastReceiver {
	SharedPreferences prefs;
	Context context;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		prefs = context.getSharedPreferences("main", 0);
		this.context = context;
		
		String action = prefs.getString("action", "nothing");
		String trigger = prefs.getString("trigger", "nothing");

		if ("nothing".equals(action)){
			return;
		}
		
		String event = intent.getAction();
		
		if ("desk_dock".equals(trigger)) {
			if (Intent.ACTION_DOCK_EVENT.equals(event)) {
				int mode = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
				if (mode == Intent.EXTRA_DOCK_STATE_DESK && timeConditionHolds()) {
					dataOff(context, action);
				} else if (mode == Intent.EXTRA_DOCK_STATE_UNDOCKED) {
					dataOn(context, action);
				}
			}
		}
		
		if ("power".equals(trigger)) {
			if (Intent.ACTION_POWER_CONNECTED.equals(event) && timeConditionHolds()) {
				dataOff(context, action);
			} else if (Intent.ACTION_POWER_DISCONNECTED.equals(event)) {
				dataOn(context, action);
			}
		}
	}
	
	private void dataOff(Context context, String action) {
		if ("airplane".equals(action)) {
			setAirplaneMode(context, true);
		} else if ("datasync".equals(action)) {
			turnDataSyncOff(context);
		}
	}
	
	private void dataOn(Context context, String action) {
		if ("airplane".equals(action)) {
			setAirplaneMode(context, false);
		} else if ("datasync".equals(action)) {
			turnDataSyncOn(context);
		}
	}
	
	private void turnDataSyncOff(Context context) {
		boolean sync = ContentResolver.getMasterSyncAutomatically();
		if (sync) {
			ContentResolver.setMasterSyncAutomatically(false);
		}
	}
	
	private void turnDataSyncOn(Context context) {
		ConnectivityManager connManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean backgroundData = connManager.getBackgroundDataSetting();
		boolean sync = ContentResolver.getMasterSyncAutomatically();
        
        if (!backgroundData) {
            // connManager.setBackgroundDataSetting(true);
        	Log.w("bedside", "Background data is disabled.");
        }

        if (!sync) {
        	ContentResolver.setMasterSyncAutomatically(true);
        }
	}
	
	/**
     * Gets the state of background data.
     *
     * @param context
     * @return true if enabled
     */
    private static boolean getBackgroundDataState(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getBackgroundDataSetting();
    }

    private void setAirplaneMode(Context context, boolean enabling) {
        // Change the system setting
        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 
                                enabling ? 1 : 0);
        
        // Post the intent
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
        context.sendBroadcast(intent);
    }
    
    /**
     * Verifies that the time is within the user's specified bound.
     * @return
     */
    private boolean timeConditionHolds() {
    	String condition = prefs.getString("condition", "always");
    	if (condition.equals("always")) {
    		return true;
    	}
    	
    	Calendar current = Calendar.getInstance();
    	int curMins = current.get(Calendar.HOUR_OF_DAY)*60 + current.get(Calendar.MINUTE);
    	
    	String start = prefs.getString("starttime", null);
    	String end = prefs.getString("endtime", null);
    	if (start == null || end == null) {
    		return true;
    	}
    	
    	int startMins = timeInMinutes(start);
    	int endMins = timeInMinutes(end);
    	
    	
    	if (startMins < endMins) {
    		// standard stuff.
    		boolean between = (curMins >= startMins && curMins <= endMins);
    		return (between ^ condition.equals("except"));
    	} else {
    		boolean between = (curMins >= endMins && curMins <= startMins);
    		return (between ^ condition.equals("only"));
    	}
    }
    
    /**
     * Returns the number of minutes since 12AM.
     * @param rep
     * @return
     */
    private int timeInMinutes(String time) {
    	int t = 60 * Integer.valueOf(time.split(":")[0]);
    	t += Integer.valueOf(time.split(":")[1]);
    	return t;
    }
}