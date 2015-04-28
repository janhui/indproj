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
import com.example.josekalladanthyil.myapplication.utils.FixedBeacon;
import com.example.josekalladanthyil.myapplication.utils.Position;

import java.util.ArrayList;
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
                                        beaconList.add(new FixedBeacon(beacon, new Position(0, 30)));
                                    } else if (beacon.getMajor() == 46433) {
                                        beaconList.add(new FixedBeacon(beacon, new Position(30, 0)));
                                    }
                                }
                            }
                        }
                        Log.v(TAG, beaconList.size() + "");
                        if (beaconList.size() == 3) {
                            Position p = Trilateration.calculatePosition(beaconList);
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


}
