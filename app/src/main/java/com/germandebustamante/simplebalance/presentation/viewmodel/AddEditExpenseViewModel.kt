package com.germandebustamante.simplebalance.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.domain.usecase.AddExpenseUseCase
import com.germandebustamante.domain.usecase.GetLastUsedCategoryUseCase
import com.germandebustamante.domain.usecase.UpdateExpenseUseCase
import com.germandebustamante.domain.usecase.ValidateExpenseUseCase
import com.germandebustamante.domain.usecase.GetExpenseByIdUseCase
import com.germandebustamante.model.Category
import com.germandebustamante.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.math.BigDecimal

class AddEditExpenseViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val getLastUsedCategoryUseCase: GetLastUsedCategoryUseCase,
    private val validateExpenseUseCase: ValidateExpenseUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditExpenseUiState())
    val uiState: StateFlow<AddEditExpenseUiState> = _uiState.asStateFlow()

    private var expenseId: Long? = null

    init {
        expenseId = savedStateHandle.get<Long>("expenseId")
        if (expenseId != null && expenseId != 0L) {
            loadExpense(expenseId!!)
        } else {
            loadLastUsedCategory()
        }
    }

    private fun loadExpense(id: Long) {
        viewModelScope.launch {
            getExpenseByIdUseCase.execute(id).collect { expense ->
                if (expense != null) {
                    _uiState.value = _uiState.value.copy(
                        amount = expense.amount.toString(),
                        date = expense.date,
                        category = expense.category,
                        note = expense.note,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Expense not found", // Ticket: Localize string
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadLastUsedCategory() {
        viewModelScope.launch {
            getLastUsedCategoryUseCase.execute().collect {
                    category ->
                _uiState.value = _uiState.value.copy(
                    category = category ?: Category.OTHER,
                    isLoading = false
                )
            }
        }
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount, amountError = null)
    }

    fun onDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun onCategoryChange(category: Category) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun onNoteChange(note: String) {
        _uiState.value = _uiState.value.copy(note = note, noteError = null)
    }

    @Suppress("LongMethod", "TooGenericExceptionCaught")
    fun saveExpense() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, amountError = null, noteError = null)
            try {
                validateExpenseUseCase.execute(
                    amount = BigDecimal(_uiState.value.amount),
                    date = _uiState.value.date,
                    note = _uiState.value.note
                )

                val expense = Expense.create(
                    id = expenseId ?: 0L,
                    amount = BigDecimal(_uiState.value.amount),
                    date = _uiState.value.date,
                    category = _uiState.value.category,
                    note = _uiState.value.note?.trim()?.takeIf { it.isNotBlank() }
                )

                val result = if (expenseId != null && expenseId != 0L) {
                    updateExpenseUseCase.execute(expense)
                } else {
                    addExpenseUseCase.execute(expense.amount, expense.date, expense.category, expense.note)
                }

                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
                }
            } catch (e: IllegalArgumentException) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
                when {
                    e.message?.contains("amount", ignoreCase = true) == true -> {
                        _uiState.value = _uiState.value.copy(amountError = e.message)
                    }
                    e.message?.contains("note", ignoreCase = true) == true -> {
                        _uiState.value = _uiState.value.copy(noteError = e.message)
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}

data class AddEditExpenseUiState(
    val amount: String = "",
    val amountError: String? = null,
    val date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val category: Category = Category.OTHER,
    val note: String? = null,
    val noteError: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
