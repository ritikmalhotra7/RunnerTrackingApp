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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

//Foreground Services -> Sticky services
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var serviceKilled = false
    private var isFirstRun = true

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    //as we have to update our notification
    private lateinit var curNotificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    private var timeRunInSeconds = MutableLiveData<Long>()
    private var isTimerEnabled = false

    //start and pause time
    private var lapTime = 0L

    //total time
    private var timeRun = 0L

    //saving time started
    private var timeStarted = 0L
    private var secondLastTimeStamp = 0L

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
        //post initial values
        postInitialValues()
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
                        startForeGroundService()
                    } else {
                        Timber.d("Resumed service")
                        startTimer()
                    }

                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                    Timber.d("Stopped Service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }


    //giving initial values
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
        curNotificationBuilder = baseNotificationBuilder
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    //updating location according to isTracking
    private fun observeIsTracking() {
        isTracking.observe(this) {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        }
    }

    //starts timer, isTracking = true, create and start notification
    private fun startForeGroundService() {
        startTimer()
        //whenever service is starting giving value to isTracking
        isTracking.postValue(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        //starting notification
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
        timeRunInSeconds.observe(this){
            if(!serviceKilled){
                val notification = curNotificationBuilder.setContentText(Utils.getFormattedStopWatchTime(it*1000L))
                notificationManager.notify(NOTIFICATION_ID,notification.build())
            }
        }
    }

    //updating lapTime, timeRun, timeRunInMillis
    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                //time difference between now and time started
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                //post new lapTime
                if (timeRunInMillis.value!! >= secondLastTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    secondLastTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

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

    private fun addPathPoints(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Utils.NOTIFICATION_CHANNEL_ID,
            Utils.NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 0, pauseIntent, FLAG_MUTABLE)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_MUTABLE)
        }
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled){
            curNotificationBuilder = baseNotificationBuilder.addAction(
                R.drawable.icons8_pause,
                notificationActionText,
                pendingIntent
            )
            notificationManager.notify(NOTIFICATION_ID,curNotificationBuilder.build())
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    fun getServiceKilledOrNot(): Boolean {
        return serviceKilled
    }

}