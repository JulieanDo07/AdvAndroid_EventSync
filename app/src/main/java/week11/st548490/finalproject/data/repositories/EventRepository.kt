package week11.st548490.finalproject.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import week11.st548490.finalproject.data.models.Event
import week11.st548490.finalproject.data.models.User

class EventRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val eventsCollection = firestore.collection("events")
    private val usersCollection = firestore.collection("users")

    suspend fun createEvent(event: Event): String {
        val documentRef = eventsCollection.add(event).await()
        return documentRef.id
    }

    fun getEventsForUser(userId: String): Flow<List<Event>> = flow {
        val snapshot = eventsCollection
            .whereArrayContains("members", userId)
            .get()
            .await()
        val events = snapshot.documents.map {
            it.toObject<Event>()!!.copy(id = it.id)
        }
        emit(events)
    }

    // NEW: Get pending invitations for user
    fun getPendingInvitationsForUser(userId: String): Flow<List<Event>> = flow {
        val snapshot = eventsCollection
            .whereArrayContains("pendingInvites", userId)
            .get()
            .await()
        val events = snapshot.documents.map {
            it.toObject<Event>()!!.copy(id = it.id)
        }
        emit(events)
    }

    // NEW: Real-time listener for pending invitations - FIXED VERSION
    fun getPendingInvitationsListener(userId: String): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .whereArrayContains("pendingInvites", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val newEvents = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject<Event>()?.copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(newEvents)
            }

        // Cleanup listener when flow is cancelled
        awaitClose {
            listener.remove()
        }
    }

    // NEW: Accept invitation - moves user from pendingInvites to members
    suspend fun acceptInvitation(eventId: String, userId: String): Boolean {
        return try {
            firestore.runTransaction { transaction ->
                val eventRef = eventsCollection.document(eventId)
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

    // NEW: Decline invitation - removes user from pendingInvites
    suspend fun declineInvitation(eventId: String, userId: String): Boolean {
        return try {
            firestore.runTransaction { transaction ->
                val eventRef = eventsCollection.document(eventId)
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

    suspend fun getEvent(eventId: String): Event? {
        return eventsCollection.document(eventId).get().await().toObject<Event>()
    }

    suspend fun updateEvent(eventId: String, updates: Map<String, Any>) {
        eventsCollection.document(eventId).update(updates).await()
    }

    suspend fun deleteEvent(eventId: String) {
        eventsCollection.document(eventId).delete().await()
    }

    suspend fun getAllUsers(): List<User> {
        val snapshot = usersCollection.get().await()
        return snapshot.documents.map {
            User(
                id = it.id,
                email = it.getString("email") ?: "",
                displayName = it.getString("displayName") ?: ""
            )
        }
    }

    // NEW: Get events where user is creator
    suspend fun getEventsCreatedByUser(userId: String): List<Event> {
        val snapshot = eventsCollection
            .whereEqualTo("creatorId", userId)
            .get()
            .await()
        return snapshot.documents.map {
            it.toObject<Event>()!!.copy(id = it.id)
        }
    }

    // NEW: Add user to event (for creator adding people after creation)
    suspend fun addUserToEvent(eventId: String, userId: String, asPending: Boolean = true): Boolean {
        return try {
            val eventRef = eventsCollection.document(eventId)

            if (asPending) {
                // Add to pending invites
                eventRef.update("pendingInvites", FieldValue.arrayUnion(userId)).await()
            } else {
                // Add directly to members
                eventRef.update("members", FieldValue.arrayUnion(userId)).await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // NEW: Remove user from event
    suspend fun removeUserFromEvent(eventId: String, userId: String): Boolean {
        return try {
            val eventRef = eventsCollection.document(eventId)

            // Remove from both pending invites and members
            firestore.runTransaction { transaction ->
                val event = transaction.get(eventRef)
                if (!event.exists()) {
                    throw Exception("Event not found")
                }

                // Remove from pending invites
                val pendingInvites = event.get("pendingInvites") as? List<String> ?: emptyList()
                if (pendingInvites.contains(userId)) {
                    transaction.update(eventRef, "pendingInvites", pendingInvites - userId)
                }

                // Remove from members (unless they're the creator)
                val members = event.get("members") as? List<String> ?: emptyList()
                val creatorId = event.getString("creatorId") ?: ""
                if (members.contains(userId) && userId != creatorId) {
                    transaction.update(eventRef, "members", members - userId)
                }
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // NEW: Check if user can view event details
    suspend fun canUserViewEvent(eventId: String, userId: String): Boolean {
        return try {
            val event = getEvent(eventId)
            event != null && (event.members.contains(userId) || event.pendingInvites.contains(userId))
        } catch (e: Exception) {
            false
        }
    }

    // NEW: Check if user can edit/delete event
    suspend fun canUserEditEvent(eventId: String, userId: String): Boolean {
        return try {
            val event = getEvent(eventId)
            event != null && event.creatorId == userId
        } catch (e: Exception) {
            false
        }
    }
}