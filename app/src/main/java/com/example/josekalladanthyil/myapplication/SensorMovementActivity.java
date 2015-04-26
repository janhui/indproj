package com.example.josekalladanthyil.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * Created by josekalladanthyil on 23/02/15.
 */
public class SensorMovementActivity extends Activity implements SensorEventListener, View.OnClickListener {

    private static final int STEP_SIZE = 500;
    private final int total_count = 50;

    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private TextView textView_X;
    private TextView textView_Y;
    private TextView textview_position_x;
    private TextView textView_position_Y;
//    TextView textView_Z;
    Button calibrate_accelerometer;
    Calibration clickListener;
    private double lastUpdate = 0d;
    private float last_x, last_y, last_z;
    private float last_velocity_x, last_velocity_y, last_velocity_z;
    private LinkedList<Float> accelerate_values_x;
    private LinkedList<Float> accelerate_values_y;
    private LinkedList<Double> timestamps;
    private LinkedList<Float> velocity_values_x;
    private LinkedList<Float> velocity_values_y;

//    private LinkedList<Float> values_z;
    private float total_x;
    private float total_y;
//    private float total_z;
    private float position_x;
    private float velocity_x;
    private float position_y;

    RK4Integration rk4Integrate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);


        accelerate_values_x = new LinkedList<>();
        accelerate_values_y = new LinkedList<>();
        timestamps = new LinkedList<>();
        velocity_values_x = new LinkedList<>();
        velocity_values_y = new LinkedList<>();
        total_x = 1f;
        total_y = 1f;
//        total_z = 0f;
        position_x = 1f;
        position_y = 1f;

        rk4Integrate = new RK4Integration();

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
        Button clearPosition = (Button) findViewById(R.id.clearPosition);
        clearPosition.setOnClickListener(this);

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

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //get values
            float x = sensorEvent.values[0] - clickListener.getdX();
            float y = sensorEvent.values[1] - clickListener.getdY();
            float z = sensorEvent.values[2] - clickListener.getdZ();
            double curTime = System.currentTimeMillis();

//            /find the current avg
            float avg_x = total_x/ accelerate_values_x.size();
            float avg_y = total_y/ accelerate_values_y.size();

            textView_X.setText(String.format("X: %s", avg_x));
            textView_Y.setText(String.format("Y: %s", avg_y));
            if (accelerate_values_x.size() >= total_count) {
                float removed_x = accelerate_values_x.removeFirst();
                float removed_y = accelerate_values_y.removeFirst();
                timestamps.removeFirst();
                velocity_values_x.removeFirst();
                velocity_values_y.removeFirst();
                total_x -= removed_x;
                total_y -= removed_y;
            }

            velocity_values_x.addLast(rk4Integrate.integrate(accelerate_values_x, timestamps));

            if ((curTime - lastUpdate) > 100) {
                diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                total_x += x;
                total_y += y;
//                total_z += z;
                accelerate_values_x.addLast(x);
                accelerate_values_y.addLast(y);
                timestamps.addLast(curTime);

            }
            last_x = x;
            last_y = y;
            last_z = z;
            last_velocity_x = velocity_x;
        }
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        this.position_x = 1f;
        this. position_y = 1f;

        textview_position_x.setText(String.format("Position X: %s", position_x));
        textView_position_Y.setText(String.format("Position Y: %s", position_y));
    }
}
