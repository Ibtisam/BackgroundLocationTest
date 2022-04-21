package com.example.backgroundlocationtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MyLocationService myLocationService;
    private boolean mBound = false;
    private LocationData locationData;
    private GeofenceTransitionData geofenceTransitionData;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationData = new ViewModelProvider(this).get(LocationData.class);
        locationData.initialize();

        geofenceTransitionData = GeofenceTransitionData.getInstance();

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 99);
        } else {
            // Bind to MyLocationService
            Intent intent = new Intent(getApplicationContext(), MyLocationService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }


    }



    @Override
    protected void onStart() {
        super.onStart();
        if(!mBound) {
            Intent intent = new Intent(getApplicationContext(), MyLocationService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }

        findViewById(R.id.locUpSB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    myLocationService.setContext(MainActivity.this);
                    myLocationService.createLocationRequest();
                }
            }
        });

        findViewById(R.id.geoB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    myLocationService.setContext(MainActivity.this);
                    myLocationService.checkGeofence();
                }
            }
        });

        MutableLiveData<LocationCoords> coordsData = locationData.getData();
        coordsData.observe(this, new Observer<LocationCoords>() {
            @Override
            public void onChanged(LocationCoords locationCoords) {
                TextView textView = findViewById(R.id.locInfoTV);
                textView.setText("Location: " + locationCoords.getLatitude() + " " + locationCoords.getLongitude());
            }
        });

        MutableLiveData<String> mutableLiveData = geofenceTransitionData.getData();
        mutableLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                TextView textView = findViewById(R.id.geoInfoTV);
                textView.setText(s);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 99:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Bind to MyLocationService
                    Intent intent = new Intent(getApplicationContext(), MyLocationService.class);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                }
                break;
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to MyLocationService, cast the IBinder and get MyLocationService instance
            MyLocationService.LocalBinder binder = (MyLocationService.LocalBinder) service;
            myLocationService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}