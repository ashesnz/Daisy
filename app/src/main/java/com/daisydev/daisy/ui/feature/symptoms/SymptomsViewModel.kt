package com.daisydev.daisy.ui.feature.symptoms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daisydev.daisy.ui.compose.symptoms.Message
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@OptIn(FlowPreview::class)
class MainViewModel : ViewModel() {
    // Variable for the list of plants
    private val _sampleData = MutableLiveData(emptyArray<Message>())
    val sampleData: LiveData<Array<Message>> get() = _sampleData

    // Text entered by the user
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // State to detect if the user is typing in the search bar
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // State of the list of symptoms
    private val _symptoms = MutableStateFlow(allSymptoms)

    val symptoms = searchText
        .debounce(1000L)
        .onEach { _isSearching.update { true } }
        .combine(_symptoms) { text, symptoms ->
            if (text.isBlank()) {
                symptoms
            } else {
                delay(1000L)
                symptoms.filter {
                    it.doesMatchSearchQuery(text)
                }
            }
        }.onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(2000),
            _symptoms.value
        )

    // Changes to the search text, used in the UI, every time the user types something
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun setSampleData(plantMessages: Array<Message>) {
        _sampleData.value = plantMessages
    }

    fun loadingFalse() {
        viewModelScope.launch {
            _isLoading.value = false
        }
    }

    fun loadingTrue() {
        viewModelScope.launch {
            _isLoading.value = true
        }
    }

}

// Class for symptoms
data class Symptom(
    val symptom: String
) {
    // Detects some combination of a symptom
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            "$symptom"
        )
        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}

private val allSymptoms = listOf(
    Symptom(
        symptom = "Fever"
    ),
    Symptom(
        symptom = "Cough"
    ),
    Symptom(
        symptom = "Hoarseness"
    ),
    Symptom(
        symptom = "Fatigue"
    ),
    Symptom(
        symptom = "Nausea"
    ),
    Symptom(
        symptom = "Vomiting"
    ),
    Symptom(
        symptom = "Stomach problems"
    ),
    Symptom(
        symptom = "Abdominal pain"
    ),
    Symptom(
        symptom = "Skin spots"
    ),
)