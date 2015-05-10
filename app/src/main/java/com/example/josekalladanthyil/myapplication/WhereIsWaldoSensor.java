package com.example.josekalladanthyil.myapplication;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.view.Display;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * Created by josekalladanthyil on 09/05/15.
 */
public class WhereIsWaldoSensor extends Activity implements SensorEventListener {
    HorizontalScrollView hsv;
    ScrollView sv;
    private final String TAG = "Where is Waldo Sensor";

    int screenWidth;
    int screenHeight;

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

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    private void scroll(HorizontalScrollView hsv, int x, int y) {
        hsv.scrollTo(x,y);
    }

    private void scroll(ScrollView sv , int x, int y){
        sv.scrollTo(x, y);
    }
}
