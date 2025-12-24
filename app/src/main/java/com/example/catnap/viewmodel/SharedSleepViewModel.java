package com.example.catnap.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedSleepViewModel extends ViewModel {
    private final MutableLiveData<Boolean> dataUpdated = new MutableLiveData<>();

    public void notifyDataUpdated() {
        dataUpdated.setValue(true);
    }

    public LiveData<Boolean> getDataUpdated() {
        return dataUpdated;
    }
}