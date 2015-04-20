package com.example.josekalladanthyil.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josekalladanthyil on 11/04/15.
 */
public class TrilaterationActivity extends Activity {
    private BeaconManager beaconManager;
    private List<FixedBeacon> beaconList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trilateration);
        beaconList = new ArrayList<FixedBeacon>();
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("Trilateration","in trilat run ui");
                        // TODO: filters by my beacon ids --- get rid when not necessary
                        for (Beacon beacon : beacons) {
                            if (beacon.getMajor() == 170) {
                                TextView t = (TextView) findViewById(R.id.trilateration_position_value);
                                t.setText(beacon.getProximityUUID());
                                beaconList.add(new FixedBeacon(beacon, new Position(0, 0)));
                            } else if (beacon.getMajor() == 61787) {
                                beaconList.add(new FixedBeacon(beacon, new Position(0, 100)));
                            } else if (beacon.getMajor() == 46433) {
                                beaconList.add(new FixedBeacon(beacon, new Position(100, 0)));
                            }

                        }

                    }
                });
            }
        });
//        Position pos = calculatePosition(beaconList);

    }

    private void calculatePosition(List<FixedBeacon> beaconList) {
        Position position;
        //p1 x y
        double p1x = beaconList.get(1).getPosition().getX();
        double p1y = beaconList.get(1).getPosition().getY();
        //p2 x y
        double p2x = beaconList.get(2).getPosition().getX();
        double p2y = beaconList.get(2).getPosition().getY();
//        p3 x yo
        double p3x = beaconList.get(3).getPosition().getX();
        double p3y = beaconList.get(3).getPosition().getY();
//        distances
        double d1 = beaconList.get(1).getDistance();
        double d2 = beaconList.get(2).getDistance();
        double d3 = beaconList.get(3).getDistance();

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
            exx = t*t/Math.sqrt(temp);
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

        ArrayList<Double> point = new ArrayList<>();
        for (int i = 0; i < p1.length; i++) {
            double t1 = p1[i];
            double t2 = ex.get(i);
            double t3 = ey.get(i);
            double tx = t1+t2+t3;
            point.add(tx);
        }
        Utils.computeAccuracy(beaconList.get(0).getBeacon());
//        return null;
    }


}
