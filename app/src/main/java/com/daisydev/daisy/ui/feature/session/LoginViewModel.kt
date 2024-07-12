package com.daisydev.daisy.ui.feature.session

import android.util.Patterns
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daisydev.daisy.models.Session
import com.daisydev.daisy.repository.local.SessionDataStore
import com.daisydev.daisy.repository.remote.AppWriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the login screen
 * Contains the business logic for the screen
 * @property appWriteRepository AppWriteRepository
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val appWriteRepository: AppWriteRepository,
    private val sessionDataStore: SessionDataStore
) :
    ViewModel() {
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _loginEnable = MutableLiveData<Boolean>()
    val loginEnable: LiveData<Boolean> = _loginEnable

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _showError = MutableLiveData<Boolean>()
    val showError: LiveData<Boolean> = _showError

    // Updates the value of email and password and enables the login button if they are valid
    fun onLoginChanged(email: String, password: String) {
        _email.value = email
        _password.value = password
        _loginEnable.value = isValidEmail(email) && isValidPassword(password)
    }

    // Validates that the password has at least 8 characters
    private fun isValidPassword(password: String): Boolean = password.length >= 8

    // Validates that the email has the correct format
    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // Performs the login
    fun onLoginSelected() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Login
                val result = appWriteRepository.login(email.value!!, password.value!!)

                // Get user data
                val user = appWriteRepository.getAccount()

                // Save session
                sessionDataStore.saveSession(
                    Session(
                        id = user.id,
                        name = user.name,
                        email = user.email
                    )
                )

                _loginSuccess.value = true
            } catch (e: Exception) {
                _showError.value = true
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Shows the error snackbar
    fun showSnackbar(snackbarHostState: SnackbarHostState) {
        viewModelScope.launch {
            snackbarHostState
                .showSnackbar("Incorrect credentials, please try again")
            _showError.value = false
        }
    }
}