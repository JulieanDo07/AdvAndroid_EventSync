package week11.st548490.finalproject.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Timestamp = Timestamp.now(),
    val time: String = "",
    val themeColor: String = "#4FC3F7",
    val imageUrl: String = "",
    val creatorId: String = "",
    val members: List<String> = emptyList(),
    val pendingInvites: List<String> = emptyList(),
    val budget: Double = 0.0,
    val location: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val expenseSummary: Map<String, String> = emptyMap()
){
    fun isUserMember(userId: String): Boolean {
        return members.contains(userId)
    }

    fun isUserInvited(userId: String): Boolean {
        return pendingInvites.contains(userId)
    }

    fun hasUserResponded(userId: String): Boolean {
        return !pendingInvites.contains(userId) && !members.contains(userId)
    }
}

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val name: String = "",
    val profileImageUrl: String = ""
)