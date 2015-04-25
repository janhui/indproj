package com.example.josekalladanthyil.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

/**
 * Created by josekalladanthyil on 25/04/15.
 */
public class Calibration implements View.OnClickListener, SensorEventListener {
    private final int CALIBRATION_TIME = 1000;
    private Sensor accel;
    private SensorManager manager;
    private float accumulator_x;
    private float accumulator_y;
    private float accumulator_z;
    private float dX;
    private float dY;
    private float dZ;

    public Calibration(Sensor senAccelerometer, SensorManager sensorManager) {
        this.accel = senAccelerometer;
        this.manager = sensorManager;
        this.accumulator_x = 0;
        this.accumulator_y = 0;
        this.accumulator_z = 0;
    }

    @Override
    public void onClick(View v) {
        manager.registerListener(this, accel , SensorManager.SENSOR_DELAY_NORMAL);
     }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long startTime = System.currentTimeMillis();
        int counter = 0;
        while (System.currentTimeMillis() - startTime < 500) {
            accumulator_x += event.values[0];
            accumulator_y += event.values[1];
            accumulator_z += event.values[2];
            counter++;
        }
        manager.unregisterListener(this);
        dX = accumulator_x/counter;
        dY = accumulator_y/counter;
        dZ = accumulator_z/counter;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public float getdX() {
        return dX;
    }

    public float getdY() {
        return dY;
    }

    public float getdZ() {
        return dZ;
    }
}
