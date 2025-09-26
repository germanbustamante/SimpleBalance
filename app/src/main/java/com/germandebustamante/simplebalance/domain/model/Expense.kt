package com.germandebustamante.simplebalance.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

/**
 * Domain model representing an expense.
 * 
 * This model represents the business entity and contains validation logic.
 * Money amounts are handled as [BigDecimal] for precision in business logic,
 * while the data layer stores them as Long (cents) to avoid floating-point errors.
 * 
 * @param id Unique identifier, 0 for new expenses
 * @param amount Expense amount in euros (must be positive)
 * @param date Date when the expense occurred
 * @param category Expense category
 * @param note Optional note/description
 * @param createdAt Timestamp when expense was created
 * @param updatedAt Timestamp when expense was last modified
 */
data class Expense(
    val id: Long = 0,
    val amount: BigDecimal,
    val date: LocalDate,
    val category: Category,
    val note: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    
    init {
        require(amount > BigDecimal.ZERO) { 
            "Expense amount must be positive, got: $amount" 
        }
        require(amount.scale() <= 2) { 
            "Expense amount cannot have more than 2 decimal places, got: $amount" 
        }
        require(note == null || note.length <= MAX_NOTE_LENGTH) { 
            "Note cannot exceed $MAX_NOTE_LENGTH characters, got: ${note?.length}" 
        }
    }
    
    /**
     * Check if this is a new expense (not yet persisted).
     * @return true if this expense hasn't been saved to database yet
     */
    fun isNew(): Boolean = id == 0L
    
    /**
     * Convert amount to cents for database storage.
     * @return Amount in cents as Long to avoid floating-point precision issues
     */
    fun amountInCents(): Long {
        return (amount * BigDecimal(100)).toLong()
    }
    
    /**
     * Create a copy of this expense with updated timestamp.
     * @param newUpdatedAt New timestamp, defaults to current time
     * @return Updated expense copy
     */
    fun withUpdatedTimestamp(newUpdatedAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())): Expense {
        return copy(updatedAt = newUpdatedAt)
    }
    
    /**
     * Check if expense has a note.
     * @return true if note is not null or blank
     */
    fun hasNote(): Boolean = !note.isNullOrBlank()
    
    companion object {
        const val MAX_NOTE_LENGTH = 200
        
        /**
         * Create new expense with current timestamps.
         * @param amount Expense amount
         * @param date Expense date
         * @param category Expense category
         * @param note Optional note
         * @return New expense with current timestamps
         */
        fun create(
            amount: BigDecimal,
            date: LocalDate,
            category: Category,
            note: String? = null
        ): Expense {
            val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            return Expense(
                id = 0,
                amount = amount,
                date = date,
                category = category,
                note = note?.takeIf { it.isNotBlank() },
                createdAt = now,
                updatedAt = now
            )
        }
        
        /**
         * Create expense from cents amount.
         * @param amountInCents Amount in cents (avoids floating-point issues)
         * @param date Expense date
         * @param category Expense category  
         * @param note Optional note
         * @return New expense with amount converted from cents
         */
        fun fromCents(
            amountInCents: Long,
            date: LocalDate,
            category: Category,
            note: String? = null
        ): Expense {
            val amount = BigDecimal(amountInCents).divide(BigDecimal(100))
            return create(amount, date, category, note)
        }
    }
}