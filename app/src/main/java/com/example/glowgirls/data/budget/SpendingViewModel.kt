package com.example.glowgirls.data.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowgirls.models.budget.CategoryProgress
import com.example.glowgirls.models.budget.Expense
import com.example.glowgirls.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SpendingViewModel(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _categoryProgress = MutableStateFlow<List<CategoryProgress>>(emptyList())
    val categoryProgress: StateFlow<List<CategoryProgress>> = _categoryProgress

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _totalSpent = MutableStateFlow(0.0)
    val totalSpent: StateFlow<Double> = _totalSpent

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val auth = FirebaseAuth.getInstance()

    init {
        loadData()
    }

    private fun loadData() {
        if (auth.currentUser == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Collect category progress
                budgetRepository.getCategoryProgress().collect { progress ->
                    _categoryProgress.value = progress.map {
                        CategoryProgress(it.name, it.allocated, it.spent)
                    }
                    _totalSpent.value = progress.sumOf { it.spent }
                }
                // Collect expenses
                budgetRepository.getExpenses().collect { expenses ->
                    _expenses.value = expenses.sortedByDescending { it.date }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addExpense(expense: Expense) {
        if (auth.currentUser == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            try {
                budgetRepository.saveExpense(expense)
                // Data will be updated through the Flow collection
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save expense: ${e.message}"
            }
        }
    }

    fun updateCategoryAllocation(categoryName: String, newAllocation: Double) {
        if (auth.currentUser == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            try {
                // Using the new method in BudgetRepository
                budgetRepository.updateCategoryAllocation(categoryName, newAllocation)
                // Data will be updated through the Flow collection
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update category allocation: ${e.message}"
            }
        }
    }

    fun deleteExpense(expenseId: String, category: String, amount: Double) {
        if (auth.currentUser == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            try {
                budgetRepository.deleteExpense(expenseId, category, amount)
                // Data will be updated through the Flow collection
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete expense: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshData() {
        loadData()
    }
}