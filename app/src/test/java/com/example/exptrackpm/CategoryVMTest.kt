package com.example.exptrackpm.ui.screens.categories

// Add RobolectricTestRunner here
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.exptrackpm.data.categories.CategoryService
import com.example.exptrackpm.domain.model.Category
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class CategoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: CategoryViewModel

    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser
    // Add mock for FirebaseFirestore as CategoryService initializes it
    private lateinit var mockFirebaseFirestore: FirebaseFirestore

    private val fakeUserId = "test_user_id"
    private val initialFakeCategories = listOf(
        Category( userId = fakeUserId, name = "Food", icon = "food_icon"),
        Category( userId = fakeUserId, name = "Shopping", icon = "shopping_icon")
    )
    private val newCategoryName = "Travel"
    private val newCategoryIcon = "travel_icon"

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        Dispatchers.setMain(testDispatcher)

        // Mock Firebase SDK initialization and instances
        mockkStatic(Firebase::class)
        mockkStatic("com.google.firebase.auth.ktx.AuthKt")
        mockkStatic(FirebaseApp::class) // Mock FirebaseApp for getInstance calls
        mockkStatic("com.google.firebase.firestore.ktx.FirestoreKt") // Mock Firestore KTX extension

        mockFirebaseAuth = mockk(relaxed = true)
        mockFirebaseUser = mockk(relaxed = true)
        mockFirebaseFirestore = mockk(relaxed = true) // Initialize mock Firestore

        // Setup Firebase mock behavior
        every { FirebaseApp.initializeApp(any()) } returns mockk() // Stub for any app initialization
        every { FirebaseApp.getInstance() } returns mockk() // Stub for getting default app instance
        every { Firebase.auth } returns mockFirebaseAuth
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns fakeUserId
        // Ensure Firebase.firestore returns our mockFirestore
        every { Firebase.firestore } returns mockFirebaseFirestore


        // Mock the CategoryService object AFTER setting up its Firebase dependencies
        // This is crucial because CategoryService initializes db and auth fields statically.
        mockkObject(CategoryService)

        // Set up initial mock behavior for CategoryService *before* ViewModel init
        every { CategoryService.getUserCategories(any()) } answers {
            val callback = arg<(List<Category>) -> Unit>(0)
            callback.invoke(initialFakeCategories)
        }

        // Initialize the ViewModel
        viewModel = CategoryViewModel()

        testDispatcher.scheduler.runCurrent()
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `ViewModel initializes and loads categories correctly`() = runTest {
        assertEquals(initialFakeCategories, viewModel.categories.value)
        assertEquals("", viewModel.name)
        assertNull(viewModel.icon)
        assertNull(viewModel.color)
        assertNull(viewModel.error)

        verify(exactly = 1) { CategoryService.getUserCategories(any()) }
    }

    @Test
    fun `loadCategories updates categories with new data`() = runTest {
        val updatedCategories = listOf(
            initialFakeCategories[0],
            initialFakeCategories[1],
            Category( userId = fakeUserId, name = "New", icon = "new_icon")
        )

        every { CategoryService.getUserCategories(any()) } answers {
            val callback = arg<(List<Category>) -> Unit>(0)
            callback.invoke(updatedCategories)
        }

        viewModel.loadCategories()

        testDispatcher.scheduler.runCurrent()

        assertEquals(updatedCategories, viewModel.categories.value)
        verify(exactly = 2) { CategoryService.getUserCategories(any()) }
    }

    @Test
    fun `addCategory adds a new category and reloads categories on success`() = runTest {
        every {
            CategoryService.addCategory(any(), any(), any())
        } answers {
            val successCallback = arg<() -> Unit>(1)
            successCallback.invoke()
        }

        val categoriesAfterAdd = initialFakeCategories + Category(
            userId = fakeUserId,
            name = newCategoryName,
            icon = newCategoryIcon
        )
        every { CategoryService.getUserCategories(any()) } answers {
            val callback = arg<(List<Category>) -> Unit>(0)
            callback.invoke(categoriesAfterAdd)
        }

        viewModel.addCategory(newCategoryName, newCategoryIcon)

        testDispatcher.scheduler.runCurrent()

        assertEquals(categoriesAfterAdd, viewModel.categories.value)
        assertNull(viewModel.error)

        verify(exactly = 1) {
            CategoryService.addCategory(
                match { it.userId == fakeUserId && it.name == newCategoryName && it.icon == newCategoryIcon },
                any(),
                any()
            )
        }
        verify(exactly = 2) { CategoryService.getUserCategories(any()) }
    }

    @Test
    fun `addCategory sets error if name is blank`() = runTest {
        viewModel.addCategory("", newCategoryIcon)

        assertEquals("Category name can't be blank!", viewModel.error)
        assertEquals(initialFakeCategories, viewModel.categories.value)

        verify(exactly = 0) { CategoryService.addCategory(any(), any(), any()) }
        verify(exactly = 1) { CategoryService.getUserCategories(any()) }
    }

    @Test
    fun `addCategory sets error on CategoryService failure`() = runTest {
        val errorMessage = "Database write failed"
        every {
            CategoryService.addCategory(any(), any(), any())
        } answers {
            val failureCallback = arg<(Throwable) -> Unit>(2)
            failureCallback.invoke(Exception(errorMessage))
        }

        every { CategoryService.getUserCategories(any()) } throws IllegalStateException("getUserCategories should not be called again on add failure")


        viewModel.addCategory(newCategoryName, newCategoryIcon)

        testDispatcher.scheduler.runCurrent()

        assertEquals(errorMessage, viewModel.error)
        assertEquals(initialFakeCategories, viewModel.categories.value)

        verify(exactly = 1) { CategoryService.addCategory(any(), any(), any()) }
        verify(exactly = 1) { CategoryService.getUserCategories(any()) }
    }

    @Test
    fun `addCategory returns early if currentUser uid is null`() = runTest {
        every { mockFirebaseAuth.currentUser } returns null

        viewModel.addCategory(newCategoryName, newCategoryIcon)

        assertNull(viewModel.error)
        assertEquals(initialFakeCategories, viewModel.categories.value)

        verify(exactly = 0) { CategoryService.addCategory(any(), any(), any()) }
        verify(exactly = 1) { CategoryService.getUserCategories(any()) }
    }
}