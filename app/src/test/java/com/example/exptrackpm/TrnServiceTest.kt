package com.example.exptrackpm.ui.screens.transactions


import Transaction
import TransactionService
import TransactionType
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.temporal.ChronoUnit


@RunWith(RobolectricTestRunner::class)
class TransactionServiceTest {
    @MockK(relaxed = true)
    lateinit var mockFirestore: FirebaseFirestore

    @MockK(relaxed = true)
    lateinit var mockFirebaseAuth: FirebaseAuth

    @MockK(relaxed = true)
    lateinit var mockCurrentUser: FirebaseUser

    @MockK(relaxed = true)
    lateinit var mockFirebaseApp: FirebaseApp

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(FirebaseApp::class)
        every { FirebaseApp.initializeApp(any(), any()) } returns mockFirebaseApp
        every { FirebaseApp.initializeApp(any()) } returns mockFirebaseApp
        every { FirebaseApp.getInstance() } returns mockFirebaseApp
        every { FirebaseApp.getInstance(any()) } returns mockFirebaseApp
        mockkStatic(Firebase::class)
        every { Firebase.firestore } returns mockFirestore
        every { Firebase.auth } returns mockFirebaseAuth

        every { mockFirebaseAuth.currentUser } returns mockCurrentUser
        every { mockCurrentUser.uid } returns "test_user_id"
    }


    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun <T> mockTask(result: T? = null, exception: Exception? = null): Task<T> {
        val task: Task<T> = mockk(relaxed = true)
        every { task.addOnSuccessListener(any<OnSuccessListener<T>>()) } answers {
            val listener = arg<OnSuccessListener<T>>(0)
            if (exception == null) {
                listener.onSuccess(result)
            }
            task
        }

        every { task.addOnFailureListener(any<OnFailureListener>()) } answers {
            val listener = arg<OnFailureListener>(0)
            if (exception != null) {
                listener.onFailure(exception)
            }
            task
        }
        every { task.addOnCompleteListener(any<OnCompleteListener<T>>()) } answers {
            val listener = arg<OnCompleteListener<T>>(0)
            listener.onComplete(task)
            task
        }

        every { task.isSuccessful } answers { exception == null }
        every { task.exception } answers { exception }
        every { task.result } answers { result }
        every { task.isComplete } returns true

        return task
    }


    @Test
    fun `addTransaction should add transaction to Firestore and update id on success`() {
        val testTransaction = Transaction(
            amount = 50.0,
            description = "Coffee",
            category = "Food",
            type = TransactionType.EXPENSE,
            receiptUrl = null
        )

        val mockCollectionRef = mockk<CollectionReference>()
        val mockDocRef = mockk<DocumentReference>()


        every { mockFirestore.collection("transaction") } returns mockCollectionRef
        every { mockCollectionRef.add(any<Transaction>()) } returns mockTask(mockDocRef)
        every { mockDocRef.id } returns "new_transaction_id"
        every { mockDocRef.update("id", "new_transaction_id") } returns mockTask(null)

        var callbackSuccess: Boolean? = null
        TransactionService.addTransaction(testTransaction) { success ->
            callbackSuccess = success
        }
        verify(exactly = 1) { mockFirestore.collection("transaction") }
        verify(exactly = 1) { mockCollectionRef.add(any<Transaction>()) }
        verify(exactly = 1) { mockDocRef.update("id", "new_transaction_id") }
        assert(callbackSuccess == true) { "Callback should indicate success" }
    }


    @Test
    fun `addTransaction should not proceed if userId is null`() {
        every { mockFirebaseAuth.currentUser } returns null

        val testTransaction = Transaction(
            amount = 50.0,
            description = "Coffee",
            category = "Food",
            type = TransactionType.EXPENSE,
            receiptUrl = null
        )
        val mockCollectionRef = mockk<CollectionReference>(relaxed = true)
        every { mockFirestore.collection("transaction") } returns mockCollectionRef
        TransactionService.addTransaction(testTransaction)
        verify(exactly = 0) { mockFirestore.collection(any<String>()) }
    }


    @Test
    fun `updateTransaction should update transaction in Firestore`() {
        val existingTransaction = Transaction(
            id = "existing_id",
            userId = "test_user_id",
            amount = 100.0,
            description = "Old Desc",
            category = "Old Cat",
            type = TransactionType.EXPENSE,
            date = Timestamp.now()
        )
        val updatedTransaction = existingTransaction.copy(
            amount = 120.0,
            description = "New Desc",
            category = "New Cat"
        )
        val mockDocRef = mockk<DocumentReference>()
        every { mockFirestore.collection("transaction").document("existing_id") } returns mockDocRef
        every { mockDocRef.set(updatedTransaction) } returns mockTask(null)

        var callbackSuccess: Boolean? = null
        TransactionService.updateTransaction(updatedTransaction) { success ->
            callbackSuccess = success
        }
        verify(exactly = 1) { mockFirestore.collection("transaction").document("existing_id") }
        verify(exactly = 1) { mockDocRef.set(updatedTransaction) }
        assert(callbackSuccess == true) { "Callback should indicate success" }
    }

    @Test
    fun `updateTransaction should call onComplete with false on failure`() {

        val existingTransaction = Transaction(
            id = "existing_id",
            userId = "test_user_id",
            amount = 100.0,
            description = "Old Desc",
            category = "Old Cat",
            type = TransactionType.EXPENSE,
            date = Timestamp.now()
        )
        val updatedTransaction = existingTransaction.copy(amount = 120.0)
        val mockDocRef = mockk<DocumentReference>()
        val exception = Exception("Firestore update failed")

        every { mockFirestore.collection("transaction").document("existing_id") } returns mockDocRef
        every { mockDocRef.set(updatedTransaction) } returns mockTask(exception = exception)

        var callbackSuccess: Boolean? = null
        TransactionService.updateTransaction(updatedTransaction) { success ->
            callbackSuccess = success
        }
        verify(exactly = 1) { mockFirestore.collection("transaction").document("existing_id") }
        verify(exactly = 1) { mockDocRef.set(updatedTransaction) }
        assert(callbackSuccess == false) { "Callback should indicate failure" }
    }



    @Test
    fun `getTransactionsByDateRange should return transactions within range`() {
        val userId = "test_user_id"
        val startDate = Timestamp(Instant.now().minus(7, ChronoUnit.DAYS).epochSecond, 0)
        val endDate = Timestamp(Instant.now().epochSecond, 0)

        val mockQuerySnapshot = mockk<QuerySnapshot>()
        val expectedTransactions = listOf(
            Transaction(id = "t1", userId = userId, amount = 10.0, date = startDate),
            Transaction(id = "t2", userId = userId, amount = 20.0, date = endDate)
        )
        every {
            mockFirestore.collection("transaction")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
        } returns mockTask(mockQuerySnapshot)

        every { mockQuerySnapshot.toObjects(Transaction::class.java) } returns expectedTransactions

        var capturedTransactions: List<Transaction>? = null
        TransactionService.getTransactionsByDateRange(userId, startDate, endDate) { transactions ->
            capturedTransactions = transactions
        }
        verify(exactly = 1) {
            mockFirestore.collection("transaction")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
        }
        assert(capturedTransactions == expectedTransactions) { "Transactions returned should match expected" }
    }


    @Test
    fun `getTransactionsByDateRange should return empty list if userId is blank`() {
        val userId = ""
        val startDate = Timestamp.now()
        val endDate = Timestamp.now()

        var capturedTransactions: List<Transaction>? = null
        TransactionService.getTransactionsByDateRange(userId, startDate, endDate) { transactions ->
            capturedTransactions = transactions
        }
        verify(exactly = 0) { mockFirestore.collection(any<String>()) }
        assert(capturedTransactions == emptyList<Transaction>()) { "Should return empty list for blank userId" }
    }

    @Test
    fun `getTransactionsByDateRange should return empty list if startDate is after endDate`() {
        val userId = "test_user_id"
        val startDate = Timestamp(Instant.now().epochSecond, 0)
        val endDate = Timestamp(Instant.now().minus(1, ChronoUnit.DAYS).epochSecond, 0)

        var capturedTransactions: List<Transaction>? = null
        TransactionService.getTransactionsByDateRange(userId, startDate, endDate) { transactions ->
            capturedTransactions = transactions
        }
        verify(exactly = 0) { mockFirestore.collection(any<String>()) }
        assert(capturedTransactions == emptyList<Transaction>()) { "Should return empty list for invalid date range" }
    }

    @Test
    fun `getTransactionsByDateRange should return empty list on failure`() {
        val userId = "test_user_id"
        val startDate = Timestamp(Instant.now().minus(7, ChronoUnit.DAYS).epochSecond, 0)
        val endDate = Timestamp(Instant.now().epochSecond, 0)

        val mockQuery = mockk<Query>()
        val exception = Exception("Firestore fetch error")
        every { mockFirestore.collection("transaction") } returns mockk {
            every { whereEqualTo("userId", userId) } returns mockQuery
        }
        every { mockQuery.whereGreaterThanOrEqualTo("date", startDate) } returns mockQuery
        every { mockQuery.whereLessThanOrEqualTo("date", endDate) } returns mockQuery
        every { mockQuery.get() } returns mockTask(exception = exception)

        var capturedTransactions: List<Transaction>? = null
        TransactionService.getTransactionsByDateRange(userId, startDate, endDate) { transactions ->
            capturedTransactions = transactions
        }
        verify(exactly = 1) { mockQuery.get() }
        assert(capturedTransactions == emptyList<Transaction>()) { "Should return empty list on Firestore failure" }
    }


    @Test
    fun `getTransactionsForCurrentUser should return transactions from snapshot listener`() {
        val userId = "test_user_id"
        val mockQuerySnapshot = mockk<QuerySnapshot>()
        val expectedTransactions = listOf(
            Transaction(id = "t1", userId = userId, amount = 10.0, category = "Food"),
            Transaction(id = "t2", userId = userId, amount = 20.0, category = "Travel")
        )
        val snapshotListenerSlot = slot<EventListener<QuerySnapshot>>()
        every {
            mockFirestore.collection("transaction")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(capture(snapshotListenerSlot))
        } returns mockk()
        var capturedTransactions: List<Transaction>? = null
        TransactionService.getTransactionsForCurrentUser { transactions ->
            capturedTransactions = transactions
        }
        every { mockQuerySnapshot.toObjects(Transaction::class.java) } returns expectedTransactions
        snapshotListenerSlot.captured.onEvent(mockQuerySnapshot, null)
        verify(exactly = 1) { mockFirestore.collection("transaction") }
        verify(exactly = 1) { mockFirestore.collection("transaction").whereEqualTo("userId", userId) }
        verify(exactly = 1) { mockFirestore.collection("transaction").whereEqualTo("userId", userId).addSnapshotListener(any()) }
        assert(capturedTransactions == expectedTransactions) { "Transactions from snapshot should match expected" }
    }
    @Test
    fun `getTransactionsForCurrentUser should not call callback on snapshot error`() {
        val userId = "test_user_id"
        val mockException = mockk<FirebaseFirestoreException>()

        val snapshotListenerSlot = slot<EventListener<QuerySnapshot>>()

        every {
            mockFirestore.collection("transaction")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(capture(snapshotListenerSlot))
        } returns mockk()

        var callbackCalled = false
        var capturedTransactions: List<Transaction>? = null
        TransactionService.getTransactionsForCurrentUser { transactions ->
            capturedTransactions = transactions
            callbackCalled = true
        }

        snapshotListenerSlot.captured.onEvent(null, mockException)

        verify(exactly = 1) {
            mockFirestore.collection("transaction")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(any())
        }
        assert(!callbackCalled) { "Callback should NOT be called on snapshot error" }
        assert(capturedTransactions == null) { "Captured transactions should remain null if callback isn't called" }
    }
}