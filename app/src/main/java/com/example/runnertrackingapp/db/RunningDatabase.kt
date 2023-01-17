package com.example.runnertrackingapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.runnertrackingapp.db.models.Run

@Database(entities = [Run::class], version = 1)
@TypeConverters(Converters::class)
abstract class RunningDatabase: RoomDatabase(){
    abstract fun getRunDao():RunDao
}