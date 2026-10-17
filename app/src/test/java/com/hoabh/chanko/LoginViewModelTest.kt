package com.hoabh.chanko

import com.hoabh.chanko.presentation.screens.login.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel()
    }

    @Test
    fun initialState_isEmpty() {
        val state = viewModel.uiState.value
        assertEquals("", state.account)
        assertEquals("", state.password)
        assertFalse(state.isPasswordVisible)
        assertFalse(state.isLoginSuccess)
        assertNull(state.accountError)
        assertNull(state.passwordError)
        assertNull(state.generalError)
    }

    @Test
    fun onAccountChange_updatesStateAndClearsErrors() {
        viewModel.loginWithCredentials() // Triggers error
        assertNotNull(viewModel.uiState.value.generalError)

        viewModel.onAccountChange("testuser@example.com")
        val state = viewModel.uiState.value
        assertEquals("testuser@example.com", state.account)
        assertNull(state.accountError)
        assertNull(state.generalError)
    }

    @Test
    fun onPasswordChange_updatesStateAndClearsErrors() {
        viewModel.loginWithCredentials() // Triggers error
        assertNotNull(viewModel.uiState.value.generalError)

        viewModel.onPasswordChange("secret123")
        val state = viewModel.uiState.value
        assertEquals("secret123", state.password)
        assertNull(state.passwordError)
        assertNull(state.generalError)
    }

    @Test
    fun togglePasswordVisibility_switchesBoolean() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun loginWithCredentials_emptyAccount_fails() {
        viewModel.onPasswordChange("password123")
        viewModel.loginWithCredentials()

        val state = viewModel.uiState.value
        assertFalse(state.isLoginSuccess)
        assertNotNull(state.accountError)
        assertNull(state.passwordError)
        assertNotNull(state.generalError)
    }

    @Test
    fun loginWithCredentials_emptyPassword_fails() {
        viewModel.onAccountChange("user@example.com")
        viewModel.loginWithCredentials()

        val state = viewModel.uiState.value
        assertFalse(state.isLoginSuccess)
        assertNull(state.accountError)
        assertNotNull(state.passwordError)
        assertNotNull(state.generalError)
    }

    @Test
    fun loginWithCredentials_blankSpaces_fails() {
        viewModel.onAccountChange("   ")
        viewModel.onPasswordChange("   ")
        viewModel.loginWithCredentials()

        val state = viewModel.uiState.value
        assertFalse(state.isLoginSuccess)
        assertNotNull(state.accountError)
        assertNotNull(state.passwordError)
    }

    @Test
    fun loginWithCredentials_validInput_succeeds() {
        viewModel.onAccountChange("user@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.loginWithCredentials()

        val state = viewModel.uiState.value
        assertTrue(state.isLoginSuccess)
        assertNull(state.accountError)
        assertNull(state.passwordError)
        assertNull(state.generalError)
    }

    @Test
    fun loginWithGoogle_succeeds() {
        viewModel.loginWithGoogle()

        val state = viewModel.uiState.value
        assertTrue(state.isLoginSuccess)
        assertNull(state.generalError)
    }

    @Test
    fun consumeLoginSuccess_resetsFlag() {
        viewModel.loginWithGoogle()
        assertTrue(viewModel.uiState.value.isLoginSuccess)

        viewModel.consumeLoginSuccess()
        assertFalse(viewModel.uiState.value.isLoginSuccess)
    }
}
