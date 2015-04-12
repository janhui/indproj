package com.example.josekalladanthyil.myapplication;

/**
 * Created by josekalladanthyil on 12/04/15.
 */
public class Utils {
    public static double DotProduct(Position positionA, Position positionB) {
        double sum = 0;
        sum += positionA.getX() * positionB.getY();
        sum += positionA.getY() * positionB.getY();
        return sum;
    }
}
