package com.example.backgroundlocationtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private GeofenceTransitionData geofenceTransitionData;
    @Override
    public void onReceive(Context context, Intent intent) {
        geofenceTransitionData = GeofenceTransitionData.getInstance();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,triggeringGeofences);

            // Send notification and log the transition details.
            geofenceTransitionData.setData(geofenceTransitionDetails);
        } else {
            // Log the error.
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public String getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences){
        String details="";
        if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_ENTER){
            details += "Type: Enter ";
        }else if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_EXIT){
            details += "Type: Exit ";
        }
        for (Geofence geofence:triggeringGeofences) {
            details += "Landmark: "+geofence.getRequestId();
        }

        return details;
    }
}
