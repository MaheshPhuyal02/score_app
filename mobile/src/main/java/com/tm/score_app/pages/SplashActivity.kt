package com.tm.score_app.pages

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tm.score_app.pages.ui.theme.Score_appTheme

class SplashActivity : ComponentActivity() {

    // Define ONLY location and Bluetooth permissions
    private val requiredPermissions = mutableListOf<String>().apply {
        // Location permissions
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Bluetooth permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Android 11 and below
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
        }
    }.toTypedArray()

    // Modern Activity Result API launcher for Bluetooth enabling
    private lateinit var bluetoothEnableLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register bluetooth enable launcher with the Activity Result API
        bluetoothEnableLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // Regardless of whether user enabled Bluetooth or not, proceed
            navigateToMainScreen()
        }

        enableEdgeToEdge()
        setContent {
            Score_appTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SplashScreen(
                        requiredPermissions = requiredPermissions,
                        modifier = Modifier.padding(innerPadding),
                        onAllPermissionsGranted = {
                            // First check if Bluetooth needs to be enabled
                            checkBluetoothEnabled()
                        }
                    )
                }
            }
        }
    }

    private fun checkBluetoothEnabled() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(enableBtIntent)
        } else {
            navigateToMainScreen()
        }
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun SplashScreen(
    requiredPermissions: Array<String>,
    modifier: Modifier = Modifier,
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var permissionsToRequest by remember { mutableStateOf(requiredPermissions.toList()) }
    var allPermissionsGranted by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(true) }

    // Check which permissions are already granted
    LaunchedEffect(Unit) {
        permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        // If all permissions are already granted, proceed immediately
        if (permissionsToRequest.isEmpty()) {
            allPermissionsGranted = true
            // Small delay to show splash screen briefly
            kotlinx.coroutines.delay(1000)
            onAllPermissionsGranted()
        } else {
            showLoading = false
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        // Check if all requested permissions were granted
        val allGranted = permissionsResult.all { it.value }

        if (allGranted) {
            allPermissionsGranted = true
            onAllPermissionsGranted()
        } else {
            // Some permissions were denied
            permissionsToRequest = permissionsToRequest.filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }

            // If all permissions are granted after filtering, proceed
            if (permissionsToRequest.isEmpty()) {
                allPermissionsGranted = true
                onAllPermissionsGranted()
            } else {
                // Show rationale for denied permissions
                showPermissionRationale = true
                Toast.makeText(context, "Location and Bluetooth permissions are needed for the app to function correctly.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // App Logo/Splash content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Score App",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Permissions UI - ONLY shows for location & Bluetooth
        AnimatedVisibility(
            visible = !showLoading && !allPermissionsGranted,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (showPermissionRationale)
                        "Location and Bluetooth permissions are required for connecting with nearby devices."
                    else
                        "Please grant the following permissions:",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Show only location and Bluetooth permissions
                permissionsToRequest.forEach { permission ->
                    val permissionName = when {
                        permission.contains("BLUETOOTH") -> "Bluetooth"
                        permission.contains("LOCATION") -> "Location"
                        else -> permission.split(".").last()
                    }
                    Text(
                        text = "• $permissionName",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (permissionsToRequest.isNotEmpty()) {
                            // Launch permission request
                            permissionLauncher.launch(permissionsToRequest.toTypedArray())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Grant Permissions")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Score_appTheme {
        SplashScreen(
            requiredPermissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT
            ),
            onAllPermissionsGranted = {}
        )
    }
}