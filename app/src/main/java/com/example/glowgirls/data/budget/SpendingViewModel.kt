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

    private val auth = FirebaseAuth.getInstance()

    init {
        loadData()
    }

    private fun loadData() {
        if (auth.currentUser == null) {
            // User not authenticated, handle gracefully
            return
        }

        viewModelScope.launch {
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
        }
    }
}