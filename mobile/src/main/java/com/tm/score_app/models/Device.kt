package com.tm.score_app.models

import DeviceType

data class Device(
    var deviceId: String,
    var deviceName: String,
    var deviceType: DeviceType,
    var deviceStatus: String,
    var score: Double,
    var heartRate: Double,
    var isMine: Boolean,
    var isPaired: Boolean,
    var isSynced: Boolean,
    var color: Int,
    var isConnected:Boolean = false,
    var address: String = ""

)