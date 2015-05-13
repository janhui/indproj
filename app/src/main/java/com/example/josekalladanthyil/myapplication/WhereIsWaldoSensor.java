package com.example.josekalladanthyil.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.example.josekalladanthyil.myapplication.utils.Calibration;
import com.example.josekalladanthyil.myapplication.utils.Position;

/**
 * Created by josekalladanthyil on 09/05/15.
 */
public class WhereIsWaldoSensor extends Activity implements SensorEventListener {
    HorizontalScrollView hsv;
    ScrollView sv;
    private final String TAG = "Where is Waldo Sensor";

    private final float ERROR_VALUE = 0.15f;
    private final int SECOND = 1000;

    private int screenWidth;
    private int screenHeight;
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private float total_x, total_y;
    private long last_update;
    private int num_of_values;
    private float total_accl_x, total_accl_y;
//    private Button calibrate_accelerometer;
    private Calibration calibrator;
    private Position currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_is_waldo_sensor);
        hsv = (HorizontalScrollView)findViewById(R.id.horizontal_scroll_view);
        sv = (ScrollView)findViewById(R.id.vertical_scroll_view);
        scroll(hsv, 0, 0);
        scroll(sv, 0, 0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;


        currentPosition = new Position(0f,0f);
        total_accl_x = total_accl_y = 0f;
        total_x = total_y = 0f;
        last_update = 0l;
        num_of_values = 0;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        calibrator = new Calibration(this.senAccelerometer, sensorManager);


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float accl_x;
        float accl_y;

        if(!calibrator.isCalibrated()) {
            calibrator.runCalibration(sensorEvent);
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //get values
            accl_x = sensorEvent.values[0] - calibrator.getdX();
            accl_y = sensorEvent.values[1] - calibrator.getdY();
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

                //scroll to new position
                if (Math.abs(avg_x) > ERROR_VALUE || Math.abs(avg_y) > ERROR_VALUE) {
                    currentPosition = new Position(curr_x + avg_x, curr_y + avg_y);
                    scroll(currentPosition);
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
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void scroll(Position currentPosition) {
        scroll(hsv, (int)((currentPosition.getX()-ERROR_VALUE)*100),(int)((currentPosition.getY()-ERROR_VALUE)*100));
        scroll(sv, (int)((currentPosition.getX()-ERROR_VALUE)*100),(int)((currentPosition.getY()-ERROR_VALUE)*100));
    }

    private void scroll(HorizontalScrollView hsv, int x, int y) {
        hsv.scrollTo(x,y);
    }

    private void scroll(ScrollView sv , int x, int y){
        sv.scrollTo(x, y);
    }
}
