package com.example.pocketsafe.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pocketsafe.data.dao.BudgetGoalDao
import com.example.pocketsafe.data.dao.CategoryDao
import com.example.pocketsafe.data.dao.ExpenseDao

@Database(
    entities = [
        Expense::class,
        Category::class,
        BudgetGoal::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetGoalDao(): BudgetGoalDao
} 