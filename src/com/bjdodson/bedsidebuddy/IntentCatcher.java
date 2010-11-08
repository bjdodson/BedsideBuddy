package com.bjdodson.bedsidebuddy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.util.Log;

public class IntentCatcher extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = context.getSharedPreferences("main", 0);
		String action = prefs.getString("action", "nothing");
		String trigger = prefs.getString("trigger", "nothing");
		
		if ("nothing".equals(action)){
			return;
		}
		
		String event = intent.getAction();
		
		if ("desk_dock".equals(trigger)) {
			if (Intent.ACTION_DOCK_EVENT.equals(event)) {
				int mode = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
				if (mode == Intent.EXTRA_DOCK_STATE_DESK) {
					dataOff(context, action);
				} else if (mode == Intent.EXTRA_DOCK_STATE_UNDOCKED) {
					dataOn(context, action);
				}
			}
		}
		
		if ("power".equals(trigger)) {
			if (Intent.ACTION_POWER_CONNECTED.equals(event)) {
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
}