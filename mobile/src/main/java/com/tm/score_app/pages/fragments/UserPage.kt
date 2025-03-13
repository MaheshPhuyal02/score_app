package com.tm.score_app.pages.fragments

import DatabaseManager
import DeviceType
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tm.score_app.R
import com.tm.score_app.pages.ScanDeviceActivity

data class ConnectedDevice(
    val name: String,
    val score: Int,
    val color: Color,
    val isWatch: Boolean
)

@Composable
fun ConnectedDevicesList(context: Context) {
    val databaseManager = remember { DatabaseManager(context) }
    val devices = remember { mutableStateListOf<ConnectedDevice>() }

    // Load connected devices from database when the screen appears
    LaunchedEffect(key1 = Unit) {
        loadConnectedDevices(databaseManager, devices)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Connected Devices",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No devices connected",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn {
                items(devices) { device ->
                    DeviceCard(device)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                context.startActivity(Intent(context, ScanDeviceActivity::class.java))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add device", color = Color.White)
        }
    }
}

// Function to load connected devices from the database
private fun loadConnectedDevices(
    databaseManager: DatabaseManager,
    devicesList: SnapshotStateList<ConnectedDevice>
) {
    // Clear the current list
    devicesList.clear()

    // Get all devices from the database
    val allDevices = databaseManager.getDeviceList(DeviceType.WATCH)

    // Filter for only connected devices and convert to UI model
    val connectedDevices = allDevices
        .filter { it.deviceStatus == "connected" }
        .map { device ->
            ConnectedDevice(
                name = device.deviceName,
                score = device.score.toInt(),
                color = Color(device.color),
                isWatch = device.deviceType == DeviceType.WATCH
            )
        }

    // Add all connected devices to our list
    devicesList.addAll(connectedDevices)
}

@Composable
fun DeviceCard(device: ConnectedDevice) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(device.color, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (device.isWatch) R.drawable.watch_search else R.drawable.phone
                    ),
                    contentDescription = "Device Icon",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = device.name,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .background(
                        color = if (device.score > 0) Color(0xFF1A5E1A) else Color(0xFF5E1A1A),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = device.score.toString(),
                    color = if (device.score > 0) Color.Green else Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun UserPage(context: Context) {
    ConnectedDevicesList(context)
}