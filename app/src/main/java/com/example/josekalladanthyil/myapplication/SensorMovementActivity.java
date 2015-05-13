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
import com.example.josekalladanthyil.myapplication.utils.Position;

/**
 * Created by josekalladanthyil on 23/02/15.
 */
public class SensorMovementActivity extends Activity implements SensorEventListener {
    private final float ERROR_VALUE = 0.15f;
    private final int SECOND = 1000;

    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private TextView textView_X;
    private TextView textView_Y;
    private TextView textview_position_X;
    private TextView textView_position_Y;
    private float total_x, total_y;
    private long last_update;
    private int num_of_values;
    private float total_accl_x, total_accl_y;
    private Button calibrate_accelerometer;
    private Calibration clickListener;
    private Position currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        init();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        textView_X = (TextView)findViewById(R.id.accelerometer_value_x);
        textView_Y = (TextView)findViewById(R.id.accelerometer_value_Y);
        textview_position_X = (TextView)findViewById(R.id.position_x);
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

        float accl_x;
        float accl_y;

        if(!clickListener.isCalibrated()) {
            clickListener.runCalibration(sensorEvent);
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //get values
            accl_x = sensorEvent.values[0] - clickListener.getdX();
            accl_y = sensorEvent.values[1] - clickListener.getdY();
            long curTime = System.currentTimeMillis();

            //last update was more than a second ago... then get avg acceleration and integrate!
            if(curTime - last_update > SECOND) {
                float avg_x, avg_y;
                total_x += accl_x;
                total_y += accl_y;
                num_of_values++;
                avg_x = (float) total_x/num_of_values;
                avg_y = (float) total_y/num_of_values;
                float curr_x = currentPosition.getX();
                float curr_y = currentPosition.getY();

                if (Math.abs(avg_x) > ERROR_VALUE || Math.abs(avg_y) > ERROR_VALUE) {
                    currentPosition = new Position(curr_x + avg_x, curr_y + avg_y);
                }

                //resets values
                total_x = total_y = 0f;
                num_of_values = 0;
                last_update = curTime;

//             else add the acceleration and the counter
            } else {
                total_x += accl_x;
                total_y += accl_y;
                num_of_values++;
            }
            setTextviewFields(accl_x, accl_y);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //no op
    }


    // set up all the values§
    private void init() {
        currentPosition = new Position(0f,0f);
        total_accl_x = total_accl_y = 0f;
        total_x = total_y = 0f;
        last_update = 0l;
        num_of_values = 0;
    }

    private void setTextviewFields(float accl_x, float accl_y) {
        this.textview_position_X.setText("Position X: " + currentPosition.getX());
        this.textView_position_Y.setText("Position Y: " + currentPosition.getY());
        textView_X.setText(String.format("Accl X: %s", accl_x));
        textView_Y.setText(String.format("Accl Y: %s", accl_y));
    }

}
