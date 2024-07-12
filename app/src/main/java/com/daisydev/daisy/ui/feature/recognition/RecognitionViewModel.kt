package com.daisydev.daisy.ui.feature.recognition

import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daisydev.daisy.models.DataPlant
import com.daisydev.daisy.repository.local.SessionDataStore
import com.daisydev.daisy.repository.remote.AppWriteRepository
import com.daisydev.daisy.util.convertImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the plant recognition screen
 * @property appWriteRepository Repository to access application data
 */
@HiltViewModel
class RecognitionViewModel
@Inject constructor(
    private val appWriteRepository: AppWriteRepository,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    // -- For session --

    private val _isUserLogged = MutableLiveData<Boolean>()
    val isUserLogged: LiveData<Boolean> = _isUserLogged

    private val _isSessionLoading = MutableLiveData<Boolean>()
    val isSessionLoading: LiveData<Boolean> = _isSessionLoading

    // Function to check if the user is logged in
    fun isLogged() {
        viewModelScope.launch {
            val userData = sessionDataStore.getSession()

            // If the user id is not empty, then they are logged in
            _isUserLogged.value = userData.id.isNotEmpty()

            _isSessionLoading.value = false
        }
    }

    // -- For plant recognition --

    private val _response = MutableLiveData<List<DataPlant>>()
    val response: LiveData<List<DataPlant>> = _response

    private val _showResponse = MutableLiveData<Boolean>()
    val showResponse: LiveData<Boolean> = _showResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _imageConverted = MutableLiveData<java.io.File>()
    val imageConverted: LiveData<java.io.File> = _imageConverted

    // Function to analyze the image
    fun analyzeImage(context: Context, image: ImageProxy, snackbarHostState: SnackbarHostState) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _imageConverted.value = convertImage(context, image)
                val uploaded = appWriteRepository.uploadImage(_imageConverted.value!!)
                _showResponse.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackbar(snackbarHostState, message = e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to hide the response
    fun hideResponse() {
        _imageConverted.value = java.io.File("")
        _showResponse.value = false
    }

    // Function to show the error snackbar
    fun showSnackbar(snackbarHostState: SnackbarHostState, message: String? = null) {
        val msg = message ?: "Error recognizing the image, please try again"
        viewModelScope.launch {
            snackbarHostState
                .showSnackbar(message = msg)
        }
    }
}