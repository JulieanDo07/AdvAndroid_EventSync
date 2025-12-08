package week11.st548490.finalproject.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st548490.finalproject.data.models.Expense
import week11.st548490.finalproject.data.models.ExpenseItem
import week11.st548490.finalproject.data.repositories.ExpenseRepository

class ExpenseViewModel : ViewModel() {
    private val repo = ExpenseRepository()

    private val _expense = MutableStateFlow(
        Expense(
            id = "",
            eventId = "",
            title = "",
            divideBy = 1,
            items = emptyList(),
            attendees = emptyList(),
            totalCost = 0.0,
            costPerPerson = 0.0
        )
    )

    val expense = _expense.asStateFlow()
    fun divideByString(): String = _expense.value.divideBy.toString()


    // Load expense for a given event
    fun loadExpense(eventId: String) {
        viewModelScope.launch {
            val existing = repo.getExpenseByEventId(eventId)

            _expense.value = existing ?: Expense(eventId = eventId)
        }
    }

    // Title + DivideBy
    fun updateTitle(text: String) {
        _expense.value = _expense.value.copy(title = text)
    }

    fun updateDivideBy(num: String) {
        val divide = num.toIntOrNull() ?: 1
        _expense.value = _expense.value.copy(divideBy = divide)
        calculateCosts()
    }

    // Attendees
    fun updateAttendees(list: List<String>) {
        _expense.value = _expense.value.copy(attendees = list)
        calculateCosts()
    }

    // Items
    fun addItem() {
        val newItems = _expense.value.items.toMutableList()
        newItems.add(ExpenseItem("", 0.0))

        _expense.value = _expense.value.copy(items = newItems)
        calculateCosts()
    }

    fun updateItemName(index: Int, name: String) {
        val newItems = _expense.value.items.toMutableList()
        newItems[index] = newItems[index].copy(name = name)
        _expense.value = _expense.value.copy(items = newItems)
    }

    fun updateItemPrice(index: Int, price: String) {
        val newItems = _expense.value.items.toMutableList()
        newItems[index] = newItems[index].copy(price = price.toDoubleOrNull() ?: 0.0)

        _expense.value = _expense.value.copy(items = newItems)
        calculateCosts()
    }

    // Cost Calculation
    private fun calculateCosts() {
        val total = _expense.value.items.sumOf { it.price }
        val people = _expense.value.attendees.size.takeIf { it > 0 } ?: 1

        _expense.value = _expense.value.copy(
            totalCost = total,
            costPerPerson = total / people
        )
    }

    // Save or update expense
    fun saveExpense(onFinish: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exp = _expense.value

            val success = if (exp.id.isBlank()) {
                // new expense
                repo.saveExpense(exp) != null
            } else {
                // updating
                repo.updateExpense(exp)
            }

            onFinish(success)
        }
    }
}
