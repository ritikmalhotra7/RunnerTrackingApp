package com.example.runnertrackingapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.ui.activities.MainActivity
import com.example.runnertrackingapp.utils.Utils
import com.example.runnertrackingapp.utils.Utils.ACTION_PAUSE_SERVICE
import com.example.runnertrackingapp.utils.Utils.ACTION_START_OR_RESUME_SERVICE
import com.example.runnertrackingapp.utils.Utils.ACTION_STOP_SERVICE
import com.example.runnertrackingapp.utils.Utils.FASTEST_LOCATION_INTERVAL
import com.example.runnertrackingapp.utils.Utils.LOCATION_UPDATE_INTERVAL
import com.example.runnertrackingapp.utils.Utils.NOTIFICATION_ID
import com.example.runnertrackingapp.utils.Utils.TIMER_UPDATE_INTERVAL
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {
    private var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var timeRunInSeconds = MutableLiveData<Long>()
    private var isTimerEnabled = false
    //start and pause time
    private var lapTime = 0L
    //total time
    private var timeRun = 0L
    //saving time started
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    companion object {
        //give info that we are tracking or not
        val isTracking = MutableLiveData<Boolean>()

        //having info about location points and
        val pathPoints = MutableLiveData<Polylines>()

        //give run time to observe
        val timeRunInMillis = MutableLiveData<Long>()
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        observeIsTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //null check
        intent?.let {
            //checking which action is called
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    //start only on first run else resume
                    if (isFirstRun) {
                        isFirstRun = false
                        Timber.d("Started service")
                    } else {
                        Timber.d("Resumed service")
                        startTimer()
                    }
                    startForeGroundService()
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped Service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //giving initial values
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoints(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    //fetching location
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (isTracking.value!!) {
                p0?.locations?.let { locations ->
                    locations.forEach { location ->
                        addPathPoints(location)
                        Timber.d("Location:${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    //updating location
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (Utils.hasLocationPermissions(this)) {
                //defining request to have location in an certain interval
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                //this will attach locationCallback
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            //this will detach callback
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    //updating location according to isTracking
    private fun observeIsTracking() {
        isTracking.observe(this) {
            updateLocationTracking(it)
        }
    }

    private fun startForeGroundService() {
        startTimer()
        //whenever service is starting giving value to isTracking
        isTracking.postValue(true)

        //creating notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val notificationBuilder =
            NotificationCompat.Builder(this, Utils.NOTIFICATION_CHANNEL_ID).apply {
                setAutoCancel(false)
                setOngoing(true)
                setSmallIcon(R.drawable.icons8_direction)
                setContentTitle("Running App")
                setContentText("00:00:00")
                //onclick of notification
                setContentIntent(getMainActivityPendingIntent())
            }
        //starting notification
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Utils.NOTIFICATION_CHANNEL_ID,
            Utils.NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun getMainActivityPendingIntent(): PendingIntent {
        //giving a pending intent what to do on click
        return PendingIntent.getActivity(
            this,
            0,
            //task to do
            Intent(this, MainActivity::class.java).also {
                it.action = Utils.ACTION_SHOW_TRACKING_FRAGMENT
            },
            FLAG_MUTABLE
        )
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while(isTracking.value!!){
                //time difference between now and time started
                lapTime = System.currentTimeMillis()-timeStarted
                timeRunInMillis.postValue(timeRun+lapTime)
                //post new lapTime
                if(timeRunInMillis.value!!>=lastSecondTimeStamp+1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!!+1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

}