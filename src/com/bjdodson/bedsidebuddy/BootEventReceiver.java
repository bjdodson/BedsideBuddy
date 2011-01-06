package com.bjdodson.bedsidebuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootEventReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		BedsideBuddy.resetTimers(context);
	}
}
