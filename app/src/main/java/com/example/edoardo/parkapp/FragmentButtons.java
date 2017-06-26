package com.example.edoardo.parkapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.MODE_PRIVATE;

public class FragmentButtons extends Fragment {

    private int parkType;
    public static final int GRATUITO = 0;
    public static final int DISCO_ORARIO = 1;
    public static final int PARCHIMETRO = 2;
    public TextView park_id;
    public TextView park_description;

    private TimePicker timePicker;
    private TextView timePickerLabel;
    private Button button_save;

    private Button button_find;
    private Button button_nearby;
    private Spinner parkTypes;
    private View view;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ParkAppApplicationObject app;

    private final static long MINUTE_PER_DAY = 1440;
    private final static long MILLISECOND_PER_MINUTE = 60000;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_buttons_layout,
                container, false);

        button_save = (Button) view.findViewById(R.id.button_save);
        button_find = (Button) view.findViewById(R.id.button_find);
        //button_config = (Button) view.findViewById(R.id.button_config);
        button_nearby = (Button) view.findViewById(R.id.button_nearby_parks);


        button_save.setOnClickListener(buttonListener);
        //button_config.setOnClickListener(buttonListener);
        button_find.setOnClickListener(buttonListener);
        button_nearby.setOnClickListener(buttonListener);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);

    }

    //Finestra di configurazione Parcheggio

    public void displayOptionsDialog(){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.alert_dialog_layout,null);
        dialogBuilder.setView(dialogView);

        parkTypes = (Spinner) dialogView.findViewById(R.id.spinner_park_types);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.park_types, android.R.layout.simple_spinner_dropdown_item);

        Date date = new Date(System.currentTimeMillis());
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        timePickerLabel = (TextView) dialogView.findViewById(R.id.time_picker_label);
        parkTypes.setOnItemSelectedListener(spinnerListener);
        parkTypes.setAdapter(adapter);

        //Restore previous values of the dialog
        parkTypes.setSelection(sharedPreferences.getInt("park_type", GRATUITO));
        timePicker.setCurrentHour(sharedPreferences.getInt("end_hour", calendar.get(Calendar.HOUR_OF_DAY)));
        timePicker.setCurrentMinute(sharedPreferences.getInt("end_minute", calendar.get(Calendar.MINUTE)));


        dialogBuilder.setTitle(R.string.dialog_title);
        //////////////////////Bottoni salva o annulla/////////////////////////////////////////////////
        dialogBuilder.setPositiveButton("SALVA", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){

                long currentTimeMillis = System.currentTimeMillis();
                Date date = new Date(currentTimeMillis);
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(date);

                int beginPickerHour = calendar.get(Calendar.HOUR_OF_DAY);
                int beginPickerMinute = calendar.get(Calendar.MINUTE);

                app = (ParkAppApplicationObject) (getActivity()).getApplication();
                Intent intentService = new Intent(app, ParkAppService.class);
                app.stopService(intentService);//arresto eventuale servizio di norifica in esecuzione

                /*salvataggio ora inizio e tipo parcheggio sempre*/

                editor = sharedPreferences.edit();
                editor.putString("begin_date", Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "/"
                        + Integer.toString(calendar.get(Calendar.MONTH)+1) + "/" +
                        Integer.toString(calendar.get(Calendar.YEAR)));
                editor.putInt("begin_hour", calendar.get(Calendar.HOUR_OF_DAY));
                editor.putInt("begin_minute", calendar.get(Calendar.MINUTE));
                editor.putInt("park_type", parkType);
                editor.commit();

                if( parkType == DISCO_ORARIO || parkType == PARCHIMETRO) {

                    int timePickerHour = timePicker.getCurrentHour();
                    int timePickerMinute = timePicker.getCurrentMinute();
                    long duration = calculateDuration(timePickerHour, timePickerMinute, beginPickerHour, beginPickerMinute);
                    long endTimeMillis = currentTimeMillis + duration;//dato essenziale al servizio


                    PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
                    SharedPreferences settingsPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    int notificationType = Integer.parseInt( settingsPreferences.getString("pref_notification", "2") );
                    long notificationPeriod =  MILLISECOND_PER_MINUTE*Long.parseLong( settingsPreferences.getString("pref_period_notification", "30") );
                    boolean isRingingEnabled = settingsPreferences.getBoolean("pref_ringing_notification", true);

                    /*in questo caso aggiungo alle shared preferences anche ora fine e durata*/
                    editor = sharedPreferences.edit();
                    editor.putInt("end_hour", timePickerHour);
                    editor.putInt("end_minute", timePickerMinute);
                    editor.putLong("park_duration", duration);//per sola statistica nello storicoL
                    editor.putLong("endTimeMillis", endTimeMillis);//l'ora di fine è l'unico dato essenziale al servizio
                    editor.putBoolean("hasFirstNotificationHappened", false);
                    editor.putLong("duration", duration);
                    editor.putInt("pref_notification", notificationType);
                    editor.putLong("pref_period_notification", notificationPeriod);
                    editor.putBoolean("pref_ringing_notification", isRingingEnabled);
                    editor.commit();

                    app.startService(intentService);

                    double latitude = Double.parseDouble(sharedPreferences.getString("latitude","0"));
                    double longitude = Double.parseDouble(sharedPreferences.getString("longitude","0"));
                    Toast.makeText(getActivity(), latitude+" "+longitude, Toast.LENGTH_SHORT).show();

                }

                ((MainActivity)getActivity()).saveLocation();
                displayInfoPark();


            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////
        dialogBuilder.setNegativeButton("ANNULLA", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                //User canceleed thekfjasuifh
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id){


            if(position == GRATUITO){
                timePicker.setVisibility(View.GONE);
                timePickerLabel.setVisibility(View.GONE);

            }
            else if (position == DISCO_ORARIO) {
                timePicker.setVisibility(View.VISIBLE);
                timePickerLabel.setVisibility(View.VISIBLE);

            }
            else if (position == PARCHIMETRO) {
                timePicker.setVisibility(View.VISIBLE);
                timePickerLabel.setVisibility(View.VISIBLE);

            }

            parkType = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //
        }

    };
    public interface OnNewsfromFragment{

    }
    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.button_save:
                    if(checkExistingParks()){
                    displayOptionsDialog();
                    }
                   /* ((MainActivity)getActivity()).saveLocation();

                    displayInfoPark();
                    //.sharedPreferences = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);

                    double latitude = Double.parseDouble(sharedPreferences.getString("latitude","0"));
                    double longitude = Double.parseDouble(sharedPreferences.getString("longitude","0"));

                    Toast.makeText(getActivity(), latitude+" "+longitude, Toast.LENGTH_SHORT).show();
                    */
                    break;
                case R.id.button_find:
                    // nuova modifica///////////////////////////////////////////////////
                    sharedPreferences = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
                    int parkType = sharedPreferences.getInt("park_type", -1);
                    String destinationLatitudeString = sharedPreferences.getString("latitude","");
                    String destinationLongitudeString = sharedPreferences.getString("longitude","");
                    if(parkType != -1) {
                        if (destinationLatitudeString != null && destinationLongitudeString != null) {
                            double destinationLatitude = Double.parseDouble(destinationLatitudeString);
                            double destinationLongitude = Double.parseDouble(destinationLongitudeString);

                            Location currentLocation = ((MainActivity) getActivity()).getCurrentLocation();
                            double sourceLatitude = currentLocation.getLatitude();
                            double sourceLongitude = currentLocation.getLongitude();


                            Intent navigation = new Intent(Intent.ACTION_VIEW, Uri
                                    .parse("http://maps.google.com/maps?saddr="
                                            + sourceLatitude + ","
                                            + sourceLongitude + "&daddr="
                                            + destinationLatitude + "," + destinationLongitude));
                            startActivity(navigation);
                        }
                        ////////////////////////////////////////////////////////////////

                        SystemClock.sleep(1000);
                        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("Attenzione");
                        alertDialog.setMessage("Hai raggiunto il parcheggio?");
                        alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "SI",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ParkDB db = new ParkDB(getActivity());

                                        sharedPreferences = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
                                        int begin_time_hour = sharedPreferences.getInt("begin_hour", -1);
                                        int begin_time_minute = sharedPreferences.getInt("begin_minute", -1);
                                        String begin_date = sharedPreferences.getString("begin_date", " ");
                                        int parkType = sharedPreferences.getInt("park_type", -1);
                                        int end_time_hour = sharedPreferences.getInt("end_hour", -1);
                                        int end_time_minute = sharedPreferences.getInt("end_minute", -1);

                                        String indirizzo_db = sharedPreferences.getString("citta", " ") + ", " +
                                                sharedPreferences.getString("indirizzo", "");
                                        String parktype_db = intToParkType(parkType);
                                        String date_db = begin_date;
                                        String orainizio_db = Integer.toString(begin_time_hour) + ":" + Integer.toString(begin_time_minute);
                                        String orafine_db;
                                        if(parkType!=0)
                                            orafine_db = Integer.toString(end_time_hour) + ":" + Integer.toString(end_time_minute);
                                        else
                                            orafine_db="/";

                                        Park park = new Park(indirizzo_db,parktype_db,date_db,orainizio_db,orafine_db);
                                        db.insertPark(park);

                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.clear().commit();
                                        ((MainActivity)getActivity()).deleteMarker();
                                        ((MainActivity)getActivity()).hideButtons();
                                        dialog.dismiss();

                                        //arresto il servizio
                                        app = (ParkAppApplicationObject) (getActivity()).getApplication();
                                        Intent intentservice = new Intent(app, ParkAppService.class);
                                        app.stopService(intentservice);


                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                    }
                                });
                        alertDialog.show();
                    }
                    else{
                        Toast.makeText(getActivity(), "Devi prima salvare il parcheggio", Toast.LENGTH_SHORT).show();
                    }
                    break;
               /* case R.id.button_config:
                    displayOptionsDialog();
                    break;*/
                case R.id.button_nearby_parks:

                    ((MainActivity)getActivity()).showNearbParks();

                    Toast.makeText(getActivity(), "Hai premuto NEARBY", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public String intToParkType(int parkType){
        switch (parkType){
            case GRATUITO: {
                return "Gratuito";
            }
            case DISCO_ORARIO:{
                return "Disco Orario";
            }
            case PARCHIMETRO: {
                return "Parchimetro";
            }
            default:return "Gratuito";
        }
    }

    public boolean checkExistingParks(){
        sharedPreferences = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        int parkType = sharedPreferences.getInt("park_type", -1);
        if(parkType == -1) {
            return true;
        }
        else{

            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Attenzione");
            alertDialog.setMessage("Cliccando su procedi sovrascriverai il parcheggio precedente");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Procedi",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            displayOptionsDialog();
                        }
                    });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Annulla",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

        }
        return false;
    }

    public void displayInfoPark(){

        sharedPreferences = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        int begin_time_hour = sharedPreferences.getInt("begin_hour", -1);
        int begin_time_minute = sharedPreferences.getInt("begin_minute", -1);
        String begin_date = sharedPreferences.getString("begin_date", " ");
        int parkType = sharedPreferences.getInt("park_type", -1);
        int end_time_hour = sharedPreferences.getInt("end_hour", -1);
        int end_time_minute = sharedPreferences.getInt("end_minute", -1);
        String indirizzo = sharedPreferences.getString("citta", " ") + "\n" +
                sharedPreferences.getString("indirizzo", "") + "\n";

        park_id = (TextView) getActivity().findViewById(R.id.park_id_textview);
        park_id.setText("Il tuo parcheggio");
        park_description = (TextView) getActivity().findViewById(R.id.park_description_textview);

        String parkDescriptionText =  indirizzo;
        parkDescriptionText += intToParkType(parkType) + "\n";
        Toast.makeText(getActivity(), Integer.toString(R.id.park_description_textview), Toast.LENGTH_SHORT).show();

        String beginHourString = HistoryActivity.getXXFormat( begin_time_hour );
        String beginMinuteString = HistoryActivity.getXXFormat( begin_time_minute );
        String endHourString = HistoryActivity.getXXFormat( end_time_hour );
        String endMinuteString = HistoryActivity.getXXFormat( end_time_minute );

        parkDescriptionText += "Data: " + begin_date + "\n";
        parkDescriptionText += "Ora inizio: " + beginHourString + ":" + beginMinuteString + "\n";

        if(parkType != GRATUITO) {
            parkDescriptionText +=  "Ora fine : " + endHourString + ":" + endMinuteString + "\n";
        }
        park_description.setText(parkDescriptionText);


    }
    @Override
   public void onResume() {
        super.onResume();
    }

    private long calculateDuration(long endHour, long endMinute, long beginHour, long beginMinute) {

        long endMinuteFrom00 = endHour*60 + endMinute;
        long beginMinuteFrom00 = beginHour*60 + beginMinute;

        long duration = (endMinuteFrom00 - beginMinuteFrom00 + MINUTE_PER_DAY) % MINUTE_PER_DAY;

        if(duration == 0) {
            duration = MINUTE_PER_DAY;
        }
        duration = duration * MILLISECOND_PER_MINUTE;
        return duration;

    }



}
