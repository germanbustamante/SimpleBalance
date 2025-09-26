package com.germandebustamante.simplebalance.data.local.mapper

import com.germandebustamante.simplebalance.data.local.entity.ExpenseEntity
import com.germandebustamante.simplebalance.domain.model.Category
import com.germandebustamante.simplebalance.domain.model.Expense
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for ExpenseMapper functions.
 * 
 * Tests cover:
 * - Entity to domain model conversion
 * - Domain model to entity conversion
 * - List conversions
 * - Edge cases and boundary conditions
 * - Data precision and integrity
 */
class ExpenseMapperTest {
    
    // Test data constants
    private val testId = 123L
    private val testAmountEuros = BigDecimal("12.34")
    private val testAmountCents = 1234L
    private val testDate = LocalDate(2024, 9, 26)
    private val testEpochDay = testDate.toEpochDays().toLong()
    private val testCategory = Category.FOOD
    private val testNote = "Test expense note"
    private val testCreatedAt = Instant.fromEpochMilliseconds(1695734400000L) // 2024-09-26T12:00:00Z
    private val testUpdatedAt = Instant.fromEpochMilliseconds(1695738000000L) // 2024-09-26T13:00:00Z
    
    @Test
    fun `toDomainModel converts entity to domain model correctly`() {
        // Given
        val entity = ExpenseEntity(
            id = testId,
            amountCents = testAmountCents,
            epochDay = testEpochDay,
            categoryCode = testCategory.code,
            note = testNote,
            createdAtEpochMillis = testCreatedAt.toEpochMilliseconds(),
            updatedAtEpochMillis = testUpdatedAt.toEpochMilliseconds()
        )
        
        // When
        val domainModel = entity.toDomainModel()
        
        // Then
        assertEquals(testId, domainModel.id)
        assertEquals(testAmountEuros, domainModel.amount)
        assertEquals(testDate, domainModel.date)
        assertEquals(testCategory, domainModel.category)
        assertEquals(testNote, domainModel.note)
        assertEquals(testCreatedAt, domainModel.createdAt)
        assertEquals(testUpdatedAt, domainModel.updatedAt)
    }
    
    @Test
    fun `toDomainModel handles null note correctly`() {
        // Given
        val entity = ExpenseEntity(
            id = testId,
            amountCents = testAmountCents,
            epochDay = testEpochDay,
            categoryCode = testCategory.code,
            note = null,
            createdAtEpochMillis = testCreatedAt.toEpochMilliseconds(),
            updatedAtEpochMillis = testUpdatedAt.toEpochMilliseconds()
        )
        
        // When
        val domainModel = entity.toDomainModel()
        
        // Then
        assertNull(domainModel.note)
    }
    
    @Test
    fun `toDomainModel handles unknown category code`() {
        // Given
        val entity = ExpenseEntity(
            id = testId,
            amountCents = testAmountCents,
            epochDay = testEpochDay,
            categoryCode = "unknown_category",
            note = testNote,
            createdAtEpochMillis = testCreatedAt.toEpochMilliseconds(),
            updatedAtEpochMillis = testUpdatedAt.toEpochMilliseconds()
        )
        
        // When
        val domainModel = entity.toDomainModel()
        
        // Then
        assertEquals(Category.OTHER, domainModel.category) // Should fallback to OTHER
    }
    
    @Test
    fun `toEntity converts domain model to entity correctly`() {
        // Given
        val domainModel = Expense(
            id = testId,
            amount = testAmountEuros,
            date = testDate,
            category = testCategory,
            note = testNote,
            createdAt = testCreatedAt,
            updatedAt = testUpdatedAt
        )
        
        // When
        val entity = domainModel.toEntity()
        
        // Then
        assertEquals(testId, entity.id)
        assertEquals(testAmountCents, entity.amountCents)
        assertEquals(testEpochDay, entity.epochDay)
        assertEquals(testCategory.code, entity.categoryCode)
        assertEquals(testNote, entity.note)
        assertEquals(testCreatedAt.toEpochMilliseconds(), entity.createdAtEpochMillis)
        assertEquals(testUpdatedAt.toEpochMilliseconds(), entity.updatedAtEpochMillis)
    }
    
    @Test
    fun `toEntity handles null note correctly`() {
        // Given
        val domainModel = Expense(
            id = testId,
            amount = testAmountEuros,
            date = testDate,
            category = testCategory,
            note = null,
            createdAt = testCreatedAt,
            updatedAt = testUpdatedAt
        )
        
        // When
        val entity = domainModel.toEntity()
        
        // Then
        assertNull(entity.note)
    }
    
    @Test
    fun `cents conversion maintains precision`() {
        // Test various amounts to ensure precision
        val testCases = listOf(
            BigDecimal("0.01") to 1L,
            BigDecimal("0.99") to 99L,
            BigDecimal("1.00") to 100L,
            BigDecimal("12.34") to 1234L,
            BigDecimal("999.99") to 99999L
        )
        
        testCases.forEach { (euros, expectedCents) ->
            // Given
            val domainModel = Expense(
                id = testId,
                amount = euros,
                date = testDate,
                category = testCategory,
                note = null,
                createdAt = testCreatedAt,
                updatedAt = testUpdatedAt
            )
            
            // When
            val entity = domainModel.toEntity()
            val backToDomain = entity.toDomainModel()
            
            // Then
            assertEquals("Amount conversion failed for $euros", expectedCents, entity.amountCents)
            assertEquals("Round-trip conversion failed for $euros", 0, euros.compareTo(backToDomain.amount))
        }
    }
    
