package com.example.runnertrackingapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runnertrackingapp.db.local.RunDao
import com.example.runnertrackingapp.db.local.RunningDatabase
import com.example.runnertrackingapp.utils.Utils
import com.example.runnertrackingapp.utils.Utils.FIRST_TIME_TOGGLE_KEY
import com.example.runnertrackingapp.utils.Utils.RUNNING_DATABASE_NAME
import com.example.runnertrackingapp.utils.Utils.USERNAME_KEY
import com.example.runnertrackingapp.utils.Utils.WEIGHT_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext app: Context): RunningDatabase = Room.databaseBuilder(app,
        RunningDatabase::class.java,RUNNING_DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideRunDao(db: RunningDatabase): RunDao = db.getRunDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context:Context) = context.getSharedPreferences(Utils.SHARED_PREFERENCES,Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideName(sharedPrefs:SharedPreferences):String = sharedPrefs.getString(USERNAME_KEY,"") ?: ""

    @Provides
    @Singleton
    fun provideWeight(sharedPrefs:SharedPreferences):Float = sharedPrefs.getFloat(WEIGHT_KEY,80f)

    @Provides
    @Singleton
    fun provideFirstTimeToggle(sharedPrefs:SharedPreferences):Boolean = sharedPrefs.getBoolean(FIRST_TIME_TOGGLE_KEY,true)
}