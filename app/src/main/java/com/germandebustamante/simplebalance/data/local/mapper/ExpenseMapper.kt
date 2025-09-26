package com.germandebustamante.simplebalance.data.local.mapper

import com.germandebustamante.simplebalance.data.local.entity.ExpenseEntity
import com.germandebustamante.simplebalance.domain.model.Category
import com.germandebustamante.simplebalance.domain.model.Expense
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

/**
 * Mapper functions to convert between [ExpenseEntity] and domain [Expense] models.
 * 
 * These functions handle the conversion between:
 * - Long (cents) ↔ BigDecimal (euros) for precise money calculations
 * - Long (epochDay) ↔ LocalDate for efficient date storage and type safety
 * - String (categoryCode) ↔ Category enum for stable category references
 * - Long (epochMillis) ↔ Instant for timestamp handling
 */

/**
 * Convert [ExpenseEntity] to domain [Expense] model.
 * 
 * @receiver ExpenseEntity from database
 * @return Domain expense model with proper types and validation
 */
fun ExpenseEntity.toDomainModel(): Expense {
    return Expense(
        id = id,
        amount = BigDecimal(amountCents).divide(BigDecimal(100)), // Convert cents to euros
        date = LocalDate.fromEpochDays(epochDay.toInt()),
        category = Category.fromCode(categoryCode),
        note = note,
        createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
        updatedAt = Instant.fromEpochMilliseconds(updatedAtEpochMillis)
    )
}

/**
 * Convert domain [Expense] to [ExpenseEntity] for database storage.
 * 
 * @receiver Domain expense model
 * @return Room entity optimized for database operations
 */
fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        amountCents = amountInCents(), // Use domain method for cents conversion
        epochDay = date.toEpochDays().toLong(),
        categoryCode = category.code,
        note = note,
        createdAtEpochMillis = createdAt.toEpochMilliseconds(),
        updatedAtEpochMillis = updatedAt.toEpochMilliseconds()
    )
}

/**
 * Convert list of [ExpenseEntity] to domain models.
 * 
 * @receiver List of entities from database
 * @return List of domain expense models
 */
fun List<ExpenseEntity>.toDomainModels(): List<Expense> {
    return map { it.toDomainModel() }
}

/**
 * Convert list of domain [Expense] to entities.
 * 
 * @receiver List of domain expense models
 * @return List of entities for database operations
 */
fun List<Expense>.toEntities(): List<ExpenseEntity> {
    return map { it.toEntity() }
}