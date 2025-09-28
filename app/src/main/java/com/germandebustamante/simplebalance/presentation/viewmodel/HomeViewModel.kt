package com.germandebustamante.simplebalance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.domain.usecase.GetMonthlyExpensesUseCase
import com.germandebustamante.domain.usecase.GetMonthlyTotalUseCase
import com.germandebustamante.domain.usecase.DeleteExpenseUseCase
import com.germandebustamante.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import java.math.BigDecimal

sealed interface HomeEvent {
    data class DeleteExpense(val expense: Expense) : HomeEvent
    data object UndoDelete : HomeEvent
    data object OnDeleteSnackBarDismiss : HomeEvent
}

class HomeViewModel(
    private val getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase,
    private val getMonthlyTotalUseCase: GetMonthlyTotalUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    val currentMonth: StateFlow<LocalDate> = _currentMonth.asStateFlow()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var recentlyDeletedExpense: Expense? = null

    init {
        viewModelScope.launch {
            _currentMonth.flatMapLatest { month ->
                combine(
                    getMonthlyExpensesUseCase.executeWithStats(month.year, month.monthNumber),
                    getMonthlyTotalUseCase.executeWithBreakdown(month.year, month.monthNumber)
                ) { expensesStats, totalData ->
                    _uiState.value.copy(
                        expenses = expensesStats.expenses,
                        monthlyTotal = totalData.total,
                        hasExpenses = expensesStats.hasExpenses,
                        isLoading = false,
                        error = null
                    )
                }
            }.catch { error ->
                _uiState.value.copy(error = error.message)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.DeleteExpense -> deleteExpense(event.expense)
            is HomeEvent.UndoDelete -> undoDelete()
            is HomeEvent.OnDeleteSnackBarDismiss -> onDeleteSnackBarDismiss()
        }
    }

    private fun deleteExpense(expense: Expense) {
        recentlyDeletedExpense = expense
        _uiState.update {
            it.copy(
                expenses = it.expenses.filter { e -> e.id != expense.id },
                monthlyTotal = it.monthlyTotal - expense.amount
            )
        }
    }

    private fun undoDelete() {
        recentlyDeletedExpense?.let { expense ->
            _uiState.update {
                it.copy(
                    expenses = it.expenses + expense,
                    monthlyTotal = it.monthlyTotal + expense.amount
                )
            }
        }
        recentlyDeletedExpense = null
    }

    private fun onDeleteSnackBarDismiss() {
        viewModelScope.launch {
            recentlyDeletedExpense?.let { deleteExpenseUseCase(it) }
            recentlyDeletedExpense = null
        }
    }

    fun goToPreviousMonth() {
        viewModelScope.launch {
            _currentMonth.update { it.minus(DatePeriod(months = 1)) }
        }
    }

    fun goToNextMonth() {
        viewModelScope.launch {
            _currentMonth.update { it.plus(DatePeriod(months = 1)) }
        }
    }
}

data class HomeUiState(
    val expenses: List<Expense> = emptyList(),
    val monthlyTotal: BigDecimal = BigDecimal.ZERO,
    val hasExpenses: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val pendingDeletedExpenseId: Long? = null,
)