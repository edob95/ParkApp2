package com.example.edoardo.parkapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by Niko on 20/06/2017.
 */

public class HistoryActivity extends AppCompatActivity{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_bar);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }
    /*
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;

    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.action_settings_three:

                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
                alertDialog.setTitle("Attenzione");
                alertDialog.setMessage("Stai per cancellare lo storico dei tuoi parcheggi, confermi?");
                alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ParkDB db = new ParkDB(getApplicationContext());
                                db.deleteAll();
                                Toast.makeText(getApplicationContext(), "Cancellazione avvenuta con successo", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                recreate();


                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "ANNULLA",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                               recreate();

                            }
                        });
                alertDialog.show();
                return true;
            default:
                return true;
        }
    }
}
