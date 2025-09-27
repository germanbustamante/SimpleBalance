package com.germandebustamante.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.germandebustamante.data.local.entity.ExpenseEntity

/**
 * Room database for expense storage.
 * 
 * Features:
 * - Version 1 schema with [ExpenseEntity]
 * - KSP compilation for faster builds (instead of KAPT)
 * - Optimized for offline-first usage
 * - Future-ready for migrations and additional entities
 * 
 * Database version history:
 * - Version 1: Initial schema with expenses table
 * 
 * Future versions will add:
 * - Custom categories table
 * - User authentication table  
 * - Sync metadata columns
 */
@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = false // Disable for Phase 1, enable in production
)
abstract class ExpenseDatabase : RoomDatabase() {
    
    /**
     * Get the expense DAO for database operations.
     */
    abstract fun expenseDao(): ExpenseDao
    
    companion object {
        const val DATABASE_NAME = "expenses.db"
        
        /**
         * Create database instance with proper configuration.
         * 
         * @param context Application context
         * @param databaseName Optional custom database name (for testing)
         * @return Configured Room database instance
         */
        fun create(
            context: Context,
            databaseName: String = DATABASE_NAME
        ): ExpenseDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ExpenseDatabase::class.java,
                databaseName
            )
                .fallbackToDestructiveMigration() // Safe for MVP, remove in production
                .build()
        }
        
        /**
         * Create in-memory database for testing.
         * 
         * @param context Test context
         * @return In-memory database that doesn't persist
         */
        fun createInMemory(context: Context): ExpenseDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                ExpenseDatabase::class.java
            )
                .allowMainThreadQueries() // OK for tests
                .build()
        }
    }
}

// DatabaseConverters will be added in future phases when complex type conversions are needed
// Examples: List<String>, LocalDateTime, custom enums, etc.
