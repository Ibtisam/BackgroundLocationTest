package com.example.backgroundlocationtest;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GeofenceTransitionData {
    private static GeofenceTransitionData geofenceTransitionData=null;
    private MutableLiveData<String> mutableLiveData;

    private GeofenceTransitionData(){
        mutableLiveData = new MutableLiveData<>();
    }
    public static GeofenceTransitionData getInstance(){
        if(geofenceTransitionData==null){
            geofenceTransitionData = new GeofenceTransitionData();
        }

        return geofenceTransitionData;
    }

    public void setData(String data){
        mutableLiveData.setValue(data);
    }

    public MutableLiveData<String> getData(){
        return mutableLiveData;
    }
}
