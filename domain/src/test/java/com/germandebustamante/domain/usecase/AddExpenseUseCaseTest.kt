package com.germandebustamante.domain.usecase

import com.germandebustamante.model.Category
import com.germandebustamante.model.Expense
import com.germandebustamante.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for AddExpenseUseCase.
 * 
 * Tests cover:
 * - Successful expense creation
 * - Business rule validation
 * - Edge cases and error handling
 * - Repository interaction verification
 */
class AddExpenseUseCaseTest {
    
    private lateinit var mockRepository: ExpenseRepository
    private lateinit var addExpenseUseCase: AddExpenseUseCase
    
    // Test data
    private val validAmount = BigDecimal("25.50")
    private val validDate = LocalDate(2024, 9, 26)
    private val validCategory = Category.FOOD
    private val validNote = "Test expense"
    private val expectedExpenseId = 123L
    
    @Before
    fun setup() {
        mockRepository = mockk()
        addExpenseUseCase = AddExpenseUseCase(mockRepository)
    }
    
    @Test
    fun `execute creates expense successfully with valid data`() = runTest {
        // Given
        coEvery { mockRepository.insertExpense(any()) } returns expectedExpenseId
        
        // When
        val result = addExpenseUseCase.execute(
            amount = validAmount,
            date = validDate,
            category = validCategory,
            note = validNote
        )
        
        // Then
        assertTrue("Result should be successful", result.isSuccess)
        assertEquals(expectedExpenseId, result.getOrNull())
        
        // Verify repository was called with correct expense
        coVerify {
            mockRepository.insertExpense(match { expense ->
                expense.amount == validAmount &&
                expense.date == validDate &&
                expense.category == validCategory &&
                expense.note == validNote &&
                expense.isNew()
            })
        }
    }
    
