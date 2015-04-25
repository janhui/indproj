package com.example.josekalladanthyil.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by josekalladanthyil on 11/04/15.
 */
public class TrilaterationActivity extends Activity {

    private final String TAG = "TrilaterationActivity";
    private BeaconManager beaconManager;
    private List<FixedBeacon> beaconList;
    private List<Integer> majorList;
    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private Position currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trilateration);
        majorList = new ArrayList<>();
        beaconList = new ArrayList<FixedBeacon>();
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                // Note that results are not delivered on UI thread.
                majorList.clear();
                beaconList.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "in trilat run ui");
                        // TODO: filters by my beacon ids --- get rid when not necessary
                        if (beaconList.size() < 3) {
                            for (Beacon beacon : beacons) {
                                if (majorList.contains(beacon.getMajor())) {
                                    continue;
                                } else {

                                    majorList.add(beacon.getMajor());
                                    if (beacon.getMajor() == 170) {
                                        beaconList.add(new FixedBeacon(beacon, new Position(0, 0)));
                                    } else if (beacon.getMajor() == 61787) {
                                        beaconList.add(new FixedBeacon(beacon, new Position(0, 10)));
                                    } else if (beacon.getMajor() == 46433) {
                                        beaconList.add(new FixedBeacon(beacon, new Position(10, 0)));
                                    }
                                }
                            }
                        }
                        Log.v(TAG, beaconList.size() + "");
                        if (beaconList.size() == 3) {
                            Position p = calculatePosition(beaconList);
                            TextView t = (TextView) findViewById(R.id.trilateration_position_value);
                            t.setText(String.format("X: %s , Y: %s", p.getX(), p.getY()));
                        }
                    }
                });
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        // Check if device supports Bluetooth Low Energy.
        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }
    }

    private void connectToService() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Toast.makeText(TrilaterationActivity.this, "Cannot start ranging, something terrible happened",
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }



    private Position calculatePosition(List<FixedBeacon> beaconList) {
        Position position;
        //p1 x y
        double p1x = beaconList.get(0).getPosition().getX();
        double p1y = beaconList.get(0).getPosition().getY();
        //p2 x y
        double p2x = beaconList.get(1).getPosition().getX();
        double p2y = beaconList.get(1).getPosition().getY();
//        p3 x yo
        double p3x = beaconList.get(2).getPosition().getX();
        double p3y = beaconList.get(2).getPosition().getY();
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
        double currentPositionX = 0d;
        double currentPositionY = 0d;
        for (int i = 0; i < p1.length; i++) {
            double t1 = p1[i];
            double t2 = ex.get(i) * xval;
            double t3 = ey.get(i) * yval;
            double tx = t1 + t2 + t3;
            if (i == 0) {
                currentPositionX = tx;
            } else {
                currentPositionY = tx;
            }
        }
        return new Position(currentPositionX, currentPositionY);
    }


}
