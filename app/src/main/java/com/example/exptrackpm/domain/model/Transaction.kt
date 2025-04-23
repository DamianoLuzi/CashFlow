import com.google.firebase.Timestamp

enum class TransactionType { INCOME, EXPENSE }

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val category: String = "",
    val date: Timestamp = Timestamp.now(),
    val receiptUrl: String? = null,
    val type: TransactionType = TransactionType.EXPENSE
)
