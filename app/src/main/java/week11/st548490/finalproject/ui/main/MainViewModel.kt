package week11.st548490.finalproject.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st548490.finalproject.data.models.Event
import week11.st548490.finalproject.data.repositories.EventRepository

class MainViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    fun loadUserEvents(userId: String) {
        viewModelScope.launch {
            try {
                eventRepository.getEventsForUser(userId).collect { eventsList ->
                    _events.value = eventsList
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }
}