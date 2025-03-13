package com.tm.score_app.services

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BluetoothPermissionsManager(private val activity: AppCompatActivity) {

    private val TAG = "BluetoothPermissionsManager"

    // Request codes
    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_PERMISSIONS = 2
    }

    // Activity result launcher for Bluetooth enable request
    private var bluetoothEnableLauncher: ActivityResultLauncher<Intent>? = null

    // Activity result launcher for permissions
    private var permissionsLauncher: ActivityResultLauncher<Array<String>>? = null

    // Callbacks
    private var onBluetoothEnabled: ((Boolean) -> Unit)? = null
    private var onPermissionsGranted: ((Boolean) -> Unit)? = null

    /**
     * Initialize the permission launchers
     */
    fun initialize() {
        bluetoothEnableLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val isEnabled = result.resultCode == Activity.RESULT_OK
            onBluetoothEnabled?.invoke(isEnabled)
        }

        permissionsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            onPermissionsGranted?.invoke(allGranted)
        }
    }

    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        return bluetoothAdapter?.isEnabled ?: false
    }

    /**
     * Request to enable Bluetooth
     */
    fun requestBluetoothEnable(callback: (Boolean) -> Unit) {
        onBluetoothEnabled = callback

        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            callback(false)
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher?.launch(enableBtIntent) ?: run {
                // Fallback if launcher is not initialized
                ActivityCompat.startActivityForResult(
                    activity,
                    enableBtIntent,
                    REQUEST_ENABLE_BT,
                    null
                )
            }
        } else {
            // Bluetooth is already enabled
            callback(true)
        }
    }

    /**
     * Check if all required Bluetooth permissions are granted
     */
    fun hasRequiredPermissions(context: Context): Boolean {
        return getRequiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get list of required Bluetooth permissions based on Android version
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires these permissions
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            // For older Android versions
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Request required permissions
     */
    fun requestPermissions(callback: (Boolean) -> Unit) {
        onPermissionsGranted = callback

        val permissions = getRequiredPermissions()

        if (permissions.all {
                ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
            }) {
            // All permissions are already granted
            callback(true)
            return
        }

        // Request permissions
        permissionsLauncher?.launch(permissions) ?: run {
            // Fallback if launcher is not initialized
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                REQUEST_PERMISSIONS
            )
        }
    }

    /**
     * Handle permission result (for legacy permission handling)
     */
    fun handleRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS) {
            val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            onPermissionsGranted?.invoke(allGranted)
        }
    }

    /**
     * Handle activity result (for legacy Bluetooth enable handling)
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            onBluetoothEnabled?.invoke(resultCode == Activity.RESULT_OK)
        }
    }
}