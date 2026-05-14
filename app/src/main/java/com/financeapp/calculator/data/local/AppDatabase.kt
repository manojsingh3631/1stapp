package com.financeapp.calculator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.financeapp.calculator.data.local.dao.CalculationDao
import com.financeapp.calculator.data.local.entity.CalculationEntity

@Database(
    entities = [CalculationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
}
