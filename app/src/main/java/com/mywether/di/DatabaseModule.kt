package com.mywether.di

import android.content.Context
import androidx.room.Room
import com.mywether.data.database.DataBase
import com.mywether.data.database.WeatherDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): DataBase {
        return Room.databaseBuilder(
            appContext,
            DataBase::class.java,
            "weatherDB"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideChannelDao(db: DataBase): WeatherDAO {
        return db.weatherDao
    }
}