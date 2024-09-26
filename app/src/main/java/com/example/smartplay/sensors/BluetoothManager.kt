package com.example.smartplay.sensors

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log

class CustomBluetoothManager(context: Context) {
    private val TAG = "CustomBluetoothManager"
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val scannedDevices = mutableMapOf<String, Int>()
    private lateinit var scanHandler: Handler
    private lateinit var scanRunnable: Runnable

    private var onScanResult: ((Map<String, Int>) -> Unit)? = null

    init {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device doesn't support Bluetooth")
        } else {
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            if (bluetoothLeScanner == null) {
                Log.e(TAG, "Bluetooth LE Scanner not available")
            }
        }
    }

    fun startScanning(frequencyRate: Long) {
        if (!bluetoothAdapter?.isEnabled!!) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }

        val bluetoothLeScanner = this.bluetoothLeScanner
        if (bluetoothLeScanner == null) {
            Log.e(TAG, "BluetoothLeScanner is null")
            return
        }

        scanHandler = Handler(Looper.getMainLooper())
        scanRunnable = object : Runnable {
            override fun run() {
                try {
                    bluetoothLeScanner.startScan(null, ScanSettings.Builder().build(), scanCallback)
                    scanHandler.postDelayed({
                        try {
                            bluetoothLeScanner.stopScan(scanCallback)
                            onScanResult?.invoke(scannedDevices.toMap())
                            scannedDevices.clear()
                            scanHandler.postDelayed(this, frequencyRate)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error stopping BLE scan: ${e.message}")
                        }
                    }, frequencyRate)
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting BLE scan: ${e.message}")
                }
            }
        }
        scanHandler.post(scanRunnable)
    }

    fun stopScanning() {
        scanHandler.removeCallbacks(scanRunnable)
        bluetoothLeScanner?.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceAddress = result.device.address
            val rssi = result.rssi
            scannedDevices[deviceAddress] = rssi
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE Scan Failed. Error Code: $errorCode")
        }
    }

    fun setOnScanResultListener(listener: (Map<String, Int>) -> Unit) {
        onScanResult = listener
    }
}