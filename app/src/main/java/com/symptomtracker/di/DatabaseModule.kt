package com.symptomtracker.di

import android.content.Context
import androidx.room.Room
import com.symptomtracker.data.db.AppDatabase
import com.symptomtracker.data.notification.AlarmScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideSymptomDao(db: AppDatabase) = db.symptomDao()

    @Provides
    fun provideMedicationDao(db: AppDatabase) = db.medicationDao()

    @Provides
    fun provideLogEntryDao(db: AppDatabase) = db.logEntryDao()

    @Provides
    fun provideSideEffectDao(db: AppDatabase) = db.sideEffectDao()

    @Provides
    fun provideMedicationReminderDao(db: AppDatabase) = db.medicationReminderDao()

    @Provides
    @Singleton
    fun provideAlarmScheduler(@ApplicationContext context: Context): AlarmScheduler =
        AlarmScheduler(context)
}
