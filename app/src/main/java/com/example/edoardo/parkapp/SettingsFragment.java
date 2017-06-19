package com.example.edoardo.parkapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    //costruisco una classe esterna che una volta istanziata permette all'atto della creazione di
    //caricare il file delle preferenze a partire dal file xml
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //getActivity().setContentView(R.layout.settings_activity);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

    }
}