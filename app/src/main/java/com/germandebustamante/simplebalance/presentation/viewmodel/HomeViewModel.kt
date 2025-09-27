package com.germandebustamante.simplebalance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.germandebustamante.domain.usecase.GetMonthlyExpensesUseCase
import com.germandebustamante.domain.usecase.GetMonthlyTotalUseCase
import com.germandebustamante.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.math.BigDecimal

class HomeViewModel(
    private val getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase,
    private val getMonthlyTotalUseCase: GetMonthlyTotalUseCase
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    val currentMonth: StateFlow<LocalDate> = _currentMonth.asStateFlow()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMonthlyData()
    }

    fun loadMonthlyData() {
        viewModelScope.launch {
            _currentMonth.collect { month ->
                combine(
                    getMonthlyExpensesUseCase.executeWithStats(month.year, month.monthNumber),
                    getMonthlyTotalUseCase.executeWithBreakdown(month.year, month.monthNumber)
                ) { expensesStats, totalData ->
                    _uiState.value = HomeUiState(
                        expenses = expensesStats.expenses,
                        monthlyTotal = totalData.total,
                        hasExpenses = expensesStats.hasExpenses,
                        isLoading = false,
                        error = null
                    )
                }.collect { }
            }
        }
    }

    fun goToPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minus(DatePeriod(months = 1))
    }

    fun goToNextMonth() {
        _currentMonth.value = _currentMonth.value.plus(DatePeriod(months = 1))
    }
}

data class HomeUiState(
    val expenses: List<Expense> = emptyList(),
    val monthlyTotal: BigDecimal = BigDecimal.ZERO,
    val hasExpenses: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
