package com.tm.score_app.pages

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.tm.score_app.models.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.math.abs

enum class DeviceType {
    PHONE,
    WATCH
}

class BluetoothService(val context: Context) {

    private val TAG = "BluetoothService"
    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private var phoneSocket: BluetoothSocket? = null
    private var watchSocket: BluetoothSocket? = null
    private var phoneOutputStream: OutputStream? = null
    private var phoneInputStream: InputStream? = null
    private var watchOutputStream: OutputStream? = null
    private var watchInputStream: InputStream? = null

    // UUID for Serial Port Profile (SPP)
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Data from watch
    private val _watchData = MutableStateFlow<String>("")
    val watchData = _watchData.asStateFlow()

    // Heart rate data
    private val _heartRate = MutableStateFlow(0)
    val heartRate = _heartRate.asStateFlow()

    // MutableStateFlow for discovered devices
    private val _discoveredDevicesFlow = MutableStateFlow<List<Device>>(emptyList())
    val discoveredDevicesFlow: StateFlow<List<Device>> = _discoveredDevicesFlow

    // Scanning state
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    /**
     * Filter list of devices to only return connected ones
     */
    fun filterConnectedList(deviceList: List<Device>): List<Device> {
        return deviceList.filter { it.deviceStatus == "connected" }
    }

    /**
     * Connect to a device of specified type
     */
    fun connectToDevice(device: Device): Boolean {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not available or not enabled")
            return false
        }

        try {
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.deviceId)

            when (device.deviceType) {
                DeviceType.PHONE -> {
                    phoneSocket?.close() // Close existing connection if any
                    phoneSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                    phoneSocket?.connect()
                    phoneOutputStream = phoneSocket?.outputStream
                    phoneInputStream = phoneSocket?.inputStream

                    // Start listening in background
                    Thread { listenToInputStream(phoneInputStream, "phone") }.start()
                }

                DeviceType.WATCH -> {
                    watchSocket?.close() // Close existing connection if any
                    watchSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                    watchSocket?.connect()
                    watchOutputStream = watchSocket?.outputStream
                    watchInputStream = watchSocket?.inputStream

                    // Start listening in background
                    Thread { listenToInputStream(watchInputStream, "watch") }.start()
                }
            }

