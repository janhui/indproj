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

import com.example.josekalladanthyil.myapplication.utils.Calibration;

/**
 * Created by josekalladanthyil on 23/02/15.
 */
public class SensorMovementActivity extends Activity implements SensorEventListener {

    private static final int STEP_SIZE = 500;
    private final int total_count = 50;

    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private TextView textView_X;
    private TextView textView_Y;
    private TextView textview_position_x;
    private TextView textView_position_Y;
//    TextView textView_Z;
    private float last_x, last_y, last_z;
    Button calibrate_accelerometer;
    Calibration clickListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        textView_X = (TextView)findViewById(R.id.accelerometer_value_x);
        textView_Y = (TextView)findViewById(R.id.accelerometer_value_Y);
        textview_position_x = (TextView)findViewById(R.id.position_x);
        textView_position_Y = (TextView)findViewById(R.id.position_y);
//        textView_Z = (TextView)findViewById(R.id.accelerometer_value_Z);
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
        double diffTime = 0;
        float x = 0f;
        float y = 0f;
        float z = 0f;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //get values
            x = sensorEvent.values[0] - clickListener.getdX();
            y = sensorEvent.values[1] - clickListener.getdY();
            z = sensorEvent.values[2] - clickListener.getdZ();
            double curTime = System.currentTimeMillis();


            textView_X.setText(String.format("X: %s", x));
            textView_Y.setText(String.format("Y: %s", y));


        }
    }





    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
