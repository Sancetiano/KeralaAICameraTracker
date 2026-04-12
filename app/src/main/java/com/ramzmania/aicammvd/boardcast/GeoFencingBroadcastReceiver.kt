package com.ramzmania.aicammvd.boardcast

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.ramzmania.aicammvd.R
import com.ramzmania.aicammvd.geofencing.playNotificationSound
import com.ramzmania.aicammvd.geofencing.speak
import com.ramzmania.aicammvd.ui.screens.mapview.OsmMapActivity
import com.ramzmania.aicammvd.utils.Constants
import com.ramzmania.aicammvd.utils.Constants.GEOFENCE_PENDING_INTENT_ID
import com.ramzmania.aicammvd.utils.Logger
import com.ramzmania.aicammvd.utils.NotificationUtil
import com.ramzmania.aicammvd.utils.PreferencesUtil
import java.util.Locale

/**
 * Broadcast receiver for handling geofence events.
 */
class GeoFencingBroadcastReceiver : BroadcastReceiver() {
    /**
     * Handles incoming geofence events.
     *
     * @param context The application context.
     * @param intent The intent containing the geofence event.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        Logger.d("GeofenceBroadcastReceiver - Geofence triggered")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            return
        }
        
        val location = geofencingEvent.triggeringLocation
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        if (triggeringGeofences != null) {
            for (geofence in triggeringGeofences) {
                val requestId = geofence.requestId
                // RequestId is formatted as "location_name_distance"
                val lastUnderscoreIndex = requestId.lastIndexOf("_")
                val triggeredLocationName = if (lastUnderscoreIndex != -1) {
                    requestId.substring(0, lastUnderscoreIndex).replace("*", " ").uppercase(Locale.getDefault())
                } else {
                    requestId.replace("*", " ").uppercase(Locale.getDefault())
                }
                
                val distanceStr = if (lastUnderscoreIndex != -1) requestId.substring(lastUnderscoreIndex + 1) else ""

                when (geofencingEvent.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        if (PreferencesUtil.isTrackerRunning(context)) {
                            handleEnterTransition(context, triggeredLocationName, distanceStr, location)
                        }
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        if (PreferencesUtil.isTrackerRunning(context)) {
                            // baseId is used to identify the camera regardless of the geofence circle distance
                            val baseId = requestId.substring(0, if (lastUnderscoreIndex != -1) lastUnderscoreIndex else requestId.length)
                            if (!PreferencesUtil.isAlreadyNotified(context, baseId)) {
                                handleExitTransition(context, triggeredLocationName, location)
                                PreferencesUtil.setLastPassedCamera(context, baseId)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleEnterTransition(context: Context, locationName: String, distance: String, location: Location?) {
        val title = "AI CAMERA AHEAD"
        val message = "$locationName Camera within $distance meters"
        
        showNotification(context, title, message, location)
        
        val alertType = PreferencesUtil.getString(context, Constants.PREF_ALERT_TYPE) ?: "sound"
        if (alertType == "voice") {
            speak(context, "AI Camera is within $distance meters")
        } else {
            playNotificationSound(context, R.raw.notification_sound)
        }
    }

    private fun handleExitTransition(context: Context, locationName: String, location: Location?) {
        val shouldNotifyExit = PreferencesUtil.getBoolean(context, Constants.PREF_POST_PASS_NOTIFY, true)
        
        if (shouldNotifyExit) {
            val speedKmph = location?.speed?.let { it * 3.6 }?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "unknown"
            val title = "CAMERA PASSED"
            val message = "You just passed $locationName AI camera. Your speed was $speedKmph kmph."
            
            showNotification(context, title, message, location)
            
            val alertType = PreferencesUtil.getString(context, Constants.PREF_ALERT_TYPE) ?: "sound"
            if (alertType == "voice") {
                speak(context, "You just passed $locationName camera and your speed was $speedKmph kilometers per hour")
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String, location: Location?) {
        NotificationUtil.createNotificationChannel(context, Constants.CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)

        val intent = Intent(context, OsmMapActivity::class.java).apply {
            putExtra("lat", location?.latitude ?: 0.0)
            putExtra("long", location?.longitude ?: 0.0)
            putExtra(Constants.INTENT_FROM_GEO, "geofence")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationUtil.showNotification(
                context, title, message, pendingIntent, R.drawable.ai_camera_marker,
                NotificationCompat.PRIORITY_HIGH, GEOFENCE_PENDING_INTENT_ID, Constants.CHANNEL_ID, true
            )
        }
    }
}
