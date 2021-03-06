package com.example.edoardo.parkapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static com.example.edoardo.parkapp.R.id.add;
import static com.example.edoardo.parkapp.R.id.map;
import static com.example.edoardo.parkapp.R.id.start;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private int PROXIMITY_RADIUS = 5000;
    private TextView park_id;
    private TextView park_description;

    //provvisorio
    private Button park_delete_button;

    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    // Parte di update location
    public static final int UPDATE_INTERVAL = 1000;
    public static final int FASTEST_UPDATE_INTERVAL = 2000;
    private LocationRequest locationRequest;
    private Marker currentPositionMarker;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient googleApiClient;
    public GoogleMap mMap;

    private Location mLocation;
    private SharedPreferences sharedPreferences;
    private ParkAppApplicationObject app;


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // new notificationThread().execute();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*---------------------Mappa------------------*/

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);

        currentPositionMarker = null;


        //Provvisorio
        sharedPreferences = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        final View header = navigationView.getHeaderView(0);
        park_id = (TextView) header.findViewById(R.id.park_id_textview);
        park_description = (TextView) header.findViewById(R.id.park_description_textview);
        park_delete_button = (Button) header.findViewById(R.id.park_delete_button);
        hideButtons();
        park_delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                alertDialog.setTitle("Attenzione");
                alertDialog.setMessage("Vuoi eliminare il parcheggio?");
                alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "SI",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ParkDB db = new ParkDB(MainActivity.this);

                                sharedPreferences = MainActivity.this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
                                int begin_time_hour = sharedPreferences.getInt("begin_hour", 0);
                                int begin_time_minute = sharedPreferences.getInt("begin_minute", 0);
                                String begin_date = sharedPreferences.getString("begin_date", " ");
                                int parkType = sharedPreferences.getInt("park_type", 0);
                                int end_time_hour = sharedPreferences.getInt("end_hour", 0);
                                int end_time_minute = sharedPreferences.getInt("end_minute", 0);
                                String indirizzo_db = sharedPreferences.getString("citta", " ") + ", " +
                                        sharedPreferences.getString("indirizzo", "");
                                FragmentButtons buttonsFragment = (FragmentButtons) getFragmentManager().findFragmentById(R.id.fragment_buttons);
                                String parktype_db = buttonsFragment.intToParkType(parkType);
                                String date_db = begin_date;
                                String orainizio_db = HistoryActivity.getXXFormat(begin_time_hour) + ":" + HistoryActivity.getXXFormat(begin_time_minute);
                                String orafine_db;
                                if(parkType!=0) {
                                    orafine_db = HistoryActivity.getXXFormat(end_time_hour) + ":" + HistoryActivity.getXXFormat(end_time_minute);
                                } else {
                                    orafine_db = "/";
                                }

                                Park park = new Park(indirizzo_db, parktype_db,date_db,orainizio_db,orafine_db);
                                db.insertPark(park);
                                dialog.dismiss();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear().commit();
                                currentPositionMarker.remove();
                                hideButtons();


                                //arresto il servizio se elimino il parcheggio
                                app = (ParkAppApplicationObject) getApplication();
                                Intent intentService = new Intent(app, ParkAppService.class);
                                app.stopService(intentService);
                            }
                        });
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });
                alertDialog.show();

                //cancella la sharedPreferences alla pressione del bottone

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });
        //Recuperta il parktype che se è a meno uno significa che è stato cancellato dal bottone

    }

    public void hideButtons() {
        sharedPreferences = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        int parkType = sharedPreferences.getInt("park_type", -1);
        if (parkType == -1) {

            park_delete_button.setVisibility(View.GONE);
            park_id.setVisibility(View.GONE);
            park_description.setVisibility(View.GONE);

        } else {
            park_delete_button.setVisibility(View.VISIBLE);
            park_id.setVisibility(View.VISIBLE);
            park_description.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Attenzione");
            alertDialog.setMessage("ParkApp ha bisogno del GPS per funzionare, riavviare l'app e attivare il gps");
            alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Attiva GPS",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Esci",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();

                        }
                    });
            alertDialog.show();

        }

        if (!isOnline()) {
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Attenzione");
            alertDialog.setMessage("ParkApp ha bisogno della connessione internet per funzionare, riavviare l'app e attivare internet");
            alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Connetti",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    });
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Esci",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();

                        }
                    });
            alertDialog.show();
        }


        sharedPreferences = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);


        int begin_time_hour = sharedPreferences.getInt("begin_hour", -1);
        int begin_time_minute = sharedPreferences.getInt("begin_minute", -1);
        String begin_date = sharedPreferences.getString("begin_date", " ");
        int parkType = sharedPreferences.getInt("park_type", -1);
        int end_time_hour = sharedPreferences.getInt("end_hour", -1);
        int end_time_minute = sharedPreferences.getInt("end_minute", -1);


        String indirizzo = sharedPreferences.getString("citta", " ") + "\n" +
                sharedPreferences.getString("indirizzo", "") + "\n";

        String beginHourString = HistoryActivity.getXXFormat( begin_time_hour );
        String beginMinuteString = HistoryActivity.getXXFormat( begin_time_minute );
        String endHourString = HistoryActivity.getXXFormat( end_time_hour );
        String endMinuteString = HistoryActivity.getXXFormat( end_time_minute );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //getSupportActionBar().setIcon(R.mipmap.ic_launcher_parkapp);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        FragmentButtons buttonsFragment = (FragmentButtons) getFragmentManager().findFragmentById(R.id.fragment_buttons);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        String parkDescriptionText = indirizzo;
        parkDescriptionText += buttonsFragment.intToParkType(parkType) + "\n";
        park_id = (TextView) header.findViewById(R.id.park_id_textview);
        park_description = (TextView) header.findViewById(R.id.park_description_textview);
        park_id.setText("Il tuo parcheggio");
        parkDescriptionText += "Data: " + begin_date + "\n" + "Ora inizio: " + beginHourString
                + ":" + beginMinuteString + "\n";
        if (parkType != buttonsFragment.GRATUITO) {
            parkDescriptionText += "Ora fine : " + endHourString + ":" + endMinuteString + "\n";
        }
        park_description.setText(parkDescriptionText);
        app = (ParkAppApplicationObject) getApplication();



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            // Handle the camera action
            startActivity(new Intent(getApplicationContext(), HistoryActivity.class));

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));

        } else if (id == R.id.nav_info) {


            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.info_layout,null);


            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
            alertDialog.setView(dialogView);

            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.setCancelable(true);
            alertDialog.setTitle("Informazioni");
            alertDialog.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //MapStyleOptions map_style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
        //googleMap.setMapStyle(map_style);
        String stringLatitude = sharedPreferences.getString("latitude", null);
        String stringLongitude = sharedPreferences.getString("longitude", null);

        if (stringLatitude != null && stringLongitude != null) {
            if (currentPositionMarker != null) {
                currentPositionMarker.remove();
            }
            double latitude = Double.parseDouble(stringLatitude);
            double longitude = Double.parseDouble(stringLongitude);
            LatLng currentPosition = new LatLng(latitude, longitude);
            currentPositionMarker = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Il tuo parcheggio").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        }
    }
    public void deleteMarker(){
        currentPositionMarker.remove();
    }

    public void saveLocation() {

        if(mLocation != null ) {

            hideButtons();
            //Toast.makeText(this, "triggereato", Toast.LENGTH_SHORT).show();
            sharedPreferences = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            TextView park_id = (TextView) findViewById(R.id.park_id_textview);
            park_id.setText("Il tuo parcheggio");
            //Toast.makeText(this, (float)mLocation.getLatitude()+" "+(float)mLocation.getLongitude(), Toast.LENGTH_SHORT).show();


            String lat = Double.toString(mLocation.getLatitude());
            String lon = Double.toString(mLocation.getLongitude());


            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String indirizzo = addresses.get(0).getAddressLine(0);
                String citta = addresses.get(0).getLocality();
                editor.putString("citta", citta);
                editor.putString("indirizzo", indirizzo);
            } catch (IOException e) {
                e.printStackTrace();
            }


            editor.putString("latitude", lat);
            editor.putString("longitude", lon);
            editor.commit();

            //Toast.makeText(this, mLocation.getLongitude()+" "+ mLocation.getLatitude(), Toast.LENGTH_SHORT).show();


            //remove previous and add the one
            if (currentPositionMarker != null) {
                currentPositionMarker.remove();
            }
            LatLng currentPosition = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            mMap.clear();
            currentPositionMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                    .title("Il tuo parcheggio")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        } else {
            Toast.makeText(this, "Impossibile accedere alla posizione", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, String[] permissions, final int[] grantResults) {
        final String[] perm = permissions;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (mLocation != null) {

                LatLng currentPosition = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(currentPosition).zoom(15).build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            mMap.setMyLocationEnabled(true);

        }
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Attenzione");
                    alertDialog.setMessage("ParkApp ha bisogno della posizione per funzionare! " +
                            "Riavviare l'app e concedere i permessi");
                    alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();

                                }
                            });
                    alertDialog.show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
        } else {

            mLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (mLocation != null) {

                LatLng currentPosition = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(currentPosition).zoom(15).build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            mMap.setMyLocationEnabled(true);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            new AlertDialog.Builder(this).setMessage("Connection failed. Error Code: " + connectionResult.getErrorCode()).show();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {

        mLocation = location;

    }

    public Location getCurrentLocation() {
        return mLocation;
    }

    class notificationThread extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            SharedPreferences sharedPreferences;
            sharedPreferences = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);

            int begin_time_hour = sharedPreferences.getInt("begin_hour", -1);
            int begin_time_minute = sharedPreferences.getInt("begin_minute", -1);

            int end_time_hour = sharedPreferences.getInt("end_hour", -1);
            int end_time_minute = sharedPreferences.getInt("end_minute", -1);

            int end_time = end_time_hour * 60 + end_time_minute;
            int begin_time = begin_time_hour * 60 + begin_time_minute;
            int durata = end_time - begin_time;

            return "OK";
        }

        protected void onPostExecute(String result) {

        }
    }
    public void showNearbParks() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        String url = getUrl(mLocation.getLatitude(), mLocation.getLongitude(), "parking");
        Object[] DataTransfer = new Object[2];
        DataTransfer[0] = mMap;
        DataTransfer[1] = url;
        Log.d("onClick", url);
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(DataTransfer);
        Toast.makeText(this,"Parcheggi nelle vicinanze", Toast.LENGTH_LONG).show();
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=false");
        googlePlacesUrl.append("&key=" + "AIzaSyDuPJ1zEsv6exa9fbSq8j1MDss-A9aEG3Q");
        Log.d("getUrl", googlePlacesUrl.toString());
        googlePlacesUrl.toString();
        return (googlePlacesUrl.toString());
    }




}