package com.example.musicsyncer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.preferences);



        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (s.equals("ip")){
                    Preference ip_pref = findPreference(s);
                    ip_pref.setSummary(sharedPreferences.getString(s,""));
                }
                if (s.equals("webroot")){
                    Preference webroot_pref = findPreference(s);
                    webroot_pref.setSummary(sharedPreferences.getString(s,""));
                }

            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        Preference webroot_pref = findPreference("webroot");
        webroot_pref.setSummary(getPreferenceScreen().getSharedPreferences().getString("webroot",""));

        Preference ip_pref = findPreference("ip");
        ip_pref.setSummary(getPreferenceScreen().getSharedPreferences().getString("ip",""));
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);

    }
}
