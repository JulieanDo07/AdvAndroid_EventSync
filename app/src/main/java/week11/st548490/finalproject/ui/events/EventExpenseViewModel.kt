package week11.st548490.finalproject.ui.events

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ExpenseItem data class
data class ExpenseItem(
    val name: String = "",
    val price: String = ""
)

// ExpenseData data class
data class ExpenseData(
    val title: String = "",
    val divideBy: String = "",
    val items: List<ExpenseItem> = listOf(ExpenseItem()),
    val attendees: List<String> = emptyList(),
    val totalCost: String = "0.00",
    val costPerPerson: String = "0.00"
)

// Single EventExpenseViewModel class
class EventExpenseViewModel : ViewModel() {
    private val _expenseData = MutableStateFlow(ExpenseData())
    val expenseData: StateFlow<ExpenseData> = _expenseData.asStateFlow()

    // Update functions
    fun updateTitle(title: String) {
        _expenseData.update { it.copy(title = title) }
        recalculateTotals()
    }

    fun updateDivideBy(divideBy: String) {
        _expenseData.update { it.copy(divideBy = divideBy) }
        recalculateTotals()
    }

    fun addItem() {
        _expenseData.update { it.copy(items = it.items + ExpenseItem()) }
    }

    fun updateItemName(index: Int, name: String) {
        _expenseData.update { current ->
            val updatedItems = current.items.toMutableList()
            if (index < updatedItems.size) {
                updatedItems[index] = updatedItems[index].copy(name = name)
            }
            current.copy(items = updatedItems)
        }
    }

    fun updateItemPrice(index: Int, price: String) {
        _expenseData.update { current ->
            val updatedItems = current.items.toMutableList()
            if (index < updatedItems.size) {
                updatedItems[index] = updatedItems[index].copy(price = price)
            }
            current.copy(items = updatedItems)
        }
        recalculateTotals()
    }

    fun updateAttendees(attendees: List<String>) {
        _expenseData.update { it.copy(attendees = attendees) }
        recalculateTotals()
    }

    private fun recalculateTotals() {
        val total = _expenseData.value.items.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
        val peopleCount = _expenseData.value.attendees.size.takeIf { it > 0 } ?: 1

        _expenseData.update {
            it.copy(
                totalCost = "%.2f".format(total),
                costPerPerson = "%.2f".format(total / peopleCount)
            )
        }
    }

    fun clearAllData() {
        _expenseData.value = ExpenseData()
    }

    fun hasData(): Boolean {
        return _expenseData.value.items.any { it.name.isNotBlank() || it.price.isNotBlank() } ||
                _expenseData.value.title.isNotBlank() ||
                _expenseData.value.attendees.isNotEmpty()
    }
}