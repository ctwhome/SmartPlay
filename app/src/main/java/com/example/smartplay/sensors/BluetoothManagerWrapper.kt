package com.example.smartplay.sensors

import android.content.Context

class BluetoothManagerWrapper(private val context: Context) {
    private val bluetoothManager = CustomBluetoothManager(context)

    fun startScanning(frequencyRate: Long = 1000) {
        bluetoothManager.startScanning(frequencyRate)
    }

    fun stopScanning() {
        bluetoothManager.stopScanning()
    }

    fun setOnScanResultListener(listener: (Map<String, Int>) -> Unit) {
        bluetoothManager.setOnScanResultListener(listener)
    }
}