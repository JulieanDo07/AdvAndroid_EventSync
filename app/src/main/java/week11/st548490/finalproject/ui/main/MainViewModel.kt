package week11.st548490.finalproject.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st548490.finalproject.data.models.Event

class MainViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var eventsListener: ListenerRegistration? = null

    // Current user ID for convenience
    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    fun loadUserEvents(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Remove existing listener
                eventsListener?.remove()

                // Load only events where user is a member (not just invited)
                eventsListener = firestore.collection("events")
                    .whereArrayContains("members", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _loading.value = false
                            return@addSnapshotListener
                        }

                        val eventsList = snapshot?.documents?.mapNotNull { document ->
                            try {
                                val event = document.toObject(Event::class.java)
                                event?.copy(id = document.id)
                            } catch (e: Exception) {
                                null
                            }
                        } ?: emptyList()

                        // Sort by date (newest first)
                        _events.value = eventsList.sortedByDescending { it.date.toDate() }
                        _loading.value = false
                    }
            } catch (e: Exception) {
                _events.value = emptyList()
                _loading.value = false
                e.printStackTrace()
            }
        }
    }

    fun refreshEvents() {
        auth.currentUser?.uid?.let { userId ->
            loadUserEvents(userId)
        }
    }

    fun searchEvents(query: String): List<Event> {
        return if (query.isBlank()) {
            _events.value
        } else {
            _events.value.filter { event ->
                event.title.contains(query, ignoreCase = true) ||
                        event.description.contains(query, ignoreCase = true) ||
                        event.location.contains(query, ignoreCase = true)
            }
        }
    }

    fun getEventById(eventId: String): Event? {
        return _events.value.find { it.id == eventId }
    }

    fun isUserCreator(eventId: String): Boolean {
        val event = getEventById(eventId)
        return event?.creatorId == currentUserId
    }

    fun isUserMember(eventId: String): Boolean {
        val event = getEventById(eventId)
        return event?.members?.contains(currentUserId) == true
    }

    override fun onCleared() {
        super.onCleared()
        eventsListener?.remove()
    }
}