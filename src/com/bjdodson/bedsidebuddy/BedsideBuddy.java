package com.bjdodson.bedsidebuddy;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class BedsideBuddy extends PreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getPreferenceManager().setSharedPreferencesName("main");
		addPreferencesFromResource(R.layout.preferences);
    }
}