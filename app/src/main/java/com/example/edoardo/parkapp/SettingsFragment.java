package com.example.edoardo.parkapp;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    //costruisco una classe esterna che una volta istanziata permette all'atto della creazione di
    //caricare il file delle preferenze a partire dal file xml
    public static  final int NO_NOTIFICATION = 0;
    public static  final int SINGLE_NOTIFICATION = 1;
    public static  final int DOUBLE_NOTIFICATION = 2;
    ListPreference prefNotification;
    ListPreference prefPeriodNotification;
    CheckBoxPreference prefRingingNotification;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        prefNotification = (ListPreference)findPreference("pref_notification");
        prefPeriodNotification = (ListPreference)findPreference("pref_period_notification");
        prefRingingNotification = (CheckBoxPreference) findPreference("pref_ringing_notification");



        prefNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                                                           public boolean onPreferenceChange(Preference preference, Object newValue) {
                                                               final String val = newValue.toString();
                                                               int index = prefNotification.findIndexOfValue(val);
                                                               if(index==NO_NOTIFICATION) {
                                                                   prefPeriodNotification.setEnabled(false);
                                                                   prefRingingNotification.setEnabled(false);
                                                               }
                                                               else if( index == SINGLE_NOTIFICATION){
                                                                   prefPeriodNotification.setEnabled(true);
                                                                   prefRingingNotification.setEnabled(false);
                                                               } else {
                                                                   prefPeriodNotification.setEnabled(true);
                                                                   prefRingingNotification.setEnabled(true);
                                                               }
                                                               return true;
                                                           }
                                                       });

    }

    @Override
    public void onResume() {
        super.onResume();
        prefNotification = (ListPreference)findPreference("pref_notification");
        prefPeriodNotification = (ListPreference)findPreference("pref_period_notification");
        prefRingingNotification = (CheckBoxPreference) findPreference("pref_ringing_notification");
        int notificationType = Integer.parseInt(prefNotification.getValue());

        if(notificationType==NO_NOTIFICATION) {
            prefPeriodNotification.setEnabled(false);
            prefRingingNotification.setEnabled(false);
        } else if( notificationType == SINGLE_NOTIFICATION) {
            prefPeriodNotification.setEnabled(true);
            prefRingingNotification.setEnabled(false);
        } else {
            prefPeriodNotification.setEnabled(true);
            prefRingingNotification.setEnabled(true);
        }
    }
}