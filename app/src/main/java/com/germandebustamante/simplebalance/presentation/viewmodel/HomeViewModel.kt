package com.germandebustamante.simplebalance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.domain.usecase.GetMonthlyExpensesUseCase
import com.germandebustamante.domain.usecase.GetMonthlyTotalUseCase
import com.germandebustamante.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
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

class HomeViewModel(
    private val getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase,
    private val getMonthlyTotalUseCase: GetMonthlyTotalUseCase,
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    val currentMonth: StateFlow<LocalDate> = _currentMonth.asStateFlow()

    val uiState: StateFlow<HomeUiState> = _currentMonth
        .flatMapLatest { month ->
            combine(
                getMonthlyExpensesUseCase.executeWithStats(month.year, month.monthNumber),
                getMonthlyTotalUseCase.executeWithBreakdown(month.year, month.monthNumber)
            ) { expensesStats, totalData ->
                HomeUiState(
                    expenses = expensesStats.expenses,
                    monthlyTotal = totalData.total,
                    hasExpenses = expensesStats.hasExpenses,
                    isLoading = false,
                    error = null
                )
            }
        }.catch { error -> emit(HomeUiState(error = error.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

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
)
