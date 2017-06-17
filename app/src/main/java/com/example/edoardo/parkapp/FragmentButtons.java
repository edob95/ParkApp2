package com.example.edoardo.parkapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
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
    //private Button button_config;
    private Button button_find;
    private Button button_nearby;
    private Spinner parkTypes;
    private View view;
    private SharedPreferences sharedPreferences;
    private SharedPreferences savedValues;
    private SharedPreferences.Editor editor;

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

        savedValues = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);

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
        parkTypes.setSelection(savedValues.getInt("park_type", GRATUITO));
        timePicker.setCurrentHour(savedValues.getInt("end_hour", calendar.get(Calendar.HOUR)));
        timePicker.setCurrentMinute(savedValues.getInt("end_minute", calendar.get(Calendar.MINUTE)));


        dialogBuilder.setTitle(R.string.dialog_title);
        //////////////////////Bottoni salva o annulla/////////////////////////////////////////////////
        dialogBuilder.setPositiveButton("SALVA", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){

                Date date = new Date(System.currentTimeMillis());
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(date);
             /*   Toast.makeText(getActivity(),timePicker.getCurrentHour() + ":"
                        + timePicker.getCurrentMinute(), Toast.LENGTH_SHORT).show();*/
                editor = savedValues.edit();

                //SALVATAGGIO ORE E MINUTI

                editor.putString("begin_date", Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + " "
                        + Integer.toString(calendar.get(Calendar.MONTH)+1) + " " +
                        Integer.toString(calendar.get(Calendar.YEAR)));
                editor.putInt("begin_hour", calendar.get(Calendar.HOUR_OF_DAY));
                editor.putInt("begin_minute", calendar.get(Calendar.MINUTE));

                switch (parkType){
                    case DISCO_ORARIO:
                        editor.putInt("end_hour", timePicker.getCurrentHour());
                        editor.putInt("end_minute", timePicker.getCurrentMinute());
                        editor.putInt("park_type", DISCO_ORARIO);
                        break;

                    case PARCHIMETRO:
                        editor.putInt("end_hour", timePicker.getCurrentHour());
                        editor.putInt("end_minute", timePicker.getCurrentMinute());
                        editor.putInt("park_type", PARCHIMETRO);
                        break;

                    case GRATUITO:
                        editor.putInt("park_type", GRATUITO);
                        break;

                    default:
                        editor.putInt("park_type", GRATUITO);
                }

                editor.commit();

                ((MainActivity)getActivity()).saveLocation();

                displayInfoPark();
                //.savedValues = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);

                double latitude = Double.parseDouble(savedValues.getString("latitude","0"));
                double longitude = Double.parseDouble(savedValues.getString("longitude","0"));

                Toast.makeText(getActivity(), latitude+" "+longitude, Toast.LENGTH_SHORT).show();



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
                    displayOptionsDialog();
                   /* ((MainActivity)getActivity()).saveLocation();

                    displayInfoPark();
                    //.savedValues = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);

                    double latitude = Double.parseDouble(savedValues.getString("latitude","0"));
                    double longitude = Double.parseDouble(savedValues.getString("longitude","0"));

                    Toast.makeText(getActivity(), latitude+" "+longitude, Toast.LENGTH_SHORT).show();
                    */
                    break;
                case R.id.button_find:
                    Toast.makeText(getActivity(), "Hai premuto FIND", Toast.LENGTH_SHORT).show();
                    break;
               /* case R.id.button_config:
                    displayOptionsDialog();
                    break;*/
                case R.id.button_nearby_parks:
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

    public void displayInfoPark(){

        sharedPreferences = getActivity().getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        int begin_time_hour = sharedPreferences.getInt("begin_hour", 0);
        int begin_time_minute = sharedPreferences.getInt("begin_minute", 0);
        String begin_date = sharedPreferences.getString("begin_date", " ");
        int parkType = sharedPreferences.getInt("park_type", 0);
        int end_time_hour = sharedPreferences.getInt("end_hour", 0);
        int end_time_minute = sharedPreferences.getInt("end_minute", 0);


        park_id = (TextView) getActivity().findViewById(R.id.park_id_textview);
        park_id.setText("Il tuo parcheggio");
        park_description = (TextView) getActivity().findViewById(R.id.park_description_textview);
        String parkDescriptionText = intToParkType(parkType) + "\n";
        Toast.makeText(getActivity(), Integer.toString(R.id.park_description_textview), Toast.LENGTH_SHORT).show();

            parkDescriptionText += "Data: " + begin_date + "\n" + "Ora inizio: " + begin_time_hour
                    + ":" + begin_time_minute + "\n";
        if(parkType != GRATUITO) {
            parkDescriptionText +=  "Ora fine : " + end_time_hour + ":" + end_time_minute + "\n";
        }
        park_description.setText(parkDescriptionText);


    }
    @Override
   public void onResume() {
        super.onResume();
    }
}
