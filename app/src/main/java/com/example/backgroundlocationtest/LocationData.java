package com.example.backgroundlocationtest;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LocationData extends ViewModel {
    private MutableLiveData<LocationCoords> mutableLiveData;
    private LocationCoords locationCoords;
    public void initialize(){
        mutableLiveData = new MutableLiveData<>();
        locationCoords = new LocationCoords(0,0);
        mutableLiveData.setValue(locationCoords);
    }

    public void setData(double latitude, double longitude){
        locationCoords.setLatitude(latitude);
        locationCoords.setLongitude(longitude);
        mutableLiveData.setValue(locationCoords);
    }

    public MutableLiveData<LocationCoords> getData(){
        return mutableLiveData;
    }

    public LocationCoords getLocationCoords(){
        return locationCoords;
    }
}
