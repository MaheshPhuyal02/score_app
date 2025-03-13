
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.tm.score_app.models.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

enum class DeviceType {
    PHONE,
    WATCH
}


class BluetoothService(private val context: Context) {

    private val TAG = "BluetoothService"
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

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
    private var _heartRate = MutableStateFlow(0)
    val heartRate = _heartRate.asStateFlow()

    /**
     * Filter list of devices to only return connected ones
     */
    fun filterConnectedList(deviceList: List<Device>): List<Device> {
        return deviceList.filter { it.isConnected }
    }

    /**
     * Connect to a device of specified type
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: Device): Boolean {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not available or not enabled")
            return false
        }

        try {
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.address)

            when (device.deviceType) {
                DeviceType.PHONE -> {
                    phoneSocket?.close() // Close existing connection if any
                    phoneSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                    phoneSocket?.connect()
                    phoneOutputStream = phoneSocket?.outputStream
                    phoneInputStream = phoneSocket?.inputStream

                    // Start listening in background
                    Thread { listenToInputStream(phoneInputStream, DeviceType.PHONE) }.start()
                }

                DeviceType.WATCH -> {
                    watchSocket?.close() // Close existing connection if any
                    watchSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                    watchSocket?.connect()
                    watchOutputStream = watchSocket?.outputStream
                    watchInputStream = watchSocket?.inputStream

                    // Start listening in background
                    Thread { listenToInputStream(watchInputStream, DeviceType.WATCH) }.start()
                }
            }

            return true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to connect to device: ${device.deviceName}", e)
            return false
        }
    }

    /**
     * Disconnect from a device of specified type
     */
    fun disconnectFromDevice(deviceType: DeviceType): Boolean {
        return try {
            when (deviceType) {
                DeviceType.PHONE -> {
                    phoneInputStream?.close()
                    phoneOutputStream?.close()
                    phoneSocket?.close()
                    phoneSocket = null
                    phoneInputStream = null
                    phoneOutputStream = null
                }

                DeviceType.WATCH -> {
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
    fun sendDataToDevice(data: String, deviceType: DeviceType): Boolean {
        try {
            val outputStream = when (deviceType) {
                DeviceType.PHONE -> phoneOutputStream
                DeviceType.WATCH -> watchOutputStream
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
    fun checkConnectionStatus(deviceType: DeviceType): Boolean {
        return when (deviceType) {
            DeviceType.PHONE -> phoneSocket?.isConnected == true
            DeviceType.WATCH -> watchSocket?.isConnected == true
        }
    }

    /**
     * Get heart rate from connected device
     */
    fun getHeartRate(): Int {
        return _heartRate.value
    }

    /**
     * Private method to listen to incoming data
     */
    private fun listenToInputStream(inputStream: InputStream?, deviceType: DeviceType) {
        inputStream ?: return

        val buffer = ByteArray(1024)
        var bytes: Int

        while (true) {
            try {
                bytes = inputStream.read(buffer)
                val data = String(buffer, 0, bytes)

                when (deviceType) {
                    DeviceType.WATCH -> {
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
                    DeviceType.PHONE -> {
                        // Handle phone data if needed
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Disconnected from $deviceType", e)
                break
            }
        }
    }

    /**
     * Clean up resources when the service is destroyed
     */
    open fun cleanup() {
        disconnectFromDevice(DeviceType.PHONE)
        disconnectFromDevice(DeviceType.WATCH)
    }
}