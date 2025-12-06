package week11.st548490.finalproject.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import week11.st548490.finalproject.data.models.Event
import week11.st548490.finalproject.data.repositories.EventRepository

class EventDetailsViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _attendeeNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val attendeeNames: StateFlow<Map<String, String>> = _attendeeNames.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private var currentEventId: String? = null

    fun loadEvent(eventId: String) {
        currentEventId = eventId
        viewModelScope.launch {
            try {
                val eventData = eventRepository.getEvent(eventId)
                _event.value = eventData

                // Load attendee names
                eventData?.members?.let { members ->
                    loadAttendeeNames(members)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadAttendeeNames(memberIds: List<String>) {
        try {
            val namesMap = mutableMapOf<String, String>()

            // If no members, return empty map
            if (memberIds.isEmpty()) {
                _attendeeNames.value = emptyMap()
                return
            }

            // Set initial loading state
            val initialMap = memberIds.associateWith { "Loading..." }
            _attendeeNames.value = initialMap

            // Fetch user data for each member using await() for proper async handling
            for (memberId in memberIds) {
                try {
                    val document = db.collection("users").document(memberId).get().await()

                    if (document.exists()) {
                        val userName = document.getString("name")
                            ?: document.getString("email")
                            ?: "User $memberId"
                        namesMap[memberId] = userName
                    } else {
                        // If user doesn't exist, use placeholder
                        namesMap[memberId] = "User $memberId"
                    }
                } catch (e: Exception) {
                    // If failed to fetch, use placeholder
                    namesMap[memberId] = "User $memberId"
                }

                // Update the flow after each fetch so UI shows progress
                _attendeeNames.value = namesMap.toMutableMap().apply {
                    // Add placeholders for remaining users
                    memberIds.forEach { id ->
                        if (!containsKey(id)) {
                            put(id, "Loading...")
                        }
                    }
                }
            }

            // Final update
            _attendeeNames.value = namesMap

        } catch (e: Exception) {
            // If error, create placeholder names
            val placeholderMap = memberIds.associateWith { "User $it" }
            _attendeeNames.value = placeholderMap
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                eventRepository.deleteEvent(eventId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}