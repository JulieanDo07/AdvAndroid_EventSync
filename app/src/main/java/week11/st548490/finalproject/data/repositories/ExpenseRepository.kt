package week11.st548490.finalproject.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import week11.st548490.finalproject.data.models.Expense

class ExpenseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val expenseCollection = db.collection("expenses")

    suspend fun saveExpense(expense: Expense): String? {
        return try {
            val docRef = expenseCollection.document()
            val newExpense = expense.copy(id = docRef.id)
            docRef.set(newExpense).await()
            docRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getExpenseByEventId(eventId: String): Expense? {
        return try {
            val query = expenseCollection
                .whereEqualTo("eventId", eventId)
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

    suspend fun updateExpense(expense: Expense): Boolean {
        return try {
            expenseCollection.document(expense.id)
                .set(expense)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
