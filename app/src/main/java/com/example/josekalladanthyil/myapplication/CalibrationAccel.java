package com.example.josekalladanthyil.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * Created by josekalladanthyil on 26/04/15.
 */
public class CalibrationAccel extends Activity implements SensorEventListener, View.OnClickListener {
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private TextView avg_accl;
    private TextView time_diff;
    private Button calibrateButtonStart;
    private Button calibrate;
    private boolean recording;
    private long startTime;
    private long endTime;
    private double accl;
    Calibration clickListener;
    float totalAcc;
    long lastUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        recording = false;
        totalAcc = 0f;
        lastUpdate = 0l;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        avg_accl = (TextView) findViewById(R.id.avg_accl);
        time_diff = (TextView) findViewById(R.id.time_diff);
        calibrateButtonStart = (Button) findViewById(R.id.cal_button_start);
        calibrateButtonStart.setOnClickListener(this);
        Button calibrateButtonStop = (Button)findViewById(R.id.cal_button_stop);
        calibrateButtonStop.setOnClickListener(this);
        calibrate = (Button) findViewById(R.id.calibrate);
        clickListener = new Calibration(senAccelerometer,sensorManager);
        calibrate.setOnClickListener(clickListener);
    }

    @Override
    public void onClick(View v) {
        if(recording) {
            recording = false;
            endTime = System.currentTimeMillis();
            sensorManager.unregisterListener(this);
            double avg = totalAcc/accl;
            long timeDiff = endTime - startTime;
            avg_accl.setText("Avg Accl = "+avg);
            time_diff.setText("TimeDiff = "+timeDiff);

        } else {
            totalAcc =0;
            accl = 0;
            recording = true;
            startTime = System.currentTimeMillis();
            sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        double diffTime = 0;
        long currentTime = System.currentTimeMillis();
        if((currentTime -lastUpdate) > 1000) {
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //get values
                float x = event.values[0] - clickListener.getdX();
                Log.v("CalibrationAccl", "" + x);
                accl++;
                totalAcc += x;
            }
            lastUpdate = currentTime;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
