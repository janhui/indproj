package com.example.josekalladanthyil.myapplication;

/**
 * Created by josekalladanthyil on 24/04/15.
 */
public class AcccelerometerPosition extends Position {
    private float x = 0;
    private float y = 0;
    private int cnt = 0;

    //calibration variable
    private  float dX = 0;
    private  float dY = 0;
    private  float dZ = 0;

    public AcccelerometerPosition(double X, double Y, int cnt) {
        super(X, Y);
        this.x = (float) X;
        this.y = (float) Y;
        this.cnt = cnt;
    }
}
