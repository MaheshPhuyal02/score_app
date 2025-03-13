package com.tm.score_app.pages

import DatabaseManager
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tm.score_app.R
import com.tm.score_app.models.Device
import kotlinx.coroutines.launch

class ScanDeviceActivity : ComponentActivity() {
    // Initialize services
    private lateinit var bluetoothService: BluetoothService
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize services
        bluetoothService = BluetoothService(this)
        databaseManager = DatabaseManager(this)

        // Check for required permissions before proceeding
        if (!hasRequiredPermissions()) {
            requestPermissions()
        }

        setContent {
            MaterialTheme {
                BluetoothScannerScreen(
                    bluetoothService = bluetoothService,
                    databaseManager = databaseManager,
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions as Array<String>, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, we're good to go
            } else {

                Toast.makeText(this, "Bluetooth scanning requires permissions", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onDestroy() {
        super.onDestroy()
        bluetoothService.cleanup()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}

@Composable
fun BluetoothScannerScreen(
    bluetoothService: BluetoothService,
    databaseManager: DatabaseManager,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Collect real scanning state from BluetoothService
    val isScanning by bluetoothService.isScanning.collectAsState()

    // Collect discovered devices from BluetoothService
    val discoveredDevices by bluetoothService.discoveredDevicesFlow.collectAsState()

    val existingDevices = remember { mutableStateListOf<Device>() }
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Load existing devices from database
    LaunchedEffect(Unit) {
        existingDevices.clear()
        existingDevices.addAll(databaseManager.getDeviceList(

        ))
    }

    // Start scanning for devices when the screen loads
    LaunchedEffect(Unit) {
        // Start actual Bluetooth scanning
        bluetoothService.scanDevices()
    }

    // Add device dialog
    if (showAddDialog && selectedDevice != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Device") },
            text = { Text("Do you want to add ${selectedDevice?.deviceName} to your devices?") },
            confirmButton = {
                TextButton(onClick = {
                    selectedDevice?.let {
                        // Add to database
                        databaseManager.addDevice(it)

                        // Add to list of existing devices
                        if (!existingDevices.contains(it)) {
                            existingDevices.add(it)
                        }

                        Toast.makeText(context, "Device added successfully", Toast.LENGTH_SHORT).show()
                    }
                    showAddDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Add Users",
                color = Color.Green,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black,
                tonalElevation = 4.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Add Device",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Make sure your device is turned on.",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )

                    // Searching with spinner
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isScanning) "Searching Devices..." else "Found Devices",
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        // Animated loading spinner when scanning
                        AnimatedVisibility(
                            visible = isScanning,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "spinner")
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "rotation"
                            )

                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show devices
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Show only devices that are not already in the database
                        val devicesToShow = discoveredDevices.filter { foundDevice ->
                            existingDevices.none { it.deviceId == foundDevice.deviceId }
                        }

                        if (devicesToShow.isEmpty() && !isScanning) {
                            item {
                                Text(
                                    text = "No new devices found",
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .padding(vertical = 16.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        items(devicesToShow) { device ->
                            DeviceItem(
                                deviceName = device.deviceName,
                                deviceType = if (device.deviceType == DeviceType.WATCH) "Watch" else calculateDistance(device),
                                iconColor = getDeviceColor(device),
                                isWatch = device.deviceType == DeviceType.WATCH,
                                onClick = {
                                    selectedDevice = device
                                    showAddDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rescan button when scan is complete
                    AnimatedVisibility(visible = !isScanning) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    // Start real Bluetooth scanning again
                                    bluetoothService.scanDevices()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Green)
                        ) {
                            Text(
                                text = "Scan Again",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Cancel button
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(
    deviceName: String,
    deviceType: String,
    iconColor: Color,
    isWatch: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        // Icon in circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = if (isWatch) R.drawable.watch_search else R.drawable.phone),
                contentDescription = "Device Icon",
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = deviceName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = deviceType,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

// Helper functions
private fun getDeviceColor(device: Device): Color {
    // Consistent color for the same device ID
    return when {
        device.deviceType == DeviceType.WATCH -> Color(0xFFE040FB) // Purple for watches
        device.deviceId.hashCode() % 5 == 0 -> Color(0xFFFF9800) // Orange
        device.deviceId.hashCode() % 5 == 1 -> Color(0xFF4CAF50) // Green
        device.deviceId.hashCode() % 5 == 2 -> Color(0xFF2196F3) // Blue
        device.deviceId.hashCode() % 5 == 3 -> Color(0xFFFF5252) // Red
        else -> Color(0xFFFFEB3B) // Yellow
    }
}

private fun calculateDistance(device: Device): String {
    // For real device, use the RSSI (signal strength) to approximate distance
    // This is just a placeholder for now
    return if (device.deviceId.isNotEmpty()) {
        val firstDigit = device.deviceId.hashCode().toString().first().toString().toIntOrNull() ?: 1
        String.format("%.1f m", firstDigit.toDouble() + (device.deviceId.hashCode() % 10) / 10.0)
    } else {
        "Unknown"
    }
}