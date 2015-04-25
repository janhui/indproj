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

    private static final int STEP_SIZE = 100;
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
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private LinkedList<Float> values_x;
    private LinkedList<Float> values_y;
//    private LinkedList<Float> values_z;
    private float total_x;
    private float total_y;
//    private float total_z;
    private float position_x;
    private float position_y;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        values_x = new LinkedList<>();
        values_y = new LinkedList<>();
//        values_z = new LinkedList<>();
        total_x = 0f;
        total_y = 0f;
//        total_z = 0f;
        position_x = 0f;
        position_y = 0f;
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
        long diffTime = 0;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0] - clickListener.getdX();
            float y = sensorEvent.values[1] - clickListener.getdY();
            float z = sensorEvent.values[2] - clickListener.getdZ();

            long curTime = System.currentTimeMillis();

            float avg_x = total_x/values_x.size();
            float avg_y = total_y/values_y.size();
//            float temp_z = total_z/values_z.size();

            textView_X.setText(String.format("X: %s", avg_x));
            textView_Y.setText(String.format("Y: %s", avg_y));
//            textView_Z.setText(String.format("Z: %s", temp_z));
            if (values_x.size() >= total_count) {
                float removed_x = values_x.removeFirst();
                float removed_y = values_y.removeFirst();
//                float removed_z = values_z.removeFirst();
                total_x -= removed_x;
                total_y -= removed_y;
//                total_z -= removed_z;
            }
            if ((curTime - lastUpdate) > 100) {
                diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                total_x += x;
                total_y += y;
//                total_z += z;
                values_x.addLast(x);
                values_y.addLast(y);


                position_x = rk4_Integration(avg_x, position_x, lastUpdate, curTime);
                position_y = rk4_Integration(avg_y, position_y, lastUpdate, curTime);
                textview_position_x.setText(String.format("Position X: %s", position_x));
                textView_position_Y.setText(String.format("Position Y: %s", position_y));

            }
        }
    }

    private float rk4_Integration(float avg, float position, float start, float end) {
        float h = (start - end)/STEP_SIZE;

        for (int i=0; i<STEP_SIZE; i++)
        {
            // Step through, updating x
            start += i * h;

            // Computing all of the trial values
            double k1 = h * deriv(start, position);
            double k2 = h * deriv(start + h/2, position + k1/2);
            double k3 = h * deriv(start + h/2, position + k2/2);
            double k4 = h * deriv(start + h, position + k3);

            // Incrementing y
            position += k1/6 + k2/3+ k3/3 + k4/6;
        }
        return position;

    }
    // todo: need to check this
    public static double deriv(double x, double y)
    {
        return x * Math.sqrt(1 + y*y);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
