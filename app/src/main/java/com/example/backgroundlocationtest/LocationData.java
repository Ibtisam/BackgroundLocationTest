package com.example.backgroundlocationtest;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LocationData extends ViewModel {
    MutableLiveData<LocationCoords> mutableLiveData;

    public void initialize(){
        mutableLiveData = new MutableLiveData<>();
    }

    public void setData(LocationCoords locationCoords){
        mutableLiveData.setValue(locationCoords);
    }

    public MutableLiveData<LocationCoords> getData(){
        return mutableLiveData;
    }
}
