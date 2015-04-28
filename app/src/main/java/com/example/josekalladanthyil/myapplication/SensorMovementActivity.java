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
    private final int SECOND = 1000;

    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private TextView textView_X;
    private TextView textView_Y;
    private TextView textview_position_x;
    private TextView textView_position_Y;
    private float total_x, total_y;
    private long last_update;
    private int num_of_values;
    private float total_accl_x, total_accl_y;
    private Button calibrate_accelerometer;
    private Calibration clickListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        total_accl_x = total_accl_y = 0f;
        total_x = total_y = 0f;
        last_update = 0l;
        num_of_values = 0;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        textView_X = (TextView)findViewById(R.id.accelerometer_value_x);
        textView_Y = (TextView)findViewById(R.id.accelerometer_value_Y);
        textview_position_x = (TextView)findViewById(R.id.position_x);
        textView_position_Y = (TextView)findViewById(R.id.position_y);
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
        if(!clickListener.isCalibrated()) {
            clickListener.runCalibration(sensorEvent);
        }
        float x;
        float y;

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //get values
            x = sensorEvent.values[0] - clickListener.getdX();
            y = sensorEvent.values[1] - clickListener.getdY();
            long curTime = System.currentTimeMillis();

            if(curTime - last_update > SECOND) {
                float avg_x, avg_y, displacement_x, displacement_y;
                total_x += x;
                total_y += y;
                num_of_values++;
                avg_x = (float) total_x/num_of_values;
                avg_y = (float) total_y/num_of_values;
                displacement_x = getDisplacement(avg_x);
                displacement_y = getDisplacement(avg_y);
                total_x = total_y = 0f;
                num_of_values = 0;
                last_update = curTime;
            } else {
                total_x += x;
                total_y += y;
                num_of_values++;
            }

            textView_X.setText(String.format("X: %s", x));
            textView_Y.setText(String.format("Y: %s", y));


        }
    }

    private float getDisplacement(float accl) {
        return 0f;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
