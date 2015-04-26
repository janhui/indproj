package com.example.josekalladanthyil.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josekalladanthyil on 26/04/15.
 */
public class WhereIsWaldo extends Activity {
    HorizontalScrollView hsv;
    ScrollView sv;
    private final String TAG = "Where is Waldo";
    private BeaconManager beaconManager;
    private List<FixedBeacon> beaconList;
    private List<Integer> majorList;
    int prevX;
    int prevY;
    private static final int REQUEST_ENABLE_BT = 1234;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private Position currentPosition;
    int screenWidth;
    int screenHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_is_waldo);
        hsv = (HorizontalScrollView)findViewById(R.id.horizontal_scroll_view);
        sv = (ScrollView)findViewById(R.id.vertical_scroll_view);
        scroll(hsv, 0, 0);
        scroll(sv, 0, 0);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        majorList = new ArrayList<>();
        prevX = 0;
        prevY = 0;
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
                            prevX += Math.abs((int)((p.getX()*1000)-15000))*90;
                            prevY += Math.abs((int)((p.getY()*1000)-15000))*90;
                            scroll(sv, prevX, prevY);
                            scroll(hsv,prevX, prevY);
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
                    Toast.makeText(WhereIsWaldo.this, "Cannot start ranging, something terrible happened",
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }


    private void scroll(HorizontalScrollView hsv, int x, int y) {
        hsv.scrollTo(x,y);
    }

    private void scroll(ScrollView sv , int x, int y){
        sv.scrollTo(x, y);
    }


}
