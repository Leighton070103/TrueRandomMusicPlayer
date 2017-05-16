package net.classicgarage.truerandommusicplayer.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import net.classicgarage.truerandommusicplayer.R;


public class PrefsActivity extends PreferenceActivity { 
// implements OnClickListener {
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.layout.prefs);

	}


}
