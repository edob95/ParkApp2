package com.example.edoardo.parkapp;

import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener{


    private TextView park_id;
    private TextView park_description;



    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    // Parte di update location
    public static final int UPDATE_INTERVAL = 1000;
    public static final int FASTEST_UPDATE_INTERVAL = 2000;
    private LocationRequest locationRequest;
    private Marker currentPositionMarker;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient googleApiClient;
    private GoogleMap mMap;

    private Location mLocation;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*---------------------Mappa-------------------*/

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);

        currentPositionMarker = null;

    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        int begin_time_hour = sharedPreferences.getInt("begin_hour", 0);
        int begin_time_minute = sharedPreferences.getInt("begin_minute", 0);
        String begin_date = sharedPreferences.getString("begin_date", " ");
        int parkType = sharedPreferences.getInt("park_type", 0);
        int end_time_hour = sharedPreferences.getInt("end_hour", 0);
        int end_time_minute = sharedPreferences.getInt("end_minute", 0);
        /*Toast.makeText(this, begin_time_hour + ":" + begin_time_minute + " " + begin_date + " " + parkType + " " +
                end_time_hour + " " + end_time_minute, Toast.LENGTH_SHORT).show();*/
       /* FragmentButtons buttonsFragment = (FragmentButtons) getFragmentManager().findFragmentById(R.id.fragment_buttons);
        buttonsFragment.displayInfoPark();*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        FragmentButtons buttonsFragment = (FragmentButtons) getFragmentManager().findFragmentById(R.id.fragment_buttons);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        String parkDescriptionText = buttonsFragment.intToParkType(parkType) + "\n";
        park_id = (TextView) header.findViewById(R.id.park_id_textview);
        park_description = (TextView) header.findViewById(R.id.park_description_textview);
        park_id.setText("Il tuo parcheggio");
        parkDescriptionText += "Data: " + begin_date + "\n" + "Ora inizio: " + begin_time_hour
                + ":" + begin_time_minute + "\n";
        if(parkType != buttonsFragment.GRATUITO) {
            parkDescriptionText +=  "Ora fine : " + end_time_hour + ":" + end_time_minute + "\n";
        }
        park_description.setText(parkDescriptionText);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            // Handle the camera action
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_info) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String stringLatitude = sharedPreferences.getString("latitude", null);
        String stringLongitude = sharedPreferences.getString("longitude", null);

        if(stringLatitude != null && stringLongitude != null) {
            if(currentPositionMarker != null) {
                currentPositionMarker.remove();
            }

            double latitude = Double.parseDouble(stringLatitude);
            double longitude = Double.parseDouble(stringLongitude);

            LatLng currentPosition = new LatLng(latitude, longitude);
            currentPositionMarker = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Dio, sei tu?"));
        }
    }

    public void saveLocation() {
        //Toast.makeText(this, "triggereato", Toast.LENGTH_SHORT).show();
        sharedPreferences = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        TextView park_id = (TextView) findViewById(R.id.park_id_textview);
        park_id.setText("Il tuo parcheggio");
        //Toast.makeText(this, (float)mLocation.getLatitude()+" "+(float)mLocation.getLongitude(), Toast.LENGTH_SHORT).show();
        String lat = Double.toString(mLocation.getLatitude());
        String lon = Double.toString(mLocation.getLongitude());
        editor.putString("latitude", lat);
        editor.putString("longitude", lon);
        editor.commit();

        //Toast.makeText(this, mLocation.getLongitude()+" "+ mLocation.getLatitude(), Toast.LENGTH_SHORT).show();




        //remove previous and add the one
        if(currentPositionMarker != null){
            currentPositionMarker.remove();
        }
        LatLng currentPosition = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        currentPositionMarker = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Dio, sei tu?"));




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
            ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.MAPS_RECEIVE}, FINE_LOCATION_PERMISSION_REQUEST);
            ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.INTERNET}, FINE_LOCATION_PERMISSION_REQUEST);

        }
        // ActivityCompat.requestPermissions(this, permissionString);

       mLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLocation != null) {

            LatLng currentPosition = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder().target(currentPosition).zoom(15).build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        mMap.setMyLocationEnabled(true);



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if(connectionResult.hasResolution()){
            try{
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            }
            catch(IntentSender.SendIntentException e){
                e.printStackTrace();
            }
        }
        else{
            new AlertDialog.Builder(this).setMessage("Connection failed. Error Code: " + connectionResult.getErrorCode()).show();
        }
    }


    @Override
    public void onStart(){
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop(){
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        /*
        //remove previous and add the one
        if(currentPositionMarker != null){
            currentPositionMarker.remove();
        }
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        currentPositionMarker = mMap.addMarker(new MarkerOptions().position(currentPosition).title("enac oiD"));
        */

        mLocation=location;
        //Toast.makeText(this, mLocation.getLatitude() + " " + mLocation.getLongitude() + " :)", Toast.LENGTH_LONG).show();
    }
}