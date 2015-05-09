package com.example.josekalladanthyil.myapplication;

import com.example.josekalladanthyil.myapplication.utils.FixedBeacon;
import com.example.josekalladanthyil.myapplication.utils.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josekalladanthyil on 26/04/15.
 */
public class Trilateration {
    // TODO: float to double conversion
    //TODO: change this from static!

    public static Position calculatePosition(List<FixedBeacon> beaconList) {
        Position position;
        //p1 x y
        float p1x = beaconList.get(0).getPosition().getX();
        float p1y = beaconList.get(0).getPosition().getY();
        //p2 x y
        float p2x = beaconList.get(1).getPosition().getX();
        float p2y = beaconList.get(1).getPosition().getY();
//        p3 x yo
        float p3x = beaconList.get(2).getPosition().getX();
        float p3y = beaconList.get(2).getPosition().getY();
//        distances
        double d1 = beaconList.get(0).getDistance();
        double d2 = beaconList.get(1).getDistance();
        double d3 = beaconList.get(2).getDistance();

        double[] p1 = {p1x,p1y};
        double[] p2 = {p2x,p2y};
        double[] p3 = {p3x,p3y};
        double temp = 0;
        double exx = 0;

        ArrayList<Double> ex = new ArrayList<>();
        ArrayList<Double> p3p1 = new ArrayList<>();

        for (int i = 0; i<p1.length; i++) {
            double t = p2[i] - p1[i];
            temp += t*t;
            p3p1.add(p3[i] - p1[i]);
        }
        for (int i = 0; i<p1.length; i++) {
            double t = p2[i] - p1[i];
            exx = t/Math.sqrt(temp);
            ex.add(exx);
        }
        double ival = 0;
        for(int i = 0; i<ex.size(); i++){
            ival += ex.get(i) * p3p1.get(i);
        }

        ArrayList<Double> ey = new ArrayList<>();
        double p3p1i = 0;
        for(int i = 0; i < p3.length; i++) {
            double t1 = p3[i];
            double t2 = p1[i];
            double t3 = ex.get(i) * ival;
            double t = t1 - t2 - t3;
            p3p1i += (t*t);
        }

        for(int i = 0; i < p3.length; i++) {
            double t1 = p3[i];
            double t2 = p1[i];
            double t3 = ex.get(i) * ival;
            double eyy = (t1 - t2 - t3)/Math.sqrt(p3p1i);
            ey.add(eyy);
        }
        double d = Math.sqrt(temp);

        double jval = 0;
        for(int i = 0; i< ey.size(); i++) {
            double t1 = ey.get(i);
            double t2 = p3p1.get(i);
            jval += t1*t2;
        }
        double xval = (Math.pow(d1,2) - Math.pow(d2,2) + Math.pow(d,2))/(2*d);
        double yval = ((Math.pow(d1, 2) - Math.pow(d3, 2) + Math.pow(ival, 2) + Math.pow(jval, 2))/(2*jval)) - ((ival/jval)*xval);
        float currentPositionX = 0f;
        float currentPositionY = 0f;
        for (int i = 0; i < p1.length; i++) {
            double t1 = p1[i];
            double t2 = ex.get(i) * xval;
            double t3 = ey.get(i) * yval;
            float tx = (float)(t1 + t2 + t3);
            if (i == 0) {
                currentPositionX = tx;
            } else {
                currentPositionY = tx;
            }
        }
        return new Position(currentPositionX, currentPositionY);
    }
}
