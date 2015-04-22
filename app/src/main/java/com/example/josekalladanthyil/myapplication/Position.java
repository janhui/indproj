package com.example.josekalladanthyil.myapplication;

/**
 * Created by josekalladanthyil on 11/04/15.
 */
//coordinate system
public class Position {
    private double X;
    private double Y;
    public Position(double X, double Y) {
        this.X = X;
        this.Y = Y;
    }

    public double getY() {
        return Y;
    }

    public double getX() {
        return X;
    }
}
