package com.example.josekalladanthyil.myapplication;

import com.estimote.sdk.*;
import com.estimote.sdk.Utils;

/**
 * Created by josekalladanthyil on 12/04/15.
 */
public class FixedBeacon {
    private  Beacon beacon;
    //make it changeable!!
    private  Position position;

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

    public Double getDistance() {
        return Utils.computeAccuracy(beacon);
    }
}
