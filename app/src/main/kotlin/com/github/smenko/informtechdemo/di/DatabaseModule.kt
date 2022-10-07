package com.github.smenko.informtechdemo.di

import android.content.Context
import androidx.room.Room
import com.github.smenko.informtechdemo.datasource.AppRoomDatabase
import com.github.smenko.informtechdemo.datasource.ContactDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun providesContactDao(@RoomQualifier.SQL appRoomDatabase: AppRoomDatabase): ContactDao {
        return appRoomDatabase.contactDao()
    }

    @Provides
    @Singleton
    @RoomQualifier.SQL
    fun provideAppRoomDatabase(@ApplicationContext appContext: Context): AppRoomDatabase {
        return Room
            .databaseBuilder(
                appContext,
                AppRoomDatabase::class.java, "sample_app_database"
            ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }

    @Provides
    @Singleton
    @RoomQualifier.InMemory
    fun provideAppRoomInMemoryDatabase(@ApplicationContext appContext: Context): AppRoomDatabase {
        return Room.inMemoryDatabaseBuilder(
            appContext, AppRoomDatabase::class.java
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }
}