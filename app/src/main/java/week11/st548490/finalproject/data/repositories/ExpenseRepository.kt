package week11.st548490.finalproject.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import week11.st548490.finalproject.data.models.Expense

class ExpenseRepository {

    private val db = FirebaseFirestore.getInstance()

    // Helper: path to the expense subcollection inside an event
    private fun eventExpenseCollection(eventId: String) =
        db.collection("events")
            .document(eventId)
            .collection("expenses")

    // ---------------------------
    // SAVE NEW EXPENSE
    // ---------------------------
    suspend fun saveExpense(expense: Expense): String? {
        return try {
            val collection = eventExpenseCollection(expense.eventId)

            val docRef = collection.document()
            val newExpense = expense.copy(id = docRef.id)

            docRef.set(newExpense).await()
            docRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ---------------------------
    // GET EXPENSE FOR AN EVENT
    // ---------------------------
    suspend fun getExpenseByEventId(eventId: String): Expense? {
        return try {
            val query = eventExpenseCollection(eventId)
                .limit(1)
                .get()
                .await()

            if (!query.isEmpty) {
                query.documents[0].toObject(Expense::class.java)
            } else null

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ---------------------------
    // UPDATE EXPENSE
    // ---------------------------
    suspend fun updateExpense(expense: Expense): Boolean {
        return try {
            eventExpenseCollection(expense.eventId)
                .document(expense.id)
                .set(expense)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
