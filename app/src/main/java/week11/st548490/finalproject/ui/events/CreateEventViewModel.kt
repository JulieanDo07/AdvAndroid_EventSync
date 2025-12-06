package week11.st548490.finalproject.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import week11.st548490.finalproject.data.models.Event
import week11.st548490.finalproject.data.models.User
import week11.st548490.finalproject.data.repositories.EventRepository

class CreateEventViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _createEventState = MutableStateFlow<CreateEventState>(CreateEventState.Idle)
    val createEventState: StateFlow<CreateEventState> = _createEventState.asStateFlow()

    private val _eventFormData = MutableStateFlow(EventFormData())
    val eventFormData: StateFlow<EventFormData> = _eventFormData.asStateFlow()

    // Add expense data to track
    private val _expenseSummary = MutableStateFlow(ExpenseSummary())
    val expenseSummary: StateFlow<ExpenseSummary> = _expenseSummary.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    data class EventFormData(
        val eventName: String = "",
        val description: String = "",
        val selectedDate: String = "",
        val selectedTime: String = "",
        val selectedTheme: String = "#4FC3F7",
        val budget: String = "",
        val location: String = "",
        val selectedUsers: List<User> = emptyList()
    )

    data class ExpenseSummary(
        val hasExpenses: Boolean = false,
        val totalCost: String = "",
        val costPerPerson: String = ""
    )

    fun updateFormData(update: (EventFormData) -> EventFormData) {
        _eventFormData.value = update(_eventFormData.value)
    }

    fun updateExpenseSummary(hasExpenses: Boolean, totalCost: String = "", costPerPerson: String = "") {
        _expenseSummary.value = ExpenseSummary(hasExpenses, totalCost, costPerPerson)
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").get().await()
                val usersList = snapshot.documents.mapNotNull { document ->
                    try {
                        User(
                            id = document.id,
                            email = document.getString("email") ?: "",
                            displayName = document.getString("displayName") ?:
                            (document.getString("email")?.substringBefore("@") ?: "Unknown User")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _users.value = usersList
            } catch (e: Exception) {
                e.printStackTrace()
                _users.value = emptyList()
            }
        }
    }

    fun createEvent(creatorId: String) {
        viewModelScope.launch {
            _createEventState.value = CreateEventState.Loading
            try {
                val form = _eventFormData.value
                val allMembers = form.selectedUsers.map { it.id } + creatorId

                // Separate creator from invitees
                val invitees = form.selectedUsers.map { it.id }

                val event = Event(
                    title = form.eventName,
                    description = form.description,
                    date = Timestamp.now(), // TODO: Parse actual date
                    time = form.selectedTime,
                    themeColor = form.selectedTheme,
                    imageUrl = "",
                    creatorId = creatorId,
                    members = listOf(creatorId), // Only creator is initially a member
                    pendingInvites = invitees, // Others are pending
                    budget = form.budget.toDoubleOrNull() ?: 0.0,
                    location = form.location,
                    expenseSummary = if (_expenseSummary.value.hasExpenses) {
                        mapOf(
                            "totalCost" to _expenseSummary.value.totalCost,
                            "costPerPerson" to _expenseSummary.value.costPerPerson
                        )
                    } else {
                        emptyMap()
                    }
                )

                val eventId = eventRepository.createEvent(event)
                clearAllFormData()
                _createEventState.value = CreateEventState.Success(eventId)
            } catch (e: Exception) {
                _createEventState.value = CreateEventState.Error(e.message ?: "Failed to create event")
            }
        }
    }

    // Clear all form data (called after successful event creation)
    private fun clearAllFormData() {
        _eventFormData.value = EventFormData()
        _expenseSummary.value = ExpenseSummary()
        // Note: We'll clear the shared EventExpenseViewModel data separately
    }

    // For editing events
    fun loadEventForEditing(eventId: String) {
        viewModelScope.launch {
            try {
                val event = eventRepository.getEvent(eventId)
                // Pre-fill form data for editing
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        date: String,
        time: String,
        themeColor: String,
        budget: Double,
        location: String,
        invitedUserIds: List<String>
    ) {
        viewModelScope.launch {
            _createEventState.value = CreateEventState.Loading
            try {
                val updates = mapOf(
                    "title" to title,
                    "description" to description,
                    "time" to time,
                    "themeColor" to themeColor,
                    "budget" to budget,
                    "location" to location,
                    "members" to invitedUserIds
                )

                eventRepository.updateEvent(eventId, updates)
                _createEventState.value = CreateEventState.Success(eventId)
            } catch (e: Exception) {
                _createEventState.value = CreateEventState.Error(e.message ?: "Failed to update event")
            }
        }
    }
}

sealed class CreateEventState {
    object Idle : CreateEventState()
    object Loading : CreateEventState()
    data class Success(val eventId: String) : CreateEventState()
    data class Error(val message: String) : CreateEventState()
}