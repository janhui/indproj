package com.example.josekalladanthyil.myapplication;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends Activity {

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.distance_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MainActivity.this, ListBeaconsActivity.class);
                intent.putExtra(ListBeaconsActivity.EXTRAS_TARGET_ACTIVITY, ListBeaconsActivity.class.getName());
                startActivity(intent);
            }
        });
        findViewById(R.id.sensor_movement_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MainActivity.this, SensorMovementActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.trilateration_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MainActivity.this, TrilaterationActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.where_is_waldo).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MainActivity.this, WhereIsWaldoTrilat.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.where_is_waldo_sensor).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MainActivity.this, WhereIsWaldoSensor.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_canvas).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MainActivity.this, CanvasActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.ranging).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MainActivity.this, RangingExample.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.button_opencv).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MainActivity.this, Opencvd2Activity.class);
                startActivity(intent);
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


}
