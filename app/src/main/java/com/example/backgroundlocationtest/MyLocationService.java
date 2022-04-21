package com.example.backgroundlocationtest;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MyLocationService extends Service {
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    private LocationData locationData;
    private List<Geofence> geofenceList;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        MyLocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyLocationService.this;
        }
    }

    public MyLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public void setContext(Context context) {
        locationData = new ViewModelProvider((MainActivity) context).get(LocationData.class);
    }

    protected void createLocationRequest() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

                LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        for (Location location : locationResult.getLocations()) {
                            // Update ViewModel with location data
                            // ...
                            //LocationCoords locationCoords = new LocationCoords(location.getLatitude(), location.getLongitude());
                            locationData.setData(location.getLatitude(), location.getLongitude());
                        }
                    }
                };

                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setInterval(10000);
                locationRequest.setFastestInterval(5000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);

                SettingsClient client = LocationServices.getSettingsClient(getApplicationContext());
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
                task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        // All location settings are satisfied. The client can initialize
                        // location requests here.
                        // ...
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                    }
                });

                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });

    }

    @SuppressLint("MissingPermission")
    protected void checkGeofence() {
        createLocationRequest();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                geofencingClient = LocationServices.getGeofencingClient(getApplicationContext());
                //add geofences here
                geofenceList = new ArrayList<>();

                //add current location as geofence you can add from firebase
                geofenceList.add(new Geofence.Builder()
                        .setRequestId("Geofence1")
                        .setCircularRegion(34.191847, 73.2317834, 100)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());

                geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Geofences added
                                // ...
                                //Toast.makeText(getApplicationContext(), "Geofence added", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to add geofences
                                // ...
                            }
                        });
            }
        });


    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}