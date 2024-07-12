package com.daisydev.daisy.ui.feature.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daisydev.daisy.repository.local.SessionDataStore
import com.daisydev.daisy.repository.remote.AppWriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.daisydev.daisy.models.Session
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the session screen
 * Contains the business logic for the screen
 * @property appWriteRepository AppWriteRepository
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val appWriteRepository: AppWriteRepository,
    private val sessionDataStore: SessionDataStore
) :
    ViewModel() {

    private val _userData = MutableLiveData<Session>()
    val userData: LiveData<Session> = _userData

    private val _isUserLogged = MutableLiveData<Boolean>()
    val isUserLogged: LiveData<Boolean> = _isUserLogged

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Checks if the user is logged in
    fun isLogged() {
        viewModelScope.launch {
            val userData = sessionDataStore.getSession()

            // If the user's id is not empty, then they are logged in
            if (userData.id.isNotEmpty()) {
                _userData.value = userData
                _isUserLogged.value = true
            } else {
                _isUserLogged.value = false
            }

            _isLoading.value = false
        }
    }

    // Closes the user's session
    fun closeSession() {
        viewModelScope.launch {
            try {
                appWriteRepository.logout()
                sessionDataStore.clearSession()
                _isUserLogged.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}