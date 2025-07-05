package com.example.exptrackpm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.exptrackpm.data.users.UserRepository
import com.example.exptrackpm.domain.model.Budget
import com.example.exptrackpm.ui.screens.budgets.BudgetViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BudgetViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: BudgetViewModel

    private val fakeUserId = "testUser123"
    private val fakeBudgetsList = listOf(
        Budget(id = "b1", userId = fakeUserId, category = "Food", amount = 300.0),
        Budget(id = "b2", userId = fakeUserId, category = "Transport", amount = 100.0)
    )
    private val newBudget = Budget(id = "b3", userId = fakeUserId, category = "Rent", amount = 1000.0)


    @Before
    fun setup() {
        MockKAnnotations.init(this)

        Dispatchers.setMain(testDispatcher)
        mockkObject(UserRepository)
        viewModel = BudgetViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }
    @Test
    fun `loadBudgets sets loading to true then false and updates budgets on success`() = runTest {
        every {
            UserRepository.getBudgets(any())
        } answers {
            val callback = arg<(List<Budget>) -> Unit>(0)
            callback.invoke(fakeBudgetsList)
        }

        viewModel.loadBudgets(fakeUserId)
        testDispatcher.scheduler.runCurrent()
        assertEquals(fakeBudgetsList, viewModel.budgets.value)
        assertFalse(viewModel.loading.value)

        verify(exactly = 1) { UserRepository.getBudgets(any()) }
    }

    @Test
    fun `loadBudgets sets loading to true then false even if no budgets returned`() = runTest {
        every {
            UserRepository.getBudgets(any())
        } answers {
            val callback = arg<(List<Budget>) -> Unit>(0)
            callback.invoke(emptyList())
        }

        viewModel.loadBudgets(fakeUserId)

        testDispatcher.scheduler.runCurrent()
        assertTrue(viewModel.budgets.value.isEmpty())
        assertFalse(viewModel.loading.value)

        verify(exactly = 1) { UserRepository.getBudgets(any()) }
    }

    @Test
    fun `saveBudget sets loading to true then false and reloads budgets on success`() = runTest {
        every {
            UserRepository.saveBudget(newBudget, any())
        } answers {
            val callback = arg<(Boolean) -> Unit>(1)
            callback.invoke(true)
        }
        every {
            UserRepository.getBudgets(any())
        } answers {
            val callback = arg<(List<Budget>) -> Unit>(0)
            callback.invoke(fakeBudgetsList + newBudget)
        }

        viewModel.saveBudget(newBudget)

        testDispatcher.scheduler.runCurrent()

        assertEquals(fakeBudgetsList + newBudget, viewModel.budgets.value)
        assertFalse(viewModel.loading.value)

        verify(exactly = 1) { UserRepository.saveBudget(newBudget, any()) }

        verify(exactly = 1) { UserRepository.getBudgets(any()) }
    }

    @Test
    fun `saveBudget sets loading to true then false and does not reload budgets on failure`() = runTest {
        every {
            UserRepository.getBudgets(any())
        } answers {
            val callback = arg<(List<Budget>) -> Unit>(0)
            callback.invoke(fakeBudgetsList)
        }
        viewModel.loadBudgets(fakeUserId)
        testDispatcher.scheduler.runCurrent()
        every {
            UserRepository.saveBudget(newBudget, any())
        } answers {
            val callback = arg<(Boolean) -> Unit>(1)
            callback.invoke(false)
        }
        every {
            UserRepository.getBudgets(any())
        } throws IllegalStateException("loadBudgets should not be called again on save failure")

        viewModel.saveBudget(newBudget)

        testDispatcher.scheduler.runCurrent()

        assertEquals(fakeBudgetsList, viewModel.budgets.value)
        assertFalse(viewModel.loading.value)

        verify(exactly = 1) { UserRepository.saveBudget(newBudget, any()) }
        verify(exactly = 1) { UserRepository.getBudgets(any()) }
    }
}