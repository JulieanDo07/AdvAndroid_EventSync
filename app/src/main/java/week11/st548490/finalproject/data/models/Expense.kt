package week11.st548490.finalproject.data.models

data class Expense(
    val id: String = "",
    val eventId: String = "",
    val title: String = "",
    val divideBy: Int = 1,
    val items: List<ExpenseItem> = emptyList(),
    val attendees: List<String> = emptyList(),
    val totalCost: Double = 0.0,
    val costPerPerson: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
