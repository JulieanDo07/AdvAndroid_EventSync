package week11.st548490.finalproject.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st548490.finalproject.data.models.Event
import week11.st548490.finalproject.data.repositories.EventRepository

class EventDetailsViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            try {
                val eventData = eventRepository.getEvent(eventId)
                _event.value = eventData
            } catch (e: Exception) {
                e.printStackTrace()
            }
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