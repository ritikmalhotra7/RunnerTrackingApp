package com.example.runnertrackingapp.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.ui.activities.MainActivity
import com.example.runnertrackingapp.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule{

    @ServiceScoped
    @Provides
    fun provideFusedLocation(@ApplicationContext ctx: Context) = FusedLocationProviderClient(ctx)

    @Provides
    @ServiceScoped
    fun provideMainActivityPendingIntent(@ApplicationContext ctx: Context): PendingIntent =
        PendingIntent.getActivity(
            ctx,
            0,
            //task to do
            Intent(ctx, MainActivity::class.java).also {
                it.action = Utils.ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_MUTABLE
        )

    @Provides
    @ServiceScoped
    fun provideBaseNotificationBuilder(@ApplicationContext ctx: Context, pendingIntent: PendingIntent) =
        NotificationCompat.Builder(ctx, Utils.NOTIFICATION_CHANNEL_ID).apply {
            setAutoCancel(false)
            setOngoing(true)
            setSmallIcon(R.drawable.icons8_direction)
            setContentTitle("Running App")
            setContentText("00:00:00")
            //onclick of notification
            setContentIntent(pendingIntent)
        }
}