    @Test
    fun `toDomainModels converts list of entities correctly`() {
        // Given
        val entities = listOf(
            ExpenseEntity(
                id = 1L,
                amountCents = 1000L,
                epochDay = testEpochDay,
                categoryCode = Category.FOOD.code,
                note = "Food expense",
                createdAtEpochMillis = testCreatedAt.toEpochMilliseconds(),
                updatedAtEpochMillis = testUpdatedAt.toEpochMilliseconds()
            ),
            ExpenseEntity(
                id = 2L,
                amountCents = 2000L,
                epochDay = testEpochDay + 1,
                categoryCode = Category.TRANSPORT.code,
                note = null,
                createdAtEpochMillis = testCreatedAt.toEpochMilliseconds(),
                updatedAtEpochMillis = testUpdatedAt.toEpochMilliseconds()
            )
        )
        
        // When
        val domainModels = entities.toDomainModels()
        
        // Then
        assertEquals(2, domainModels.size)
        assertEquals(1L, domainModels[0].id)
        assertEquals(0, BigDecimal("10.00").compareTo(domainModels[0].amount))
        assertEquals(Category.FOOD, domainModels[0].category)
        assertEquals("Food expense", domainModels[0].note)
        
        assertEquals(2L, domainModels[1].id)
        assertEquals(0, BigDecimal("20.00").compareTo(domainModels[1].amount))
        assertEquals(Category.TRANSPORT, domainModels[1].category)
        assertNull(domainModels[1].note)
    }
    
    @Test
    fun `toEntities converts list of domain models correctly`() {
        // Given
        val domainModels = listOf(
            Expense(
                id = 1L,
                amount = BigDecimal("10.50"),
                date = testDate,
                category = Category.SHOPPING,
                note = "Shopping expense",
                createdAt = testCreatedAt,
                updatedAt = testUpdatedAt
            ),
            Expense(
                id = 2L,
                amount = BigDecimal("25.75"),
                date = LocalDate(testDate.year, testDate.month, testDate.dayOfMonth + 1),
                category = Category.HEALTH,
                note = null,
                createdAt = testCreatedAt,
                updatedAt = testUpdatedAt
            )
        )
        
        // When
        val entities = domainModels.toEntities()
        
        // Then
        assertEquals(2, entities.size)
        assertEquals(1L, entities[0].id)
        assertEquals(1050L, entities[0].amountCents)
        assertEquals(Category.SHOPPING.code, entities[0].categoryCode)
        assertEquals("Shopping expense", entities[0].note)
        
        assertEquals(2L, entities[1].id)
        assertEquals(2575L, entities[1].amountCents)
        assertEquals(Category.HEALTH.code, entities[1].categoryCode)
        assertNull(entities[1].note)
    }
    
    @Test
    fun `empty lists are handled correctly`() {
        // Given
        val emptyEntityList = emptyList<ExpenseEntity>()
        val emptyDomainList = emptyList<Expense>()
        
        // When
        val domainFromEmpty = emptyEntityList.toDomainModels()
        val entitiesFromEmpty = emptyDomainList.toEntities()
        
        // Then
        assertTrue(domainFromEmpty.isEmpty())
        assertTrue(entitiesFromEmpty.isEmpty())
    }
    
    @Test
    fun `date conversion maintains accuracy`() {
        // Test date edge cases
        val testDates = listOf(
            LocalDate(2024, 1, 1),   // New Year
            LocalDate(2024, 2, 29),  // Leap year
            LocalDate(2024, 12, 31), // Year end
            LocalDate(1970, 1, 1),   // Unix epoch start
            LocalDate(2038, 1, 19)   // Unix time boundary
        )
        
        testDates.forEach { date ->
            // Given
            val domainModel = Expense(
                id = testId,
                amount = testAmountEuros,
                date = date,
                category = testCategory,
                note = null,
                createdAt = testCreatedAt,
                updatedAt = testUpdatedAt
            )
            
            // When
            val entity = domainModel.toEntity()
            val backToDomain = entity.toDomainModel()
            
            // Then
            assertEquals("Date conversion failed for $date", date, backToDomain.date)
        }
    }
    
    @Test
    fun `round trip conversion maintains data integrity`() {
        // Given - complex expense with all fields
        val originalExpense = Expense(
            id = testId,
            amount = BigDecimal("123.45"),
            date = LocalDate(2024, 9, 26),
            category = Category.LEISURE,
            note = "Complex test expense with special chars: €@#$%",
            createdAt = testCreatedAt,
            updatedAt = testUpdatedAt
        )
        
        // When - convert to entity and back
        val entity = originalExpense.toEntity()
        val convertedExpense = entity.toDomainModel()
        
        // Then - all data should be identical
        assertEquals(originalExpense.id, convertedExpense.id)
        assertEquals(originalExpense.amount, convertedExpense.amount)
        assertEquals(originalExpense.date, convertedExpense.date)
        assertEquals(originalExpense.category, convertedExpense.category)
        assertEquals(originalExpense.note, convertedExpense.note)
        assertEquals(originalExpense.createdAt, convertedExpense.createdAt)
        assertEquals(originalExpense.updatedAt, convertedExpense.updatedAt)
        
        // Test that equals would work
        assertEquals(originalExpense, convertedExpense)
    }
}