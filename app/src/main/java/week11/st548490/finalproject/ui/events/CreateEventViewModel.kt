package week11.st548490.finalproject.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
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

    private val firestore = FirebaseFirestore.getInstance()

    fun loadUsers() {
        viewModelScope.launch {
            try {
                // Get all users from Firestore users collection
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
                        null // Skip invalid user documents
                    }
                }
                _users.value = usersList
                println("DEBUG: Loaded ${usersList.size} users from Firestore")
            } catch (e: Exception) {
                e.printStackTrace()
                println("DEBUG: Error loading users: ${e.message}")
                _users.value = emptyList()
            }
        }
    }

    fun createEvent(
        title: String,
        description: String,
        date: String,
        time: String,
        themeColor: String,
        budget: Double,
        location: String,
        creatorId: String,
        invitedUserIds: List<String>
    ) {
        viewModelScope.launch {
            _createEventState.value = CreateEventState.Loading
            try {
                val allMembers = invitedUserIds + creatorId

                val event = Event(
                    title = title,
                    description = description,
                    date = Timestamp.now(), // TODO: Parse actual date
                    time = time,
                    themeColor = themeColor,
                    imageUrl = "",
                    creatorId = creatorId,
                    members = allMembers,
                    budget = budget,
                    location = location
                )

                val eventId = eventRepository.createEvent(event)
                _createEventState.value = CreateEventState.Success(eventId)
            } catch (e: Exception) {
                _createEventState.value = CreateEventState.Error(e.message ?: "Failed to create event")
            }
        }
    }

    // New function to load event for editing
    fun loadEventForEditing(eventId: String) {
        viewModelScope.launch {
            try {
                val event = eventRepository.getEvent(eventId)
                // You can store this for pre-filling the form
                // We'll handle this in the UI
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // New function to update event
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