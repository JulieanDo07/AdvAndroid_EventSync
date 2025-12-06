// EventInvitationRepository.kt
package week11.st548490.finalproject.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventInvitationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun acceptInvitation(eventId: String, userId: String): Boolean {
        return try {
            val eventRef = firestore.collection("events").document(eventId)

            firestore.runTransaction { transaction ->
                val event = transaction.get(eventRef)

                if (!event.exists()) {
                    throw Exception("Event not found")
                }

                val pendingInvites = event.get("pendingInvites") as? List<String> ?: emptyList()
                val members = event.get("members") as? List<String> ?: emptyList()

                // Remove user from pending invites
                val updatedPendingInvites = pendingInvites - userId
                // Add user to members
                val updatedMembers = members + userId

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

    suspend fun declineInvitation(eventId: String, userId: String): Boolean {
        return try {
            val eventRef = firestore.collection("events").document(eventId)

            firestore.runTransaction { transaction ->
                val event = transaction.get(eventRef)

                if (!event.exists()) {
                    throw Exception("Event not found")
                }

                val pendingInvites = event.get("pendingInvites") as? List<String> ?: emptyList()

                // Remove user from pending invites only
                val updatedPendingInvites = pendingInvites - userId

                transaction.update(eventRef,
                    mapOf("pendingInvites" to updatedPendingInvites)
                )
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}