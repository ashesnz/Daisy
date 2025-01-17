package com.daisydev.daisy.ui.feature.session

import androidx.lifecycle.LiveData
import android.util.Patterns
import androidx.compose.material3.SnackbarHostState
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
 * ViewModel for the registration screen
 * Contains the business logic for the screen
 * @property appWriteRepository AppWriteRepository
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val appWriteRepository: AppWriteRepository,
    private val sessionDataStore: SessionDataStore
) :
    ViewModel() {

    private val _user = MutableLiveData<String>()
    val user: LiveData<String> = _user

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _ConditionsChecked = MutableLiveData<Boolean>()
    val conditionsChecked: LiveData<Boolean> = _ConditionsChecked

    private val _registerEnable = MutableLiveData<Boolean>()
    val registerEnable: LiveData<Boolean> = _registerEnable

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    private val _showError = MutableLiveData<Boolean>()
    val showError: LiveData<Boolean> = _showError

    // Updates the value of user, email, and password and enables the register button if they are valid
    fun onRegisterChanged(
        user: String,
        email: String,
        password: String,
        conditionsChecked: Boolean
    ) {
        _user.value = user
        _email.value = email
        _password.value = password
        _ConditionsChecked.value = conditionsChecked
        _registerEnable.value =
            isValidUser(user) && isValidEmail(email) && isValidPassword(password) && conditionsChecked
    }

    // Validates that the user has at least 3 characters
    private fun isValidUser(user: String): Boolean = user.length >= 3

    // Validates that the password has at least 8 characters
    private fun isValidPassword(password: String): Boolean = password.length >= 8

    // Validates that the email has the correct format
    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // Performs the registration
    fun onRegisterSelected() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Register user
                val userData = appWriteRepository.register(
                    password = password.value!!,
                    email = email.value!!,
                    name = user.value!!
                )

                // Login user
                appWriteRepository.login(email.value!!, password.value!!)

                // Save session
                sessionDataStore.saveSession(
                    Session(
                        id = userData.id,
                        name = userData.name,
                        email = userData.email
                    )
                )

                _registerSuccess.value = true
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
                .showSnackbar("Error creating account, please try again")
            _showError.value = false
        }
    }
}