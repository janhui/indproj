package com.example.josekalladanthyil.myapplication.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

/**
 * Created by josekalladanthyil on 21/05/15.
 */
public class SensorPositioning {
    float last_update = 0f;
    float total_x = 0f;
    float total_y = 0f;
    int num_of_values = 0;
    float ERROR_VALUE = 0f;
    int SECOND = 1000;

    public Position calculatePosition(SensorEvent sensorEvent,Position currentPosition, Calibration calibration) {
        if(!calibration.isCalibrated()) {
            calibration.runCalibration(sensorEvent);
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //get values
            float accl_x = sensorEvent.values[0] - calibration.getdX();
            float accl_y = sensorEvent.values[1] - calibration.getdY();
            long curTime = System.currentTimeMillis();

            //last update was more than a second ago... then get avg acceleration and integrate!
            if(curTime - last_update > SECOND) {
                float avg_x, avg_y;
                total_x += accl_x;
                total_y += accl_y;
                num_of_values++;
                avg_x = total_x/num_of_values;
                avg_y = total_y/num_of_values;
                float curr_x = currentPosition.getX();
                float curr_y = currentPosition.getY();

                //scroll to new position
                if (Math.abs(avg_x) > ERROR_VALUE || Math.abs(avg_y) > ERROR_VALUE) {
                    float new_x = curr_x + avg_x;
                    float new_y = curr_y + avg_y;
                    if(new_x > 0 && new_y > 0) {
                        currentPosition = new Position(new_x, new_y);
                    } else if (new_x > 0) {
                        currentPosition = new Position(new_x, curr_y);
                    } else if (new_y > 0) {
                        currentPosition = new Position(curr_x, new_y);
                    }
//                    currentPosition = new Position(new_x, new_y);
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
        return currentPosition;
    }
}
