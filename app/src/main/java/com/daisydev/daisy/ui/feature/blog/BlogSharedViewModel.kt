package com.daisydev.daisy.ui.feature.blog

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daisydev.daisy.models.BlogDocumentModel
import com.daisydev.daisy.models.BlogEntry
import com.daisydev.daisy.repository.remote.AppWriteRepository
import com.daisydev.daisy.util.ExportConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel for the Blog screen
 * @property selected BlogEntry which is shared between BlogScreen and EntryBlog
 */
@HiltViewModel
class BlogSharedViewModel @Inject constructor(
    private val appWriteRepository: AppWriteRepository
) : ViewModel() {
    // The one shared between BlogScreen and EntryBlog
    private val _selected = MutableLiveData<BlogEntry>()
    val selected: LiveData<BlogEntry> = _selected

    // Function to set a blogEntry
    fun setSelectBlogEntry(blogEntry: BlogEntry) {
        _selected.value = blogEntry
    }

    // To know if the content is self-owned
    private val _isSelfContent = MutableLiveData<Boolean>()
    val isSelfContent: LiveData<Boolean> = _isSelfContent

    // Function to set if the content is self-owned
    fun setIsSelfContent(isSelfContent: Boolean) {
        _isSelfContent.value = isSelfContent
    }

    // -- To edit the image --
    private val _editImage = MutableLiveData<Boolean>()
    val editImage: LiveData<Boolean> = _editImage

    private val _newImageUri = MutableLiveData<Uri?>()
    val newImageUri: LiveData<Uri?> = _newImageUri

    private val _saveImageEnabled = MutableLiveData<Boolean>()
    val saveImageEnabled: LiveData<Boolean> = _saveImageEnabled

    private val _isEditImageLoading = MutableLiveData<Boolean>()
    val isEditImageLoading: LiveData<Boolean> = _isEditImageLoading

    private val _editImageSuccess = MutableLiveData<Boolean>()
    val editImageSuccess: LiveData<Boolean> = _editImageSuccess

    private val _editImageError = MutableLiveData<Boolean>()
    val editImageError: LiveData<Boolean> = _editImageError

    // Function to set if the edit image screen is shown
    fun setEditImage(editImage: Boolean) {
        _editImage.value = editImage
    }

    // Function to set the new image
    fun setNewImageUri(uri: Uri?) {
        _newImageUri.value = uri
        _saveImageEnabled.value = uri != null
    }

    // Function to set the value that shows
    // if the image was saved successfully
    fun setEditImageSuccess(value: Boolean) {
        _editImageSuccess.value = value
    }

    // Function to set the value that shows
    // if there was an error saving the image
    fun setEditImageError(value: Boolean) {
        _editImageError.value = value
    }

    // Function to update the image
    fun onSetEditedImage() {
        _isEditImageLoading.value = true

        viewModelScope.launch {
            try {
                val docId = _selected.value!!.id

                // We update the image
                val uploaded = appWriteRepository.uploadBlogImage(_newImageUri.value!!)

                var imageUrl = "${ExportConstants.baseUrl}/storage/buckets/"
                imageUrl += "${uploaded.bucketId}/files/${uploaded.id}/view"
                imageUrl += "?project=${ExportConstants.projectId}"

                // If the image was uploaded, then we update the document
                val newBlogEntry = BlogDocumentModel(
                    id_user = _selected.value!!.id_user,
                    name_user = _selected.value!!.name_user,
                    entry_title = _selected.value!!.entry_title,
                    entry_content = _selected.value!!.entry_content,
                    entry_image_id = uploaded.id,
                    entry_image_url = imageUrl,
                    posted = _selected.value!!.posted,
                    plants = _selected.value!!.plants,
                    symptoms = _selected.value!!.symptoms
                )
                appWriteRepository.updateDocument(docId, newBlogEntry)

                // Delete the previous image if it exists
                val oldImageId = _selected.value!!.entry_image_id

                if (oldImageId.isNotEmpty())
                    appWriteRepository.deleteBlogImage(oldImageId)

                // Update the selected blogEntry
                _selected.value!!.entry_image_id = uploaded.id
                _selected.value!!.entry_image_url = imageUrl

                _editImageSuccess.value = true
                _editImage.value = false
                _newImageUri.value = null
            } catch (e: Exception) {
                _editImageError.value = true
            } finally {
                _isEditImageLoading.value = false
            }
        }
    }

    // -- To edit the content --
    private val _editContent = MutableLiveData<Boolean>()
    val editContent: LiveData<Boolean> = _editContent

    private val _saveContentEnabled = MutableLiveData<Boolean>()
    val saveContentEnabled: LiveData<Boolean> = _saveContentEnabled

    private val _isEditContentLoading = MutableLiveData<Boolean>()
    val isEditContentLoading: LiveData<Boolean> = _isEditContentLoading

    private val _editContentSuccess = MutableLiveData<Boolean>()
    val editContentSuccess: LiveData<Boolean> = _editContentSuccess

    private val _editContentError = MutableLiveData<Boolean>()
    val editContentError: LiveData<Boolean> = _editContentError

    // Variables to edit the content
    private val _newTitle = MutableLiveData<String>()
    val newTitle: LiveData<String> = _newTitle

    private val _newContent = MutableLiveData<String>()
    val newContent: LiveData<String> = _newContent

    private val _newPlants = MutableLiveData<String>()
    val newPlants: LiveData<String> = _newPlants

    private val _newSymptoms = MutableLiveData<String>()
    val newSymptoms: LiveData<String> = _newSymptoms

    fun setEditContent(editContent: Boolean) {
        _editContent.value = editContent
    }

    fun setEditContentSuccess(value: Boolean) {
        _editContentSuccess.value = value
    }

    fun setEditContentError(value: Boolean) {
        _editContentError.value = value
    }

    fun onEditBlogEntryChanged(title: String, content: String, plants: String, symptoms: String) {
        _newTitle.value = title
        _newContent.value = content
        _newPlants.value = plants
        _newSymptoms.value = symptoms

        _saveContentEnabled.value = title.isNotEmpty() && content.isNotEmpty()
    }

    fun onSetEditedContent() {
        _isEditContentLoading.value = true

        viewModelScope.launch {
            try {
                val docId = _selected.value!!.id

                val plants =
                    if (_newPlants.value!!.isEmpty())
                        listOf<String>("None")
                    else
                        _newPlants.value!!.split(",")
                            .map { it.trim() }
                val symptoms =
                    if (_newSymptoms.value!!.isEmpty())
                        listOf<String>("None")
                    else
                        _newSymptoms.value!!.split(",")
                            .map { it.trim() }

                // Update the content
                val newBlogEntry = BlogDocumentModel(
                    id_user = _selected.value!!.id_user,
                    name_user = _selected.value!!.name_user,
                    entry_title = _newTitle.value!!,
                    entry_content = _newContent.value!!,
                    entry_image_id = _selected.value!!.entry_image_id,
                    entry_image_url = _selected.value!!.entry_image_url,
                    posted = _selected.value!!.posted,
                    plants = plants,
                    symptoms = symptoms
                )
                appWriteRepository.updateDocument(docId, newBlogEntry)

                // Update the selected blogEntry
                _selected.value!!.entry_title = _newTitle.value!!
                _selected.value!!.entry_content = _newContent.value!!
                _selected.value!!.plants = plants
                _selected.value!!.symptoms = symptoms

                _editContentSuccess.value = true
                _editContent.value = false
            } catch (e: Exception) {
                _editContentError.value = true
            } finally {
                _isEditContentLoading.value = false
            }
        }
    }

    // Function to display a message on the screen through a snackbar
    fun showSnackbar(snackbarHostState: SnackbarHostState, message: String) {
        viewModelScope.launch {
            snackbarHostState
                .showSnackbar(message = message)
        }
    }
}