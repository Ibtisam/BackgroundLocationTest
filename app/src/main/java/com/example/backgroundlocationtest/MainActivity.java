package com.example.backgroundlocationtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private MyLocationService myLocationService;
    private boolean mBound = false;
    private LocationData locationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationData = new ViewModelProvider(this).get(LocationData.class);
        locationData.initialize();

        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},99);
        }else{
            // Bind to MyLocationService
            Intent intent = new Intent(getApplicationContext(), MyLocationService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.locUpSB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    myLocationService.setContext(MainActivity.this);
                    myLocationService.createLocationRequest();
                }
            }
        });

        MutableLiveData<LocationCoords> coordsData = locationData.getData();
        coordsData.observe(this, new Observer<LocationCoords>() {
            @Override
            public void onChanged(LocationCoords locationCoords) {
                Toast.makeText(getApplicationContext(), "Location: "+locationCoords.getLongitude()+" "+locationCoords.getLatitude(), Toast.LENGTH_SHORT).show();
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
        switch (requestCode){
            case 99:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    // Bind to MyLocationService
                    Intent intent = new Intent(getApplicationContext(), MyLocationService.class);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                }
                break;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
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