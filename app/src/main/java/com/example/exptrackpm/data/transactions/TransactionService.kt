
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
    }
}
