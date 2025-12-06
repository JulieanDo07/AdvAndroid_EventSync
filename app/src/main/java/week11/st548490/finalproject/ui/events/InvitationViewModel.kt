// InvitationViewModel.kt
package week11.st548490.finalproject.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import week11.st548490.finalproject.data.models.Event

class InvitationViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _pendingInvitations = MutableStateFlow<List<Event>>(emptyList())
    val pendingInvitations: StateFlow<List<Event>> = _pendingInvitations.asStateFlow()

    private val _invitationActionState = MutableStateFlow<InvitationActionState>(InvitationActionState.Idle)
    val invitationActionState: StateFlow<InvitationActionState> = _invitationActionState.asStateFlow()

    private var invitationsListener: ListenerRegistration? = null
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    fun loadPendingInvitations() {
        viewModelScope.launch {
            try {
                invitationsListener?.remove()

                invitationsListener = firestore.collection("events")
                    .whereArrayContains("pendingInvites", currentUserId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _pendingInvitations.value = emptyList()
                            return@addSnapshotListener
                        }

                        val invitations = snapshot?.documents?.mapNotNull { document ->
                            try {
                                document.toObject(Event::class.java)?.copy(id = document.id)
                            } catch (e: Exception) {
                                null
                            }
                        } ?: emptyList()

                        _pendingInvitations.value = invitations
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptInvitation(eventId: String) {
        viewModelScope.launch {
            _invitationActionState.value = InvitationActionState.Loading
            try {
                // Use a simple approach without the repository for now
                val success = acceptInvitationDirect(eventId, currentUserId)
                if (success) {
                    _invitationActionState.value = InvitationActionState.Success("Invitation accepted!")
                } else {
                    _invitationActionState.value = InvitationActionState.Error("Failed to accept invitation")
                }
            } catch (e: Exception) {
                _invitationActionState.value = InvitationActionState.Error(e.message ?: "Error accepting invitation")
            }

            // Reset state after 2 seconds
            launch {
                kotlinx.coroutines.delay(2000)
                _invitationActionState.value = InvitationActionState.Idle
            }
        }
    }

    fun declineInvitation(eventId: String) {
        viewModelScope.launch {
            _invitationActionState.value = InvitationActionState.Loading
            try {
                val success = declineInvitationDirect(eventId, currentUserId)
                if (success) {
                    _invitationActionState.value = InvitationActionState.Success("Invitation declined")
                } else {
                    _invitationActionState.value = InvitationActionState.Error("Failed to decline invitation")
                }
            } catch (e: Exception) {
                _invitationActionState.value = InvitationActionState.Error(e.message ?: "Error declining invitation")
            }

            // Reset state after 2 seconds
            launch {
                kotlinx.coroutines.delay(2000)
                _invitationActionState.value = InvitationActionState.Idle
            }
        }
    }

    private suspend fun acceptInvitationDirect(eventId: String, userId: String): Boolean {
        return try {
            firestore.runTransaction { transaction ->
                val eventRef = firestore.collection("events").document(eventId)
                val event = transaction.get(eventRef)

                if (!event.exists()) {
                    throw Exception("Event not found")
                }

                val pendingInvites = event.get("pendingInvites") as? List<String> ?: emptyList()
                val members = event.get("members") as? List<String> ?: emptyList()

                // Check if user is actually invited
                if (!pendingInvites.contains(userId)) {
                    throw Exception("User is not invited to this event")
                }

                // Create updated lists
                val updatedPendingInvites = pendingInvites - userId
                val updatedMembers = if (!members.contains(userId)) {
                    members + userId
                } else {
                    members
                }

                // Update the event
                transaction.update(eventRef,
                    mapOf(
                        "pendingInvites" to updatedPendingInvites,
                        "members" to updatedMembers
                    )
                )
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun declineInvitationDirect(eventId: String, userId: String): Boolean {
        return try {
            firestore.runTransaction { transaction ->
                val eventRef = firestore.collection("events").document(eventId)
                val event = transaction.get(eventRef)

                if (!event.exists()) {
                    throw Exception("Event not found")
                }

                val pendingInvites = event.get("pendingInvites") as? List<String> ?: emptyList()

                // Check if user is actually invited
                if (!pendingInvites.contains(userId)) {
                    throw Exception("User is not invited to this event")
                }

                // Remove user from pending invites
                val updatedPendingInvites = pendingInvites - userId

                // Update the event
                transaction.update(eventRef, "pendingInvites", updatedPendingInvites)
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        invitationsListener?.remove()
    }
}

sealed class InvitationActionState {
    object Idle : InvitationActionState()
    object Loading : InvitationActionState()
    data class Success(val message: String) : InvitationActionState()
    data class Error(val message: String) : InvitationActionState()
}