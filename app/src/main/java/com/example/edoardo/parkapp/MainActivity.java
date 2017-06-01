package com.example.edoardo.parkapp;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient googleApiClient;
    private GoogleMap mMap;

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

        /*googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();*/
    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences sharedPref = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        int time_hour = sharedPref.getInt("begin_hour", 0);
        int time_minute = sharedPref.getInt("begin_minute", 0);
        String date = sharedPref.getString("begin_date", " ");
        int parkType = sharedPref.getInt("park_type", 0);
        int end_time_hour = sharedPref.getInt("end_hour", 0);
        int end_time_minute = sharedPref.getInt("end_minute", 0);
        Toast.makeText(this, time_hour + ":" + time_minute + " " + date + " " + parkType + " " +
                end_time_hour+ " " + end_time_minute, Toast.LENGTH_SHORT).show();

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

    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;

        LatLng genova = new LatLng(44.403254, 8.958920);
        mMap.addMarker(new MarkerOptions().position(genova).title("enac oiD"));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(genova).zoom(15).build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    /*
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
    public void onConnected(@Nullable Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(location!= null){
            double lat = location.getLatitude();
            double lng = location.getLongitude();
        }
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
            catch (IntentSender.SendIntentException e){
                e.printStackTrace();
            }
        } else {
            new AlertDialog.Builder(this).setMessage("Connection failed. Error code: " + connectionResult.getErrorCode()).show();
        }
    }*/
}