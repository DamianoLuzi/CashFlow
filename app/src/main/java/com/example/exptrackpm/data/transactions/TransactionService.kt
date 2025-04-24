
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.InputStream


object TransactionService {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val storage = Firebase.storage

    fun uploadFile(
        fileUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Create a reference to the "receipts" folder in Firebase Storage and generate a unique file name
        val userId = Firebase.auth.currentUser?.uid ?: return
        val fileName = "receipts/$userId/${System.currentTimeMillis()}"
        val storageRef: StorageReference = storage.reference.child(fileName)

        // Upload the file to Firebase Storage using the putFile() method
        val uploadTask: UploadTask = storageRef.putFile(fileUri)

        // Register listeners to handle success and failure of the upload
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // When upload succeeds, retrieve the file's download URL
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUrl ->
                // Send the download URL to the onSuccess callback
                onSuccess(downloadUrl.toString())
            }?.addOnFailureListener { error ->
                // Handle failure when trying to retrieve the download URL
                onFailure(error)
            }
        }.addOnFailureListener { exception ->
            // Handle failure of the upload itself
            onFailure(exception)
        }
    }


    fun uploadFileViaInputStream(
        inputStream: InputStream,
        fileName: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageReference = storage.reference.child("uploads/$fileName")
        storageReference.putStream(inputStream)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onSuccess(downloadUrl.toString())
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }




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
