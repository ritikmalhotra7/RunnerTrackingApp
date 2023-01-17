package com.example.runnertrackingapp.di

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.runnertrackingapp.R
import com.example.runnertrackingapp.db.RunDao
import com.example.runnertrackingapp.db.RunningDatabase
import com.example.runnertrackingapp.ui.activities.MainActivity
import com.example.runnertrackingapp.utils.Utils
import com.example.runnertrackingapp.utils.Utils.RUNNING_DATABASE_NAME
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext app: Context):RunningDatabase = Room.databaseBuilder(app,RunningDatabase::class.java,RUNNING_DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideRunDao(db:RunningDatabase):RunDao = db.getRunDao()

}