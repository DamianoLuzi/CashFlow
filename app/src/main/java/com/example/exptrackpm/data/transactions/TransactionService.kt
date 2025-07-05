
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


object TransactionService {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    fun addTransaction(transaction: Transaction, onSuccess: (Boolean) -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return

        val txn = Transaction(
            userId = userId,
            amount = transaction.amount,
            description = transaction.description,
            category = transaction.category,
            type = transaction.type,
            date = Timestamp.now(),
            receiptUrl = transaction.receiptUrl
        )

        val collectionRef = db.collection("transaction")
        collectionRef
            .add(txn)
            .addOnSuccessListener { documentRef ->
                documentRef.update("id", documentRef.id)
                onSuccess(true)
            }
    }

    fun getTransactionsForCurrentUser(onData: (List<Transaction>) -> Unit) {

        val userId = auth.currentUser?.uid ?: return

        db.collection("transaction")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                onData(snapshot.toObjects(Transaction::class.java))
            }
    }

    fun updateTransaction(transaction: Transaction, onComplete: (Boolean) -> Unit) {
        val db = Firebase.firestore
        db.collection("transaction")
            .document(transaction.id!!)
            .set(transaction)
            .addOnSuccessListener { onComplete(true) }
         .addOnFailureListener { e ->
            Log.e("TransactionService", "Error updating transaction: ${e.message}", e)
            onComplete(false)
         }
    }

    fun getTransactionsByDateRange(
        userId: String,
        startDate: Timestamp,
        endDate: Timestamp,
        onData: (List<Transaction>) -> Unit
    ) {
        if (userId.isBlank()) {
            Log.w("TransactionService", "getTransactionsByDateRange: Provided userId is blank. Returning empty list.")
            onData(emptyList())
            return
        }
        if (startDate.seconds > endDate.seconds) {
            Log.e("TransactionService", "getTransactionsByDateRange: Start date is after end date. Returning empty list.")
            onData(emptyList())
            return
        }

        db.collection("transaction")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val transactions = querySnapshot.toObjects(Transaction::class.java)
                Log.d("TransactionService", "Fetched ${transactions.size} transactions for user $userId between $startDate and $endDate.")
                onData(transactions)
            }
            .addOnFailureListener { e ->
                Log.e("TransactionService", "Error fetching transactions by date range for user $userId: ${e.message}", e)
                onData(emptyList())
            }
    }

    fun deleteTransaction(transactionId: String, onComplete: (Boolean) -> Unit) {
        db.collection("transaction")
            .document(transactionId)
            .delete()
            .addOnSuccessListener {
                Log.d("TransactionService", "Transaction with ID $transactionId successfully deleted!")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("TransactionService", "Error deleting transaction with ID $transactionId: ${e.message}", e)
                onComplete(false)
            }
    }
}
