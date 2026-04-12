package com.ramzmania.aicammvd.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context


import androidx.work.Worker
import androidx.work.WorkerParameters
import android.location.Location
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import com.ramzmania.aicammvd.R
import com.ramzmania.aicammvd.boardcast.homePagePendingIntent
import com.ramzmania.aicammvd.boardcast.stopAiTrackerPendingIntent
import com.ramzmania.aicammvd.data.local.LocalRepository
import com.ramzmania.aicammvd.geofencing.LocationUtils
import com.ramzmania.aicammvd.utils.Constants
import com.ramzmania.aicammvd.utils.Constants.CHANNEL_ID
import com.ramzmania.aicammvd.utils.Constants.FAKE_SERVICE_NOTIFICATION_ID
import com.ramzmania.aicammvd.utils.Logger
import com.ramzmania.aicammvd.utils.PreferencesUtil
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.ramzmania.aicammvd.geofencing.calculateDistance
import com.ramzmania.aicammvd.geofencing.getAllLocationList
import com.ramzmania.aicammvd.utils.Constants.LOCATION_WORK_MANAGER_TAG
import java.util.concurrent.TimeUnit

/**
 * Worker class responsible for fetching and updating location data in the background.
 *
 * @param context The application context.
 * @param workerParams The worker parameters.
 * @param localRepository The repository for accessing local data.
 */
@HiltWorker
class LocationWorker @AssistedInject constructor(
    context: Context,
    workerParams: WorkerParameters,
    private val localRepository: LocalRepository
) : Worker(context, workerParams) {


    init {
        createNotificationChannel()
    }

    override fun doWork(): Result {
        if(PreferencesUtil.isTrackerRunning(applicationContext)) {
            val latch = CountDownLatch(1)
            var result: Result = Result.failure()

            // Initialize LocationUtils for fetching location updates
            val locationUtils = LocationUtils(applicationContext)
            locationUtils.startLocationUpdates(object : LocationUtils.LocationListener {
                override fun onLocationResult(location: Location?) {
                    location?.let {
                        // Log or handle the location

                        CoroutineScope(Dispatchers.IO).launch {
                            Logger.d(
                                "Current location: Latitude ${it.latitude}, Longitude ${it.longitude}"
                            )

                            // Update the AI camera circle with the new location
                            localRepository.setNewAiCameraCircle(it.latitude, it.longitude)
                                .collect { response ->
                                    if (response.data == true) {
                                        Logger.d("SUCCESS FULL WORKER")
                                    } else {
                                        Logger.d("FAILED WORKER")

                                    }

                                }
                        }
                        Logger.d("Updated location: Latitude ${it.latitude}, Longitude ${it.longitude}")
                        updateNotification()
                        
                        CoroutineScope(Dispatchers.IO).launch {
                            val nearestDistance = getDistanceToNearestCamera(it)
                            var nextDelaySeconds = 15 * 60L // Default 15 mins

                            if (nearestDistance <= 2.0) { // Within 2km (Active Zone)
                                nextDelaySeconds = 5L // 5 seconds
                            } else if (nearestDistance <= 5.0) {
                                nextDelaySeconds = 60L // 1 minute
                            } else if (nearestDistance <= 10.0) {
                                nextDelaySeconds = 5 * 60L // 5 minutes
                            }

                            Logger.d("Nearest camera distance: $nearestDistance km. Next check in $nextDelaySeconds seconds.")
                            scheduleNextWork(nextDelaySeconds)
                            result = Result.success()
                            latch.countDown()
                        }
                    }
                }

                override fun onLocationError(e: Exception) {
                    Logger.e("Error fetching location", e)
                    result = Result.failure()
                    latch.countDown()
                }
            })

            try {
                latch.await()  // Wait for the location update to complete
            } catch (e: InterruptedException) {
                return Result.failure()
            } finally {
                locationUtils.stopLocationUpdates()  // Make sure to stop updates to avoid leaks
            }
            return result
        }else
        {
            return Result.success()
        }

    }

    /**
     * Creates a notification channel for location updates.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Location Updates"
            val descriptionText = "Channel for Location Updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Updates the notification for ongoing location tracking.
     */
    private fun updateNotification() {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.red_location) // Replace with actual icon drawable
            .setContentTitle(Constants.NOTIFY_TRACKING_TITLE)
            .setContentText(Constants.NOTIFY_TRACKING_SUBTITLE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(homePagePendingIntent(applicationContext))


        builder.setAutoCancel(false)
        builder.addAction(
            R.drawable.stop,
            Constants.NOTIFY_STOP_ACTION_TITLE,
            stopAiTrackerPendingIntent(applicationContext)
        )

        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(FAKE_SERVICE_NOTIFICATION_ID, builder.build())
    }

    private suspend fun getDistanceToNearestCamera(location: Location): Double {
        val fullCameraList = getAllLocationList(applicationContext)
        var minDistance = Double.MAX_VALUE
        fullCameraList?.forEach { camera ->
            val dist = calculateDistance(
                location.latitude, location.longitude,
                camera.latitude, camera.longitude
            )
            if (dist < minDistance) minDistance = dist
        }
        return minDistance // Distance in km
    }

    private fun scheduleNextWork(delaySeconds: Long) {
        if (!PreferencesUtil.isTrackerRunning(applicationContext)) return

        val workRequest = OneTimeWorkRequest.Builder(LocationWorker::class.java)
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .addTag(LOCATION_WORK_MANAGER_TAG)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            LOCATION_WORK_MANAGER_TAG,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}