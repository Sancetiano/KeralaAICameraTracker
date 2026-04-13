package com.ramzmania.aicammvd.ui.screens.mapview

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ramzmania.aicammvd.R
import com.ramzmania.aicammvd.databinding.MapViewBinding
import com.ramzmania.aicammvd.geofencing.calculateDistance
import com.ramzmania.aicammvd.ui.base.BaseBinderActivity
import com.ramzmania.aicammvd.ui.screens.home.HomeActivity
import com.ramzmania.aicammvd.utils.Constants
import com.ramzmania.aicammvd.utils.Logger
import com.ramzmania.aicammvd.utils.MediaPlayerUtil
import com.ramzmania.aicammvd.viewmodel.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale
import java.util.ArrayList

/**
 * OsmMapActivity: An activity to display a map using OpenStreetMap (OSM) library.
 * This activity integrates Google's Fused Location Provider for user's location tracking.
 * It also includes functionalities to add markers on the map and update UI based on location changes.
 */
@AndroidEntryPoint
class OsmMapActivity : BaseBinderActivity<MapViewBinding, HomeViewModel>(), MapListener {

    // Variable to determine if distance should be shown
    private var showDistance = false

    // Map controller
    lateinit var controller: IMapController

    // Location overlay
    lateinit var mMyLocationOverlay: MyLocationNewOverlay

    // Media player utility for audio alerts
    private lateinit var mediaPlayerUtil: MediaPlayerUtil

    // Fused Location Provider client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Location callback for receiving location updates
    var locationCallback: LocationCallback? = null
    
    private var cameraPoint: GeoPoint? = null
    
    override fun getViewModelClass() = HomeViewModel::class.java

    override fun getViewBinding() = MapViewBinding.inflate(layoutInflater)

    override fun observeViewModel() {
    }

    override fun observeActivity() {
        if (intent.extras!!.containsKey("lat")) {
            showDistance = true
            binding.distanceLl.visibility = View.VISIBLE
            cameraPoint = GeoPoint(intent.extras!!.getDouble("lat"), intent.extras!!.getDouble("long"))
        }
        mediaPlayerUtil = MediaPlayerUtil(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        )
        
        binding.osmmap.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmmap.setMultiTouchControls(true)
        binding.osmmap.getLocalVisibleRect(Rect())

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), binding.osmmap)
        controller = binding.osmmap.controller

        // Enable location and follow location
        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        
        if (intent.extras!!.containsKey(Constants.INTENT_FROM_GEO)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        mMyLocationOverlay.setPersonAnchor(0.5f, 0.5f)

        cameraPoint?.let { point ->
            controller.setCenter(point)
            controller.setZoom(16.5)
            addMarker(binding.osmmap, point)
            
            // Run a one-time zoom-to-fit after a small delay to allow location to initialize
            mMyLocationOverlay.runOnFirstFix {
                val userLocation = mMyLocationOverlay.myLocation
                if (userLocation != null) {
                    runOnUiThread {
                        zoomToFit(userLocation, point)
                    }
                }
            }
        }

        binding.osmmap.overlays.add(mMyLocationOverlay)
        binding.osmmap.addMapListener(this)

        binding.btnNavigate.setOnClickListener {
            cameraPoint?.let { point ->
                val gmmIntentUri = Uri.parse("google.navigation:q=${point.latitude},${point.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    // Fallback to generic geo intent
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${point.latitude},${point.longitude}?q=${point.latitude},${point.longitude}"))
                    startActivity(intent)
                }
            }
        }

        /*Back press handler*/
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isTaskRoot) {
                    val intent = Intent(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    finish()
                }
            }
        })
    }

    private fun zoomToFit(userLocation: GeoPoint, cameraLocation: GeoPoint) {
        val points = ArrayList<GeoPoint>()
        points.add(userLocation)
        points.add(cameraLocation)
        val boundingBox = BoundingBox.fromGeoPoints(points)
        
        // Add some padding
        binding.osmmap.zoomToBoundingBox(boundingBox, true, 150)
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return false
    }

    // Add marker to the map
    private fun addMarker(mapView: MapView, point: GeoPoint) {
        val context = mapView.context
        val icon = context.resources.getDrawable(
            R.drawable.ai_camera_marker,
            context.theme
        ) // Load the custom marker drawable

        val marker = Marker(mapView)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = icon // Set the custom icon
        marker.title = "AI Camera Location"

        mapView.overlays.add(marker)
        mapView.invalidate() // Refresh the map to display the marker
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }
    }

    // Start location updates
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(1000)
            .build()

        locationCallback = object : LocationCallback() {
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    try {
                        if (showDistance) {
                            val dist = calculateDistance(
                                        location.latitude,
                                        location.longitude,
                                        intent.extras!!.getDouble("lat"),
                                        intent.extras!!.getDouble("long")
                                    )
                            binding.distanceTxt.text =
                                "DISTANCE TO CAM : " + String.format(
                                    Locale.getDefault(),
                                    "%.1f",
                                    dist
                                ) + " KM"
                        }
                    } catch (ex: Exception) {
                        binding.distanceTxt.text = "error"
                    }

                    val speed = location.speed // Speed in meters/second
                    if ((speed * 3.6) > 80) {
                        binding.speedTxt.setBackgroundResource(R.drawable.rounded_overspeed_text_background)
                        if (!mediaPlayerUtil.isPlayingSound()) {
                            mediaPlayerUtil.playSound(R.raw.overspeed)
                        }
                    } else if ((speed * 3.6) < 80 && (speed * 3.6) >= 60) {
                        binding.speedTxt.setBackgroundResource(R.drawable.rounded_warningspeed_text_background)
                    } else {
                        binding.speedTxt.setBackgroundResource(R.drawable.rounded_normalspeed_text_background)
                    }
                    val speedKmH = String.format(Locale.getDefault(), "%.1f", speed * 3.6)
                    binding.speedTxt.text = "Speed\n $speedKmH Km/H"
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            null /* Looper */
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mediaPlayerUtil != null) {
                mediaPlayerUtil.stopSound()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        try {
            stopLocationUpdates()
        } catch (ex: Exception) {
        }
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
            locationCallback = null
        }
    }
}
