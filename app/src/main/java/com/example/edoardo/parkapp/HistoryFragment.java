package com.example.edoardo.parkapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryFragment extends PreferenceFragment {
    //costruisco una classe esterna che una volta istanziata permette all'atto della creazione di
    //caricare il file delle preferenze a partire dal file xml
    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        view = inflater.inflate(R.layout.history_fragment_layout,
                container, false);

        ParkDB db = new ParkDB(getActivity());

        ArrayList<Park> parks = db.getParks();
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (Park p : parks){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("dateandaddress", p.getDate() + "    " + p.getIndirizzo());
            map.put("typeandduration", p.getPark_type() + "    " + p.getOra_inizio() + " - " + p.getOra_fine());
            data.add(map);
        }
        int resource = R.layout.listview_item;
        String[] from = {"dateandaddress", "typeandduration"};
        int[] to = {R.id.parkDateandAddressTextView, R.id.parkTypeandDurationTextView};


        SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, resource, from, to);
        ListView saveListView = (ListView)view.findViewById(R.id.historyListView);
        saveListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
