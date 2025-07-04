package com.example.exptrackpm

import TransactionType
import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.exptrackpm.ui.screens.transactions.TransactionViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class TransactionViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: TransactionViewModel

    @MockK(relaxed = true)
    lateinit var mockFirebaseAuth: FirebaseAuth

    @MockK(relaxed = true)
    lateinit var mockFirebaseUser: FirebaseUser

    @MockK(relaxed = true)
    lateinit var mockFirestore: FirebaseFirestore

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        val context = ApplicationProvider.getApplicationContext<Application>()
        mockkStatic(FirebaseApp::class)
        val mockApp = mockk<FirebaseApp>(relaxed = true)
        every { FirebaseApp.initializeApp(any<Application>()) } returns mockApp
        every { FirebaseApp.getInstance() } returns mockApp
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
        mockkStatic("com.google.firebase.firestore.ktx.FirestoreKt")
        every { Firebase.firestore } returns mockFirestore
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        mockkStatic("com.google.firebase.auth.ktx.AuthKt")
        every { Firebase.auth } returns mockFirebaseAuth

        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "test_user_id"
        viewModel = TransactionViewModel(context)
    }



    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `addTransaction triggers loading and state update`() {
        viewModel.addTransaction(100.0, "Test Desc", "Category", TransactionType.EXPENSE)
        val transactions = viewModel.transactions.value
        assert(transactions != null)
    }

    @Test
    fun `setFilter updates filter state`() {
        viewModel.setFilter(TransactionType.EXPENSE)
        assert(viewModel.filter.value == TransactionType.EXPENSE)

        viewModel.setFilter(null)
        assert(viewModel.filter.value == null)
    }

    @Test
    fun `addTransaction returns early if currentUser is null`() {
        every { mockFirebaseAuth.currentUser } returns null

        viewModel.addTransaction(50.0, "desc", "cat", TransactionType.INCOME)
        assert(viewModel.transactions.value.isEmpty())
    }

    @Test
    fun `updateTransaction returns early if transaction id not found`() {
        viewModel.updateTransaction("nonexistent", 20.0, "desc", "cat", null, Timestamp.now())
        assert(viewModel.transactions.value.isEmpty())
    }



}
