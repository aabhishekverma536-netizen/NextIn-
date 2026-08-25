package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.concurrent.ConcurrentHashMap

object QueueAlertManager {

    private const val CHANNEL_ID = "nextin_queue_alerts_channel"
    private const val CHANNEL_NAME = "NextIn Queue & Turn Alerts"
    private const val CHANNEL_DESCRIPTION = "Urgent notifications when your turn is coming up at the counter"
    private const val NOTIFICATION_ID = 5055

    // Track which bookings have received the 5-minute alert to prevent duplicate spamming
    private val alertedBookings = ConcurrentHashMap<String, Boolean>()

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val vibrationPattern = longArrayOf(0, 450, 150, 450, 150, 750)

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#D4AF37")
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if the 5-minute or <= 2 people ahead condition is met, and triggers hardware vibration + notification.
     */
    fun checkAndTriggerQueueAlert(
        context: Context,
        bookingId: String,
        firmName: String,
        estimatedMinutes: Int,
        peopleAhead: Int
    ): Boolean {
        // Trigger condition: wait time <= 5 minutes OR exactly 2 or fewer people ahead
        val shouldAlert = (estimatedMinutes in 1..5) || (peopleAhead in 0..2 && estimatedMinutes <= 7)

        if (shouldAlert && alertedBookings[bookingId] != true) {
            alertedBookings[bookingId] = true
            triggerAlert(
                context = context,
                firmName = firmName,
                estimatedMinutes = estimatedMinutes,
                peopleAhead = peopleAhead
            )
            return true
        }
        return false
    }

    /**
     * Manually triggers the 5-minute alert (useful for test buttons and instant preview).
     */
    fun triggerAlert(
        context: Context,
        firmName: String = "Luxe Salon & Clinic",
        estimatedMinutes: Int = 5,
        peopleAhead: Int = 2
    ) {
        initNotificationChannel(context)

        // 1. Trigger distinct hardware vibration
        performHardwareVibration(context)

        // 2. Post high-priority urgent Notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertMessage = "Aapka turn bas aane wala hai! Please reach the counter. (Remaining: ~${estimatedMinutes} mins)"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("NextIn | Turn Alert 🔔 ($firmName)")
            .setContentText(alertMessage)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$alertMessage\n\nPeople ahead: $peopleAhead customer(s). Token will be called shortly.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#8B0000"))
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 450, 150, 450, 150, 750))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Performs a distinct, repeating tactile haptic pattern on the device vibrator.
     */
    fun performHardwareVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { vib ->
                if (vib.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Pattern: Pause 0ms, Vib 450ms, Pause 150ms, Vib 450ms, Pause 150ms, Vib 750ms
                        val timings = longArrayOf(0, 450, 150, 450, 150, 750)
                        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                        vib.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        val pattern = longArrayOf(0, 450, 150, 450, 150, 750)
                        @Suppress("DEPRECATION")
                        vib.vibrate(pattern, -1)
                    }
                }
            }
        } catch (_: Exception) {
            // Safe fallback if hardware vibration is restricted on specific test devices
        }
    }

    /**
     * Resets alert status for testing or when a user books a new token.
     */
    fun resetAlertStatus(bookingId: String) {
        alertedBookings.remove(bookingId)
    }
}