    @Test
    fun `execute handles null note correctly`() = runTest {
        // Given
        coEvery { mockRepository.insertExpense(any()) } returns expectedExpenseId
        
        // When
        val result = addExpenseUseCase.execute(
            amount = validAmount,
            date = validDate,
            category = validCategory,
            note = null
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify {
            mockRepository.insertExpense(match { expense ->
                expense.note == null
            })
        }
    }
    
    @Test
    fun `execute trims and validates note`() = runTest {
        // Given
        coEvery { mockRepository.insertExpense(any()) } returns expectedExpenseId
        val noteWithWhitespace = "  Test expense  "
        
        // When
        val result = addExpenseUseCase.execute(
            amount = validAmount,
            date = validDate,
            category = validCategory,
            note = noteWithWhitespace
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify {
            mockRepository.insertExpense(match { expense ->
                expense.note == "Test expense" // Trimmed
            })
        }
    }
    
    @Test
    fun `execute converts blank note to null`() = runTest {
        // Given
        coEvery { mockRepository.insertExpense(any()) } returns expectedExpenseId
        
        // When
        val result = addExpenseUseCase.execute(
            amount = validAmount,
            date = validDate,
            category = validCategory,
            note = "   " // Only whitespace
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify {
            mockRepository.insertExpense(match { expense ->
                expense.note == null // Blank note converted to null
            })
        }
    }
    
    @Test
    fun `execute fails with negative amount`() = runTest {
        // Given
        val negativeAmount = BigDecimal("-10.00")
        
        // When
        val result = addExpenseUseCase.execute(
            amount = negativeAmount,
            date = validDate,
            category = validCategory,
            note = validNote
        )
        
        // Then
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Should be IllegalArgumentException", exception is IllegalArgumentException)
        assertTrue("Error message should mention positive", 
            exception?.message?.contains("positive", ignoreCase = true) ?: false)
        
        // Verify repository was not called
        coVerify(exactly = 0) { mockRepository.insertExpense(any()) }
    }
    
    @Test
    fun `execute fails with zero amount`() = runTest {
        // Given
        val zeroAmount = BigDecimal.ZERO
        
        // When
        val result = addExpenseUseCase.execute(
            amount = zeroAmount,
            date = validDate,
            category = validCategory
        )
        
        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { mockRepository.insertExpense(any()) }
    }
    
    @Test
    fun `execute fails with too many decimal places`() = runTest {
        // Given
        val invalidAmount = BigDecimal("10.123") // 3 decimal places
        
        // When
        val result = addExpenseUseCase.execute(
            amount = invalidAmount,
            date = validDate,
            category = validCategory
        )
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertTrue("Error should mention decimal places",
            exception?.message?.contains("decimal", ignoreCase = true) ?: false)
    }
    
    @Test
    fun `execute fails with amount exceeding maximum`() = runTest {
        // Given
        val excessiveAmount = AddExpenseUseCase.MAX_EXPENSE_AMOUNT.add(BigDecimal.ONE)
        
        // When
        val result = addExpenseUseCase.execute(
            amount = excessiveAmount,
            date = validDate,
            category = validCategory
        )
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertTrue("Error should mention maximum",
            exception?.message?.contains("maximum", ignoreCase = true) ?: false)
    }
    
    @Test
    fun `execute fails with future date beyond allowed limit`() = runTest {
        // Given
        val farFutureDate = LocalDate(2025, 12, 31) // Way in the future
        
        // When
        val result = addExpenseUseCase.execute(
            amount = validAmount,
            date = farFutureDate,
            category = validCategory
        )
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertTrue("Error should mention future date",
            exception?.message?.contains("future", ignoreCase = true) ?: false)
    }
    
    @Test
    fun `execute fails with note containing forbidden characters`() = runTest {
        // Given
        val noteWithForbiddenChars = "Test expense with <forbidden> characters {bad} [also bad]"
        
        // When
        val result = addExpenseUseCase.execute(
            amount = validAmount,
            date = validDate,
            category = validCategory,
            note = noteWithForbiddenChars
        )
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertTrue("Error should mention special characters",
            exception?.message?.contains("special", ignoreCase = true) ?: false)
    }
    
    @Test
    fun `execute fails with note exceeding maximum length`() = runTest {
        // Given
        val longNote = "a".repeat(Expense.MAX_NOTE_LENGTH + 1)
        
        // When
        val result = addExpenseUseCase.execute(
            amount = validAmount,
            date = validDate,
            category = validCategory,
            note = longNote
        )
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
    }
    
    @Test
    fun `execute handles repository failure gracefully`() = runTest {
        // Given
        val repositoryException = RuntimeException("Database error")
        coEvery { mockRepository.insertExpense(any()) } throws repositoryException
        
        // When
        val result = addExpenseUseCase.execute(
            amount = validAmount,
            date = validDate,
            category = validCategory,
            note = validNote
        )
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(repositoryException, result.exceptionOrNull())
    }
    
    @Test
    fun `executeWithSmartCategory uses default category when none provided`() = runTest {
        // Given
        coEvery { mockRepository.insertExpense(any()) } returns expectedExpenseId
        
        // When
        val result = addExpenseUseCase.executeWithSmartCategory(
            amount = validAmount,
            date = validDate,
            category = null,
            note = validNote
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify {
            mockRepository.insertExpense(match { expense ->
                expense.category == Category.getDefault() // Should use default category
            })
        }
    }
    
    @Test
    fun `executeWithSmartCategory uses provided category when given`() = runTest {
        // Given
        coEvery { mockRepository.insertExpense(any()) } returns expectedExpenseId
        
        // When
        val result = addExpenseUseCase.executeWithSmartCategory(
            amount = validAmount,
            date = validDate,
            category = Category.TRANSPORT,
            note = validNote
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify {
            mockRepository.insertExpense(match { expense ->
                expense.category == Category.TRANSPORT
            })
        }
    }
    
    @Test
    fun `execute accepts maximum valid amount`() = runTest {
        // Given
        coEvery { mockRepository.insertExpense(any()) } returns expectedExpenseId
        val maxAmount = AddExpenseUseCase.MAX_EXPENSE_AMOUNT
        
        // When
        val result = addExpenseUseCase.execute(
            amount = maxAmount,
            date = validDate,
            category = validCategory
        )
        
        // Then
        assertTrue("Should accept maximum valid amount", result.isSuccess)
        
        coVerify {
            mockRepository.insertExpense(match { expense ->
                expense.amount == maxAmount
            })
        }
    }
    
    @Test
    fun `execute accepts today's date`() = runTest {
        // Given
        coEvery { mockRepository.insertExpense(any()) } returns expectedExpenseId
        val today = LocalDate.fromEpochDays(
            (System.currentTimeMillis() / (24 * 60 * 60 * 1000)).toInt()
        )
        
        // When
        val result = addExpenseUseCase.execute(
            amount = validAmount,
            date = today,
            category = validCategory
        )
        
        // Then
        assertTrue("Should accept today's date", result.isSuccess)
    }
}
