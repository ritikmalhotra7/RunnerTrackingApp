package com.example.runnertrackingapp.utils

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Build
import com.example.runnertrackingapp.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object Utils {
    const val RUNNING_DATABASE_NAME = "running_db"
    const val SHARED_PREFERENCES = "SHARED_PREFERENCES"
    const val USERNAME_KEY = "USERNAME_KEY"
    const val WEIGHT_KEY = "WEIGHT_KEY"

    const val REQUEST_CODE_LOCATION_PERMISSION = 100

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val LOCATION_UPDATE_INTERVAL = 3000L
    const val FASTEST_LOCATION_INTERVAL = 1000L
    const val TIMER_UPDATE_INTERVAL = 50L
    const val FIRST_TIME_TOGGLE_KEY = "FIRST_TIME_TOGGLE_KEY"

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 18f

    const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID"
    const val NOTIFICATION_CHANNEL_NAME = "TRACKING"
    const val NOTIFICATION_ID = 1

    fun hasLocationPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    fun calculatePolylineDistance(polyLine:Polyline):Float{
        var distance = 0f
        for(i in 0..polyLine.size-2){
            val pos1 = polyLine[i]
            val pos2 = polyLine[i+1]
            val result = FloatArray(1)
            Location.distanceBetween(pos1.latitude,pos1.longitude,pos2.latitude,pos2.longitude,result)
            distance += result[0]
        }
        return distance
    }

    //format millis into stop watch format
    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var time = ms
        var hours = TimeUnit.MILLISECONDS.toHours(time).also { time -= TimeUnit.HOURS.toMillis(it) }
        var minutes: Long = TimeUnit.MILLISECONDS.toMinutes(time).also { time -= TimeUnit.MINUTES.toMillis(it) }
        var second = TimeUnit.MILLISECONDS.toSeconds(time).also { time -= TimeUnit.SECONDS.toMillis(it) }
        if(!includeMillis){
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (second < 10) "0" else ""}$second"
        }else{
            time /= 10
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (second < 10) "0" else ""}$second:" +
                    "${if(time<10) "0" else ""}$time"
        }
    }
}