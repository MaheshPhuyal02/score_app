package com.tm.score_app.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType.ANALOG
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Timer
import java.util.TimerTask
import kotlin.math.cos
import kotlin.math.sin

class CustomWatchFaceService : WatchFaceService() {

    override fun createUserStyleSchema(): UserStyleSchema {
        return UserStyleSchema(emptyList())
    }

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        return WatchFace(
            ANALOG,
            CustomCanvasRenderer(
                context = applicationContext,
                surfaceHolder = surfaceHolder,
                watchState = watchState,
                complicationSlotsManager = complicationSlotsManager,
                currentUserStyleRepository = currentUserStyleRepository,
                canvasType = CanvasType.SOFTWARE
            )
        )
    }
}

class CustomCanvasRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int
) : Renderer.CanvasRenderer(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = canvasType,
    interactiveDrawModeUpdateDelayMillis = 1000L
) {
    private val USERNAME = "maheshbca"
    private val timer = Timer()

    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
    }

    private val hourPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val minutePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val secondPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 4f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val numberPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val datePaint = Paint().apply {
        color = Color.WHITE
        textSize = 16f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val userPaint = Paint().apply {
        color = Color.LTGRAY
        textSize = 16f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    init {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                invalidate()
            }
        }, 0, 1000)
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
        // Not used
    }

    override fun render(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
        val width = bounds.width()
        val height = bounds.height()
        val centerX = width / 2f
        val centerY = height / 2f

        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Draw hour markers (5, 10, 15, 20)
        drawHourMarkers(canvas, centerX, centerY, width)

        // Format UTC date exactly as requested
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val formattedDate = utcDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        // Draw the formatted date and username
        canvas.drawText("Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted):",
            centerX, centerY - 70f, datePaint)
        canvas.drawText(formattedDate, centerX, centerY - 50f, datePaint)
        canvas.drawText("Current User's Login: $USERNAME", centerX, centerY + 70f, userPaint)

        // Calculate hand positions
        val seconds = zonedDateTime.second + zonedDateTime.nano / 1_000_000_000f
        val minutes = zonedDateTime.minute + seconds / 60
        val hours = zonedDateTime.hour % 12 + minutes / 60

        val secLength = centerX * 0.8f
        val minLength = centerX * 0.7f
        val hrLength = centerX * 0.5f

        val secRot = seconds * 6f
        val minRot = minutes * 6f
        val hrRot = hours * 30f

        // Draw hands
        canvas.drawLine(
            centerX,
            centerY,
            centerX + sin(Math.toRadians(secRot.toDouble())).toFloat() * secLength,
            centerY - cos(Math.toRadians(secRot.toDouble())).toFloat() * secLength,
            secondPaint
        )

        canvas.drawLine(
            centerX,
            centerY,
            centerX + sin(Math.toRadians(minRot.toDouble())).toFloat() * minLength,
            centerY - cos(Math.toRadians(minRot.toDouble())).toFloat() * minLength,
            minutePaint
        )

        canvas.drawLine(
            centerX,
            centerY,
            centerX + sin(Math.toRadians(hrRot.toDouble())).toFloat() * hrLength,
            centerY - cos(Math.toRadians(hrRot.toDouble())).toFloat() * hrLength,
            hourPaint
        )

        // Center dot
        canvas.drawCircle(centerX, centerY, 8f, hourPaint)
    }

    private fun drawHourMarkers(canvas: Canvas, centerX: Float, centerY: Float, size: Int) {
        val radius = size / 2f - 20f

        for (i in 1..12) {
            val angle = Math.PI * 2 * (i - 3) / 12
            val x = (centerX + cos(angle) * radius).toFloat()
            val y = (centerY + sin(angle) * radius).toFloat() + 10f

            if (i % 3 == 0) {
                val hourMarker = when (i) {
                    3 -> "5"
                    6 -> "10"
                    9 -> "15"
                    12 -> "20"
                    else -> i.toString()
                }
                canvas.drawText(hourMarker, x, y, numberPaint)
            }
        }
    }
}