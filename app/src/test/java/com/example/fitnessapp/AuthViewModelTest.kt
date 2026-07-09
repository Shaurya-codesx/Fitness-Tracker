package com.example.fitnessapp

import app.cash.turbine.test
import com.example.fitnessapp.Domain.AuthRepository
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.ui.Auth.AuthViewModel
import com.example.fitnessapp.ui.utils.CloudSyncManager
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    // 1. We mock (fake) the repositories.
    // We don't want to actually hit Firebase during a fast unit test!
    private val mockAuthRepo = mockk<AuthRepository>(relaxed = true)
    private val mockRunRepo = mockk<RunRepository>(relaxed = true)
    private val mockCloudSyncManager = mockk<CloudSyncManager>(relaxed = true)

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // This runs before EVERY test. It sets up a fake "Main" thread for coroutines
        // and initializes a fresh ViewModel.
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(mockAuthRepo, mockRunRepo, mockCloudSyncManager)
    }

    @After
    fun tearDown() {
        // Cleans up the fake thread after the test is done
        Dispatchers.resetMain()
    }

    @Test
    fun `when user types email, uiState updates correctly`() = runTest {
        // GIVEN: A fresh AuthViewModel (created in setup)
        val expectedEmail = "test@runner.com"

        // WHEN: The user types an email
        viewModel.onEmailChange(expectedEmail)

        // THEN: We use Turbine (.test) to catch the StateFlow emission and verify it
        viewModel.uiState.test {
            val currentState = awaitItem() // Grabs the current state

            assertEquals(expectedEmail, currentState.emailInput) // Did it update?
            assertEquals(null, currentState.errorMessage) // Did it clear errors?

            cancelAndIgnoreRemainingEvents() // Stop listening
        }
    }
    @Test
    fun `when submit clicked with empty password, error message is shown`() = runTest {
        // GIVEN: The user has typed an email, but NO password.
        viewModel.onEmailChange("test@runner.com")
        viewModel.onPasswordChange("") // Empty password

        // WHEN: The user clicks the Submit button
        viewModel.submit()

        // THEN: We use Turbine to inspect the StateFlow conveyor belt
        viewModel.uiState.test {
            val currentState = awaitItem() // Grab the latest state off the belt

            // Assert that the ViewModel correctly caught the error
            assertEquals("Fields cannot be empty", currentState.errorMessage)

            // Assert that the loading spinner did NOT start
            assertEquals(false, currentState.isLoading)

            cancelAndIgnoreRemainingEvents() // Tell Turbine we are done testing
        }
    }

    @Test
    fun `when firebase login fails, uiState shows error message`() = runTest {
        // GIVEN: The user types a valid email and password
        val email = "test@runner.com"
        val password = "wrongpassword"

        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        // --- THE SCRIPT (STUBBING) ---
        // 'coEvery' stands for Coroutine Every.
        // It tells MockK: "Every time the ViewModel calls the signIn function with these exact details..."
        io.mockk.coEvery {
            mockAuthRepo.signIn(email, password)
        } returns Result.failure(Exception("Firebase Network Error: Invalid Credentials"))
        // "...instantly return a Failure result with this exact error message."

        // WHEN: The user clicks the submit button
        viewModel.submit()

        // THEN: We inspect the UI State conveyor belt
        viewModel.uiState.test {
            skipItems(1)
            val currentState = awaitItem()

            // 1. We expect the loading spinner to stop
            assertEquals(false, currentState.isLoading)

            // 2. We expect the ViewModel to catch the Firebase error and show it to the user
            assertEquals("Firebase Network Error: Invalid Credentials", currentState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }
}