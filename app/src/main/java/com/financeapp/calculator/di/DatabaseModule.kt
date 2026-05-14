package com.financeapp.calculator.di

import android.content.Context
import androidx.room.Room
import com.financeapp.calculator.data.local.AppDatabase
import com.financeapp.calculator.data.local.dao.CalculationDao
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
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "finance_calculator.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCalculationDao(db: AppDatabase): CalculationDao = db.calculationDao()
}
