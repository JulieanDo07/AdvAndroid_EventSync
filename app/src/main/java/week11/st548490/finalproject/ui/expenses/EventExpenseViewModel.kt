package week11.st548490.finalproject.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st548490.finalproject.data.models.Expense
import week11.st548490.finalproject.data.models.ExpenseItem
import week11.st548490.finalproject.data.repositories.ExpenseRepository
import kotlin.math.round

class EventExpenseViewModel : ViewModel() {
    //repository that saves and loads expenses into firestore

    private val repo = ExpenseRepository()

    private val _expenseData = MutableStateFlow(
        Expense(
            id = "",
            eventId = "",
            title = "",
            divideBy = 1,
            items = listOf(),
            attendees = listOf(),
            totalCost = 0.0,
            costPerPerson = 0.0
        )
    )
    val expenseData: StateFlow<Expense> = _expenseData

    // converts int into string for textfields
    fun divideByString(): String {
        return _expenseData.value.divideBy.toString()
    }

    fun loadExpenseForEvent(eventId: String) {
        viewModelScope.launch {
            val existing = repo.getExpenseByEventId(eventId)
            if (existing != null) {
                _expenseData.value = existing
            } else {
                _expenseData.value = _expenseData.value.copy(eventId = eventId)
            }
        }
    }

    //saves current expense into firestore

    fun saveExpense(eventId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val updated = _expenseData.value.copy(eventId = eventId)
            val success = repo.saveExpense(updated) != null
            onResult(success)
        }
    }

    // Updates divideby

    fun updateTitle(text: String) {
        _expenseData.value = _expenseData.value.copy(title = text)
    }

    // Updates divideby
    fun updateDivideBy(value: String) {
        val num = value.toIntOrNull() ?: 1
        _expenseData.value = _expenseData.value.copy(divideBy = num)
        recalcCost()
    }
    //adds new item row
    fun addItem() {
        val updated = _expenseData.value.items.toMutableList()
        updated.add(ExpenseItem("", 0.0))
        _expenseData.value = _expenseData.value.copy(items = updated)
        recalcCost()
    }
    //updates item name
    fun updateItemName(index: Int, name: String) {
        val updated = _expenseData.value.items.toMutableList()
        updated[index] = updated[index].copy(name = name)
        _expenseData.value = _expenseData.value.copy(items = updated)
    }
    //updates item price
    fun updateItemPrice(index: Int, price: String) {
        val updated = _expenseData.value.items.toMutableList()
        updated[index] = updated[index].copy(price = price.toDoubleOrNull() ?: 0.0)
        _expenseData.value = _expenseData.value.copy(items = updated)
        recalcCost()
    }
    //updates split total with attendees
    fun updateAttendees(attendees: List<String>) {
        _expenseData.value = _expenseData.value.copy(attendees = attendees)
        recalcCost()
    }
    //recalculates total cost with cost person/split
    private fun recalcCost() {
        val items = _expenseData.value.items
        val total = items.sumOf { it.price }
        val people = _expenseData.value.attendees.size.takeIf { it > 0 } ?: 1

        _expenseData.value = _expenseData.value.copy(
            totalCost = round(total * 100) / 100,
            costPerPerson = round((total / people) * 100) / 100
        )
    }
    fun clearAllData() {
        _expenseData.value = Expense(
            id = "",
            eventId = "",
            title = "",
            divideBy = 1,
            items = listOf(),
            attendees = listOf(),
            totalCost = 0.0,
            costPerPerson = 0.0
        )
    }



}