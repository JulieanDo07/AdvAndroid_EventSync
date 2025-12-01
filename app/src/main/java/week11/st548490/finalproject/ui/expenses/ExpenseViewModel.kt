package week11.st548490.finalproject.ui.expenses

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// Each expense row
data class ExpenseItem(
    val name: String = "",
    val price: String = ""
)

class ExpenseViewModel : ViewModel() {


    // Title of expense page
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    fun updateTitle(newValue: String) {
        _title.value = newValue
    }


    // manually input how much to split the cost per person

    private val _divideBy = MutableStateFlow("")
    val divideBy: StateFlow<String> = _divideBy

    fun updateDivideBy(newValue: String) {
        _divideBy.value = newValue
        recalcCostPerPerson()
    }

    // Item expense List
    private val _items = MutableStateFlow(listOf(ExpenseItem()))
    val items: StateFlow<List<ExpenseItem>> = _items

    fun addItem() {
        _items.update { it + ExpenseItem() }
        recalcTotal()
    }

    fun updateItemName(index: Int, value: String) {
        _items.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) item.copy(name = value) else item
            }
        }
    }

    fun updateItemPrice(index: Int, value: String) {
        _items.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) item.copy(price = value) else item
            }
        }
        recalcTotal()
    }

    // Total cost of items
    private val _totalCost = MutableStateFlow("0.00")
    val totalCost: StateFlow<String> = _totalCost

    private fun recalcTotal() {
        val sum = _items.value.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
        _totalCost.value = "%.2f".format(sum)
        recalcCostPerPerson()
    }


    // Pick attendees to split expenses with
    private val _attendees = MutableStateFlow<List<String>>(emptyList())
    val attendees: StateFlow<List<String>> = _attendees

    fun updateAttendees(list: List<String>) {
        _attendees.value = list
        recalcCostPerPerson()
    }

    fun addAttendee(person: String) {
        _attendees.update {
            if (person !in it) it + person else it
        }
        recalcCostPerPerson()
    }

    fun removeAttendee(person: String) {
        _attendees.update {
            it.filterNot { name -> name == person }
        }
        recalcCostPerPerson()
    }


    // List of attendees - this is temporary just to show people
    private val _allPossibleAttendees = MutableStateFlow(
        listOf(
            "Lando Norris",
            "Oscar Piastri",
            "Max Verstappen",
            "Juliean Do",
            "Atin Atin",
            "Simran Simran"
        )
    )
    val allPossibleAttendees: StateFlow<List<String>> = _allPossibleAttendees


    // Cost per person based on attendees selected
    private val _costPerPerson = MutableStateFlow("0.00")
    val costPerPerson: StateFlow<String> = _costPerPerson

    private fun recalcCostPerPerson() {
        val total = _totalCost.value.toDoubleOrNull() ?: 0.0
        val people = _attendees.value.size.takeIf { it > 0 }

        _costPerPerson.value = if (people == null || people == 0)
            "0.00"
        else
            "%.2f".format(total / people)
    }
}
