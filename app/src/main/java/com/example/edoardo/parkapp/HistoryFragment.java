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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryFragment extends PreferenceFragment {
    //costruisco una classe esterna che una volta istanziata permette all'atto della creazione di
    //caricare il file delle preferenze a partire dal file xml
    private View view;

    public String ids;
    public String types;
    public String dates;
    public String begins;
    public String ends;

    public TextView firstColoumn;
    public TextView secondColoumn;
    public TextView thirdColoumn;
    public TextView fourthColoumn;
    public TextView fifthColoumn;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        view = inflater.inflate(R.layout.history_fragment_layout,
                container, false);
        firstColoumn = (TextView) view.findViewById(R.id.textview_db_id);
        secondColoumn = (TextView) view.findViewById(R.id.textview_db_parktype);
        thirdColoumn = (TextView) view.findViewById(R.id.textview_db_date);
        fourthColoumn =(TextView) view.findViewById(R.id.textview_db_orainizio);
        fifthColoumn =(TextView) view.findViewById(R.id.textview_db_orafine);

        firstColoumn.setText(ids);
        secondColoumn.setText(types);
        thirdColoumn.setText(dates);
        fourthColoumn.setText(begins);
        fifthColoumn.setText(ends);

        return view;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParkDB db = new ParkDB(getActivity());
        StringBuilder sbid = new StringBuilder();
        StringBuilder sbtype = new StringBuilder();
        StringBuilder sbdate = new StringBuilder();
        StringBuilder sbbegin = new StringBuilder();
        StringBuilder sbend = new StringBuilder();
        Park park = new Park(1, "null","null","null","null");
        long insertId = db.insertPark(park);
        if(insertId > 0) {
            sbid.append("Row inserted! Insert Id: " + insertId + "\n");
        }
        ArrayList<Park> parks = db.getParks();
        for (Park p : parks){
            sbid.append(p.getPark_id() + "\n");
            sbtype.append(p.getPark_type() + "\n");
            sbdate.append(p.getDate() + "\n");
            sbbegin.append(p.getOra_inizio() + "\n");
            sbend.append(p.getOra_fine() + "\n");
        }
        ids=sbid.toString();
        types=sbtype.toString();
        dates=sbdate.toString();
        begins=sbbegin.toString();
        ends=sbend.toString();

    }
}
