package com.germandebustamante.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for expense storage.
 * 
 * Optimized for:
 * - Fast monthly queries using [epochDay] index
 * - Precise money calculations using [amountCents] (Long)
 * - Future extensibility with [categoryCode] for custom categories
 * - KSP compatibility (uses KSP instead of KAPT for better performance)
 * 
 * @param id Auto-generated primary key
 * @param amountCents Amount stored as cents (Long) to avoid floating-point precision issues
 * @param epochDay Date stored as epoch day (Long) for efficient date range queries
 * @param categoryCode Stable category identifier for future custom category support
 * @param note Optional expense note/description
 * @param createdAtEpochMillis Creation timestamp as epoch milliseconds
 * @param updatedAtEpochMillis Last update timestamp as epoch milliseconds
 */
@Entity(
    tableName = "expenses",
    indices = [
        Index(
            name = "idx_expenses_epoch_day", 
            value = ["epoch_day"]
        ),
        Index(
            name = "idx_expenses_category", 
            value = ["category_code"]
        ),
        Index(
            name = "idx_expenses_date_category",
            value = ["epoch_day", "category_code"]
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,
    
    @ColumnInfo(name = "epoch_day") 
    val epochDay: Long,
    
    @ColumnInfo(name = "category_code")
    val categoryCode: String,
    
    @ColumnInfo(name = "note")
    val note: String? = null,
    
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long
) {
    
    companion object {
        /**
         * Table name constant for queries and migrations.
         */
        const val TABLE_NAME = "expenses"
        
        /**
         * Column name constants for type-safe queries.
         */
        object Columns {
            const val ID = "id"
            const val AMOUNT_CENTS = "amount_cents"
            const val EPOCH_DAY = "epoch_day"
            const val CATEGORY_CODE = "category_code"
            const val NOTE = "note"
            const val CREATED_AT_EPOCH_MILLIS = "created_at_epoch_millis"
            const val UPDATED_AT_EPOCH_MILLIS = "updated_at_epoch_millis"
        }
        
        /**
         * Index names for database operations.
         */
        object Indexes {
            const val EPOCH_DAY = "idx_expenses_epoch_day"
            const val CATEGORY = "idx_expenses_category"
            const val DATE_CATEGORY = "idx_expenses_date_category"
        }
    }
}
