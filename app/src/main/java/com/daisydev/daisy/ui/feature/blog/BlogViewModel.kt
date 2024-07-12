package com.daisydev.daisy.ui.feature.blog

import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daisydev.daisy.models.BlogDocumentModel
import com.daisydev.daisy.models.BlogEntry
import com.daisydev.daisy.models.Session
import com.daisydev.daisy.repository.local.SessionDataStore
import com.daisydev.daisy.repository.remote.AppWriteRepository
import com.daisydev.daisy.util.ExportConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.models.File
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Blog screen
 * @property appWriteRepository Repository to access application data
 */
@HiltViewModel
class BlogViewModel
@Inject constructor(
    private val appWriteRepository: AppWriteRepository,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    // -- For session --

    private val _userData = MutableLiveData<Session>()

    private val _isUserLogged = MutableLiveData<Boolean>()
    val isUserLogged: LiveData<Boolean> = _isUserLogged

    private val _isSessionLoading = MutableLiveData<Boolean>()
    val isSessionLoading: LiveData<Boolean> = _isSessionLoading

    // Function to check if the user is logged in
    fun isLogged() {
        viewModelScope.launch {
            val userData = sessionDataStore.getSession()

            // If the user id is not empty, then they are logged in
            if (userData.id.isNotEmpty()) {
                _userData.value = userData
                _isUserLogged.value = true
            } else {
                _isUserLogged.value = false
            }

            _isSessionLoading.value = false
        }
    }

    // -- For the blog --

    private val _response = MutableLiveData<MutableList<BlogEntry>>()
    val response: LiveData<MutableList<BlogEntry>> = _response

    private val _isFirstLoading = MutableLiveData<Boolean>()
    val isFirstLoading: LiveData<Boolean> = _isFirstLoading

    private val _isContentLoading = MutableLiveData<Boolean>()
    val isContentLoading: LiveData<Boolean> = _isContentLoading

    private val _isSelfLoading = MutableLiveData<Boolean>()
    val isSelfLoading: LiveData<Boolean> = _isSelfLoading

    private val _searchText = MutableLiveData<String>()
    val searchText: LiveData<String> = _searchText

    private val _selectedTabIndex = MutableLiveData<Int>(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex

    // For creating a new blog
    // -- / Variables for creating a new blog --
    private val _showNewBlogEntry = MutableLiveData<Boolean>()
    val showNewBlogEntry: LiveData<Boolean> = _showNewBlogEntry

    private val _isNewBlogEntryLoading = MutableLiveData<Boolean>()
    val isNewBlogEntryLoading: LiveData<Boolean> = _isNewBlogEntryLoading

    private val _isNewBlogEntrySuccess = MutableLiveData<Boolean>()
    val isNewBlogEntrySuccess: LiveData<Boolean> = _isNewBlogEntrySuccess

    private val _isNewBlogEntryError = MutableLiveData<Boolean>()
    val isNewBlogEntryError: LiveData<Boolean> = _isNewBlogEntryError

    private val _saveEnable = MutableLiveData<Boolean>()
    val saveEnable: LiveData<Boolean> = _saveEnable

    private val _entryTitle = MutableLiveData<String>()
    val entryTitle: LiveData<String> = _entryTitle

    private val _entryContent = MutableLiveData<String>()
    val entryContent: LiveData<String> = _entryContent

    private val _plants = MutableLiveData<String>()
    val plants: LiveData<String> = _plants

    private val _symptoms = MutableLiveData<String>()
    val symptoms: LiveData<String> = _symptoms

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    // To delete a blog entry
    private val _isDeleteLoading = MutableLiveData<Boolean>()
    val isDeleteLoading: LiveData<Boolean> = _isDeleteLoading

    private val _isDeleteSuccess = MutableLiveData<Boolean>()
    val isDeleteSuccess: LiveData<Boolean> = _isDeleteSuccess

    private val _isDeleteError = MutableLiveData<Boolean>()
    val isDeleteError: LiveData<Boolean> = _isDeleteError

    fun setIsDeleteSuccess(value: Boolean) {
        _isDeleteSuccess.value = value
    }

    fun setIsDeleteError(value: Boolean) {
        _isDeleteError.value = value
    }

    // Function that controls whether or not the new blog creation screen is seen
    fun setShowNewBlogEntry(show: Boolean) {
        _showNewBlogEntry.value = show

        if (!show) {
            _isNewBlogEntrySuccess.value = false
        }
    }

    // Function to update the variables of the new blog creation
    fun onNewBlogEntryChanged(
        entryTitle: String,
        entryContent: String,
        plants: String,
        symptoms: String,
        imageUri: Uri?
    ) {
        _entryTitle.value = entryTitle
        _entryContent.value = entryContent
        _plants.value = plants
        _symptoms.value = symptoms
        _imageUri.value = imageUri
        _saveEnable.value = entryTitle.isNotEmpty() && entryContent.isNotEmpty()
    }

    // Function to save a new blog in the database
    fun onSaveNewBlogEntryModel() {
        _isNewBlogEntryLoading.value = true

        viewModelScope.launch {
            try {

                // Variables for the image
                val imageUri = _imageUri.value
                var image: File? = null
                var imageId: String? = null
                var imageUrl: String? = null

                // If the image is not null, then it is uploaded to the database
                // and the image url is obtained
                if (imageUri != null) {
                    image = appWriteRepository.uploadBlogImage(imageUri)
                    imageId = image.id
                    imageUrl = "${ExportConstants.baseUrl}/storage/buckets/"
                    imageUrl += "${image.bucketId}/files/$imageId/view"
                    imageUrl += "?project=${ExportConstants.projectId}"
                }

                // The plants and symptoms are obtained
                val plants =
                    if (_plants.value!!.isEmpty())
                        listOf<String>("None")
                    else
                        _plants.value!!.split(",")
                            .map { it.trim() }
                val symptoms =
                    if (_symptoms.value!!.isEmpty())
                        listOf<String>("None")
                    else
                        _symptoms.value!!.split(",")
                            .map { it.trim() }

                // The new blog is created
                val newBlogEntry = BlogDocumentModel(
                    id_user = _userData.value!!.id,
                    name_user = _userData.value!!.name,
                    entry_title = _entryTitle.value!!,
                    entry_content = _entryContent.value!!,
                    entry_image_id = imageId ?: "",
                    entry_image_url = imageUrl ?: "",
                    posted = true,
                    plants = plants,
                    symptoms = symptoms,
                )

                // The new blog is saved in the database and it is indicated that it was successful
                appWriteRepository.createDocument(newBlogEntry)
                _isNewBlogEntrySuccess.value = true
            } catch (e: Exception) {
                // If there was an error, it is indicated that there was an error
                _isNewBlogEntryError.value = true
            } finally {
                _isNewBlogEntryLoading.value = false
            }
        }
    }

    // Function that sets the index of the selected tab
    fun setSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    // Function that sets the community content loading screen
    fun setIsContentLoading() {
        _isContentLoading.value = true
    }

    // Function that sets the own content loading screen
    fun setIsSelfLoading() {
        _isSelfLoading.value = true
    }

    // Function that sets the search text in the community
    fun setSearchText(text: String) {
        _searchText.value = text
    }

    // Function that gets the community blogs for the first time
    fun getInitialBlogEntries() {
        viewModelScope.launch {
            _response.value = appWriteRepository.listDocuments()
            _isFirstLoading.value = false
        }
    }

    // Function that gets own blogs
    fun getSelfBlogEntries() {
        viewModelScope.launch {
            _response.value = appWriteRepository.listDocumentsOfUser(_userData.value!!.id)
            _isSelfLoading.value = false
        }
    }

    // Function that gets the community blogs filtered by the search text
    fun getFilteredBlogEntries() {
        viewModelScope.launch {
            _response.value =
                if (_searchText.value!!.isEmpty())
                    appWriteRepository.listDocuments()
                else
                    appWriteRepository.listDocumentsWithFilter(
                        _searchText.value!!
                    )
            _isContentLoading.value = false
        }
    }

    fun deleteBlogEntry(item: BlogEntry, index: Int) {
        viewModelScope.launch {
            try {
                appWriteRepository.deleteBlogImage(item.entry_image_id)
                appWriteRepository.deleteDocument(item.id)
                _response.value!!.removeAt(index)
                _isDeleteSuccess.value = true
            } catch (e: Exception) {
                Log.d("Error", "Error: ${e.message}")
                _isDeleteError.value = true
            } finally {
                _isDeleteLoading.value = false
            }
        }
    }

    // Function that shows a message on the screen through a snackbar
    fun showSnackbar(snackbarHostState: SnackbarHostState, message: String) {
        viewModelScope.launch {
            snackbarHostState
                .showSnackbar(message = message)
        }
    }
}