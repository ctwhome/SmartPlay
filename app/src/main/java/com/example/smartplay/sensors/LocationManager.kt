package com.example.smartplay.sensors

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class CustomLocationManager(context: Context) : LocationListener {
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    fun startListening() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
        } catch (e: SecurityException) {
            // Handle the case where the app doesn't have location permission
        }
    }

    fun stopListening() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
    }

    // These methods are deprecated but still required to implement LocationListener
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}