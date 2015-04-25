package com.example.josekalladanthyil.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * Created by josekalladanthyil on 23/02/15.
 */
public class SensorMovementActivity extends Activity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    TextView textView_X;
    TextView textView_Y;
    TextView textView_Z;
    Button calibrate_accelerometer;
    Calibration clickListener;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
    private LinkedList<Float> values_x;
    private LinkedList<Float> values_y;
    private LinkedList<Float> values_z;
    private int total_count;
    private float total_x;
    private float total_y;
    private float total_z;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        values_x = new LinkedList<>();
        values_y = new LinkedList<>();
        values_z = new LinkedList<>();
        total_x = 0f;
        total_y = 0f;
        total_z = 0f;
        total_count = 0;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        textView_X = (TextView)findViewById(R.id.accelerometer_value_x);
        textView_Y = (TextView)findViewById(R.id.accelerometer_value_Y);
        textView_Z = (TextView)findViewById(R.id.accelerometer_value_Z);
        calibrate_accelerometer = (Button)findViewById(R.id.calibrate_accelerometer);
        clickListener = new Calibration(this.senAccelerometer, sensorManager);
        calibrate_accelerometer.setOnClickListener(clickListener);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0] - clickListener.getdX();
            float y = sensorEvent.values[1] - clickListener.getdY();
            float z = sensorEvent.values[2] - clickListener.getdZ();

            long curTime = System.currentTimeMillis();
            if (total_count == 100) {
                float temp_x = 0f;
                float temp_y = 0f;
                float temp_z = 0f;
                textView_X.setText(String.format("X: %s", temp_x));
                textView_Y.setText(String.format("Y: %s", temp_y));
                textView_Z.setText(String.format("Z: %s", temp_z));
            }
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
