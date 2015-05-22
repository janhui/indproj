package com.example.josekalladanthyil.myapplication.utils;

import com.estimote.sdk.*;
import com.estimote.sdk.Utils;

/**
 * Created by josekalladanthyil on 12/04/15.
 */
public class FixedBeacon {
    private  Beacon beacon;
    //make it changeable!!
    private Position position;

    public FixedBeacon(Beacon beacon, Position position) {
        this.beacon = beacon;
        this.position = position;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public Position getPosition() {

        return position;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
// y = mx + c where m = 0.563 and c = 0.651
    public float getDistance() {
        double rssi = beacon.getRssi();
        double power = beacon.getMeasuredPower();
        float m = 1.4995f;
        float c = -0.9355f;
        return (float) (m*(rssi/power) + c);
//        return (float) Utils.computeAccuracy(beacon);
    }
}
