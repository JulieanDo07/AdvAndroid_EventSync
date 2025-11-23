package week11.st548490.finalproject.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
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
        val events = snapshot.documents.map { it.toObject<Event>()!!.copy(id = it.id) }
        emit(events)
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
            User(id = it.id, email = it.getString("email") ?: "", displayName = it.getString("displayName") ?: "")
        }
    }
}