            return true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to connect to device: ${device.deviceName}", e)
            return false
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when connecting to: ${device.deviceName}", e)
            return false
        }
    }

    /**
     * Disconnect from a device of specified type
     */
    fun disconnectFromDevice(deviceType: String): Boolean {
        return try {
            when (deviceType) {
                "phone" -> {
                    phoneInputStream?.close()
                    phoneOutputStream?.close()
                    phoneSocket?.close()
                    phoneSocket = null
                    phoneInputStream = null
                    phoneOutputStream = null
                }

                "watch" -> {
                    watchInputStream?.close()
                    watchOutputStream?.close()
                    watchSocket?.close()
                    watchSocket = null
                    watchInputStream = null
                    watchOutputStream = null
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to disconnect from $deviceType", e)
            false
        }
    }

    /**
     * Send data to a device of specified type
     */
    fun sendDataToDevice(data: String, deviceType: String): Boolean {
        try {
            val outputStream = when (deviceType) {
                "phone" -> phoneOutputStream
                "watch" -> watchOutputStream
                else -> null
            }

            outputStream?.write(data.toByteArray())
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to send data to $deviceType", e)
            return false
        }
    }

    /**
     * Listen to data from watch
     */
    fun listenWatchData(): String {
        return watchData.value
    }

    /**
     * Check connection status with a device of specified type
     */
    fun checkConnectionStatus(deviceType: String): Boolean {
        return when (deviceType) {
            "phone" -> phoneSocket?.isConnected == true
            "watch" -> watchSocket?.isConnected == true
            else -> false
        }
    }

    /**
     * Get heart rate from connected device
     */
    fun getHeartRate(): Int {
        return _heartRate.value
    }

    /**
     * Scan for Bluetooth devices
     */
    fun scanDevices() {
        // Check for required permissions first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ permissions
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_SCAN permission not granted")
                return
            }
        } else {
            // Older Android versions
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "ACCESS_FINE_LOCATION permission not granted")
                return
            }
        }

        // Cancel any ongoing discovery
        bluetoothAdapter?.cancelDiscovery()

        // Create a receiver for found devices
        val discoveredDevices = mutableListOf<Device>()

        // Register BroadcastReceiver for found devices
        val deviceFoundReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when(intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        // Get the BluetoothDevice from the Intent
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                        device?.let { bluetoothDevice ->
                            // Get device name (safely)
                            val deviceName = try {
                                bluetoothDevice.name ?: "Unknown Device"
                            } catch (e: SecurityException) {
                                "Unknown Device"
                            }

                            // Get device address (safely)
                            val deviceAddress = try {
                                bluetoothDevice.address
                            } catch (e: SecurityException) {
                                ""
                            }

                            // Create Device object
                            val newDevice = Device(
                                deviceId = deviceAddress,
                                deviceName = deviceName,
                                deviceType = guessDeviceType(deviceName),
                                deviceStatus = "disconnected",
                                score = 0.0,
                                heartRate = 0.0,
                                isMine = false,
                                isPaired = false,
                                isSynced = false,
                                color = generateColorForDevice(deviceAddress)
                            )

                            // Add to discovered devices if not already there
                            if (discoveredDevices.none { it.deviceId == deviceAddress }) {
                                discoveredDevices.add(newDevice)
                                // Notify listeners about the new device
                                _discoveredDevicesFlow.value = discoveredDevices.toList()
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        // Unregister when discovery is finished
                        try {
                            context.unregisterReceiver(this)
                        } catch (e: IllegalArgumentException) {
                            // Receiver not registered
                        }

                        _isScanning.value = false
                        Log.d(TAG, "Bluetooth discovery finished")
                    }
                }
            }
        }

        // Register for broadcasts when a device is found or discovery finished
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        try {
            context.registerReceiver(deviceFoundReceiver, filter)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register receiver", e)
            return
        }

        // Start discovery
        try {
            _isScanning.value = true
            val started = bluetoothAdapter?.startDiscovery() ?: false

            if (!started) {
                Log.e(TAG, "Failed to start discovery")
                _isScanning.value = false
                try {
                    context.unregisterReceiver(deviceFoundReceiver)
                } catch (e: IllegalArgumentException) {
                    // Receiver not registered
                }
            }

            // Set a timeout for scanning (typically 12 seconds)
            Handler(Looper.getMainLooper()).postDelayed({
                if (_isScanning.value) {
                    bluetoothAdapter?.cancelDiscovery()
                    _isScanning.value = false
                    try {
                        context.unregisterReceiver(deviceFoundReceiver)
                    } catch (e: IllegalArgumentException) {
                        // Receiver not registered
                    }
                    Log.d(TAG, "Bluetooth discovery timeout reached")
                }
            }, 12000)

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during discovery", e)
            _isScanning.value = false
        }
    }

    /**
     * Private method to listen to incoming data
     */
    private fun listenToInputStream(inputStream: InputStream?, deviceType: String) {
        inputStream ?: return

        val buffer = ByteArray(1024)
        var bytes: Int

        while (true) {
            try {
                bytes = inputStream.read(buffer)
                val data = String(buffer, 0, bytes)

                when (deviceType) {
                    "watch" -> {
                        _watchData.value = data

                        // Extract heart rate if data contains it
                        if (data.contains("HR:")) {
                            val hrValue = data.substringAfter("HR:").substringBefore("\n").trim()
                            try {
                                _heartRate.value = hrValue.toInt()
                            } catch (e: NumberFormatException) {
                                Log.e(TAG, "Invalid heart rate format: $hrValue")
                            }
                        }
                    }
                    "phone" -> {
                        // Handle phone data if needed
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Disconnected from $deviceType", e)
                break
            }
        }
    }

    // Helper function to guess device type based on name
    private fun guessDeviceType(name: String): DeviceType {
        return when {
            name.contains("watch", ignoreCase = true) -> DeviceType.WATCH
            else -> DeviceType.PHONE
        }
    }

    // Helper function to generate consistent color for a device
    private fun generateColorForDevice(deviceId: String): Int {
        val colors = arrayOf(
            0xFFE040FB.toInt(), // Purple
            0xFF00BCD4.toInt(), // Cyan
            0xFFFFEB3B.toInt(), // Yellow
            0xFF4CAF50.toInt(), // Green
            0xFFFF5722.toInt()  // Deep Orange
        )

        // Use hash code to consistently generate same color for same device
        return colors[abs(deviceId.hashCode()) % colors.size]
    }

    /**
     * Clean up resources when the service is destroyed
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun cleanup() {
        disconnectFromDevice("phone")
        disconnectFromDevice("watch")

        // Cancel any ongoing discovery
        if (_isScanning.value) {
            bluetoothAdapter?.cancelDiscovery()
            _isScanning.value = false
        }
    }
}