package com.example.josekalladanthyil.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.josekalladanthyil.myapplication.Canvas.CanvasView;
import com.example.josekalladanthyil.myapplication.Canvas.ColorPickerDialog;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class CanvasActivity extends Activity implements ColorPickerDialog.OnColorChangedListener {

    // TODO: change this to your own Firebase URL
    private static final String FIREBASE_URL = "https://huddlecanvas.firebaseio.com";

    private static final int COLOR_MENU_ID = Menu.FIRST;

    private CanvasView mDrawingView;
    private Firebase mFirebaseRef;
    private ValueEventListener mConnectedListener;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase(FIREBASE_URL);
        mDrawingView = new CanvasView(this, mFirebaseRef);
        ScrollView sv = new ScrollView(this);
//        mDrawingView.setScrollbarFadingEnabled(false);
        sv.addView(mDrawingView);
        setContentView(sv);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Set up a notification to let us know when we're connected or disconnected from the Firebase servers
        mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(CanvasActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CanvasActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        // Clean up our listener so we don't have it attached twice.
        mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mDrawingView.cleanup();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == COLOR_MENU_ID) {
            new ColorPickerDialog(this, this, 0xFFFF0000).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void colorChanged(int newColor) {
        mDrawingView.setColor(newColor);
    }
}
