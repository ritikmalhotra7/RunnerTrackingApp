package com.example.runnertrackingapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.ui.activities.MainActivity
import com.example.runnertrackingapp.utils.Utils
import com.example.runnertrackingapp.utils.Utils.ACTION_PAUSE_SERVICE
import com.example.runnertrackingapp.utils.Utils.ACTION_START_OR_RESUME_SERVICE
import com.example.runnertrackingapp.utils.Utils.ACTION_STOP_SERVICE
import com.example.runnertrackingapp.utils.Utils.NOTIFICATION_ID
import timber.log.Timber

class TrackingService : LifecycleService() {
    private var isFirstRun = true
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Log.d("taget", "Started or Resume service")
                    Timber.d("Started or Resume service")
                    if (isFirstRun) {
                        startForeGroundService()
                        isFirstRun = false
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Log.d("taget", "Paused service")
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Log.d("taget", "Stopped Service")
                    Timber.d("Stopped Service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeGroundService() {
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
                setContentIntent(getMainActivityPendingIntent())
            }
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() :PendingIntent{
        return if(Build.VERSION.SDK_INT>Build.VERSION_CODES.Q){
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).also {
                    it.action = Utils.ACTION_SHOW_TRACKING_FRAGMENT
                },
                FLAG_MUTABLE
            )
        }else{
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).also {
                    it.action = Utils.ACTION_SHOW_TRACKING_FRAGMENT
                },
                FLAG_UPDATE_CURRENT
            )
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
}