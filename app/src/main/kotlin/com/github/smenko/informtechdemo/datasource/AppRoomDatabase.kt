package com.github.smenko.informtechdemo.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.smenko.informtechdemo.models.ContactInfoRoom

@Database(
    entities = [ContactInfoRoom::class],
    version = 1,
    exportSchema = false
)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}