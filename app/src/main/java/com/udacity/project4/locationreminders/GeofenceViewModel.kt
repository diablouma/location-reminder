package com.udacity.project4.locationreminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class GeofenceViewModel(state: SavedStateHandle) : ViewModel() {
    private val _geofenceIndex = state.getLiveData(GEOFENCE_INDEX_KEY, -1)
    val geofenceIndex: LiveData<Int>
        get() = _geofenceIndex
}

private const val GEOFENCE_INDEX_KEY = "geofenceIndex"