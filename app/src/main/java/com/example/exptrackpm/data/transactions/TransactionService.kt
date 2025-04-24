
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


object TransactionService {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    fun addTransaction(transaction: Transaction, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return

        val txn = Transaction(
            id = "", //Firestore autogenerates it
            amount = transaction.amount,
            description = transaction.description,
            category = transaction.category,
            type = transaction.type,
            date = Timestamp.now(),
            receiptUrl = transaction.receiptUrl
        )

        db.collection("expense")
            .add(txn)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getTransactionsForCurrentUser(onData: (List<Transaction>) -> Unit) {

        val userId = auth.currentUser?.uid ?: return
        Log.d("trans",userId )

        db.collection("expense")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                onData(snapshot.toObjects(Transaction::class.java))
            }
    }
}
