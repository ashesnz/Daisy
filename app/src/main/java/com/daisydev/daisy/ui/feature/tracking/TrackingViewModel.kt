package com.daisydev.daisy.ui.feature.tracking


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daisydev.daisy.ui.compose.tracking.Message
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


@OptIn(FlowPreview::class)
class TrackingViewModel:  ViewModel()  {
    // Variable for the list of plants
    private val _sampleData = MutableLiveData(emptyArray<Message>())
    val sampleData: LiveData<Array<Message>> get() = _sampleData

    // Text entered by the user
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // State to detect if the user is typing in the search bar
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    // State of the plant list
    private val _plants = MutableStateFlow(allplantsSeg)

    val care = searchText
        .debounce(1000L)
        .onEach { _isSearching.update { true } }
        .combine(_plants) { text, care ->
            if (text.isBlank()) {
                care
            } else {
                delay(2000L)
                care.filter {
                    it.doesMatchSearchQuery(text)
                }
            }
        }.onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _plants.value
        )

    // Changes to the search text, used in the UI, whenever the user types something
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun setSampleData(plantMessages: Array<Message>) {
        _sampleData.value = plantMessages
    }

}

// Class for plants
data class Care(
    val plant: String
) {
    // Detects any combination of a symptom
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            "$plant"
        )
        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}

private val allplantsSeg = listOf(
    Care(
        plant = "Orchid"
    ),
    Care(
        plant = "Rose"
    ),
    Care(
        plant = "Daisy"
    ),
    Care(
        plant = "Gardenia"
    ),
    Care(
        plant = "Cinnamon"
    ),
    Care(
        plant = "Apple Tree"
    ),
    Care(
        plant = "Hydrangea"
    ),
    Care(
        plant = "Sunflower"
    ),
    Care(
        plant = "Blueberry"
    ),
)