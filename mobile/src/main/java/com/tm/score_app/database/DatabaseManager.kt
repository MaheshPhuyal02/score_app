import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.tm.score_app.models.Device


class DatabaseManager(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "DatabaseManager"

        // Database Version
        private const val DATABASE_VERSION = 1

        // Database Name
        private const val DATABASE_NAME = "devices.db"

        // Table Name
        private const val TABLE_DEVICES = "device_table"

        // Column Names
        private const val COLUMN_DEVICE_ID = "device_id"
        private const val COLUMN_DEVICE_NAME = "device_name"
        private const val COLUMN_DEVICE_TYPE = "device_type"
        private const val COLUMN_DEVICE_STATUS = "device_status"
        private const val COLUMN_SCORE = "score"
        private const val COLUMN_HEART_RATE = "heart_rate"
        private const val COLUMN_IS_MINE = "is_mine"
        private const val COLUMN_IS_PAIRED = "is_paired"
        private const val COLUMN_IS_SYNCED = "is_synced"
        private const val COLUMN_COLOR = "color"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Creating device table with all required columns
        val CREATE_DEVICE_TABLE = ("CREATE TABLE $TABLE_DEVICES (" +
                "$COLUMN_DEVICE_ID TEXT PRIMARY KEY, " +
                "$COLUMN_DEVICE_NAME TEXT, " +
                "$COLUMN_DEVICE_TYPE TEXT, " +
                "$COLUMN_DEVICE_STATUS TEXT, " +
                "$COLUMN_SCORE REAL, " +
                "$COLUMN_HEART_RATE REAL, " +
                "$COLUMN_IS_MINE INTEGER, " +
                "$COLUMN_IS_PAIRED INTEGER, " +
                "$COLUMN_IS_SYNCED INTEGER, " +
                "$COLUMN_COLOR INTEGER)")

        db.execSQL(CREATE_DEVICE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DEVICES")

        // Create tables again
        onCreate(db)
    }

    // Add a new device to the database
    fun addDevice(device: Device) {
        val db = this.writableDatabase

        try {
            val values = ContentValues().apply {
                put(COLUMN_DEVICE_ID, device.deviceId)
                put(COLUMN_DEVICE_NAME, device.deviceName)
                put(COLUMN_DEVICE_TYPE, device.deviceType.toString())
                put(COLUMN_DEVICE_STATUS, device.deviceStatus)
                put(COLUMN_SCORE, device.score)
                put(COLUMN_HEART_RATE, device.heartRate)
                put(COLUMN_IS_MINE, if (device.isMine) 1 else 0)
                put(COLUMN_IS_PAIRED, if (device.isPaired) 1 else 0)
                put(COLUMN_IS_SYNCED, if (device.isSynced) 1 else 0)
                put(COLUMN_COLOR, device.color)
            }

            // Insert the new row
            db.insert(TABLE_DEVICES, null, values)
            Log.d(TAG, "Device added: ${device.deviceName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding device", e)
        } finally {
            db.close()
        }
    }

    // Remove a device from the database
    fun removeDevice(device: Device) {
        val db = this.writableDatabase

        try {
            db.delete(TABLE_DEVICES, "$COLUMN_DEVICE_ID = ?", arrayOf(device.deviceId))
            Log.d(TAG, "Device removed: ${device.deviceName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing device", e)
        } finally {
            db.close()
        }
    }

    // Update the score for a device
    fun updateScore(device: Device) {
        val db = this.writableDatabase

        try {
            val values = ContentValues().apply {
                put(COLUMN_SCORE, device.score)
            }

            db.update(TABLE_DEVICES, values, "$COLUMN_DEVICE_ID = ?", arrayOf(device.deviceId))
            Log.d(TAG, "Score updated for device: ${device.deviceName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating score", e)
        } finally {
            db.close()
        }
    }

    // Update heart rate for a device by ID
    fun updateHeartRate(deviceId: String, heartRate: Double) {
        val db = this.writableDatabase

        try {
            val values = ContentValues().apply {
                put(COLUMN_HEART_RATE, heartRate)
            }

            db.update(TABLE_DEVICES, values, "$COLUMN_DEVICE_ID = ?", arrayOf(deviceId))
            Log.d(TAG, "Heart rate updated for device ID: $deviceId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating heart rate", e)
        } finally {
            db.close()
        }
    }

    // Get list of all devices
    fun getDeviceList(
        deviceType: DeviceType
    ): ArrayList<Device> {
        val deviceList = ArrayList<Device>()
        val selectQuery = "SELECT * FROM $TABLE_DEVICES WHERE $COLUMN_DEVICE_TYPE = '${deviceType.toString()}'"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)

            if (cursor?.moveToFirst() == true) {
                do {
                    val device = Device(
                        deviceId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_ID)),
                        deviceName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_NAME)),
                        deviceType =
                            DeviceType.valueOf(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_TYPE))),
                        deviceStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_STATUS)),
                        score = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SCORE)),
                        heartRate = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HEART_RATE)),
                        isMine = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_MINE)) == 1,
                        isPaired = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PAIRED)) == 1,
                        isSynced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SYNCED)) == 1,
                        color = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR)),
                    )
                    deviceList.add(device)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device list", e)
        } finally {
            cursor?.close()
            db.close()
        }

        return deviceList
    }

    // Get my watch (device where isMine = true and type is watch)
    fun getMyWatch(): Device? {
        var myWatch: Device? = null
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            val query = "SELECT * FROM $TABLE_DEVICES WHERE $COLUMN_IS_MINE = 1 AND $COLUMN_DEVICE_TYPE = 'watch' LIMIT 1"
            cursor = db.rawQuery(query, null)

            if (cursor?.moveToFirst() == true) {
                myWatch = Device(
                    deviceId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_ID)),
                    deviceName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_NAME)),
                    deviceType =
                        DeviceType.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_TYPE))),
                    deviceStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_STATUS)),
                    score = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SCORE)),
                    heartRate = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HEART_RATE)),
                    isMine = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_MINE)) == 1,
                    isPaired = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PAIRED)) == 1,
                    isSynced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SYNCED)) == 1,
                    color = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR))
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting my watch", e)
        } finally {
            cursor?.close()
            db.close()
        }

        return myWatch
    }
}