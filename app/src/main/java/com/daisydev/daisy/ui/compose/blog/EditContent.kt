package com.daisydev.daisy.ui.compose.blog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daisydev.daisy.ui.components.LoadingIndicator
import com.daisydev.daisy.ui.feature.blog.BlogSharedViewModel

@Composable
fun EditContent(viewModel: BlogSharedViewModel) {
    val selected by viewModel.selected.observeAsState()
    val isEditContentLoading by viewModel.isEditContentLoading.observeAsState(false)
    val saveContentEnabled by viewModel.saveContentEnabled.observeAsState(false)

    val newTitle by viewModel.newTitle.observeAsState("")
    val newContent by viewModel.newContent.observeAsState("")
    val newPlants by viewModel.newPlants.observeAsState("")
    val newSymptoms by viewModel.newSymptoms.observeAsState("")

    LaunchedEffect(Unit) {
        viewModel.onEditBlogEntryChanged(
            selected?.entry_title!!,
            selected?.entry_content!!,
            selected?.plants?.joinToString(separator = ", ")!!,
            selected?.symptoms?.joinToString(separator = ", ")!!
        )
    }

    if (isEditContentLoading) {
        LoadingIndicator()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.90f)
                    .align(Alignment.TopStart)
            ) {
                item {
                    EntryTitleField(entryTitle = newTitle) {
                        viewModel.onEditBlogEntryChanged(
                            it,
                            newContent,
                            newPlants,
                            newSymptoms
                        )
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    EntryContentField(entryContent = newContent) {
                        viewModel.onEditBlogEntryChanged(
                            newTitle,
                            it,
                            newPlants,
                            newSymptoms
                        )
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    PlantsField(plants = newPlants) {
                        viewModel.onEditBlogEntryChanged(
                            newTitle,
                            newContent,
                            it,
                            newSymptoms
                        )
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    SymptomsField(symptoms = newSymptoms) {
                        viewModel.onEditBlogEntryChanged(
                            newTitle,
                            newContent,
                            newPlants,
                            it
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CancelButton(viewModel = viewModel)
                Spacer(modifier = Modifier.padding(2.dp))
                SaveButton(saveEnable = saveContentEnabled) {
                    viewModel.onSetEditedContent()
                }
            }
        }
    }
}

// Composable for the post title
@Composable
private fun EntryTitleField(entryTitle: String, onEntryTitleChanged: (String) -> Unit) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = entryTitle,
        onValueChange = onEntryTitleChanged,
        label = { Text(text = "Title") },
        placeholder = { Text(text = "Title") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = true,
        maxLines = 1,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
    )
}

// Composable for the post title
@Composable
private fun EntryContentField(entryContent: String, onEntryContentChanged: (String) -> Unit) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = entryContent,
        onValueChange = onEntryContentChanged,
        label = { Text(text = "Content") },
        placeholder = { Text(text = "Content") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = false,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
    )
}

// Composable for the plants field
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantsField(plants: String, onPlantsChanged: (String) -> Unit) {
    val plantsSplit = plants.split(",").map { it.trim() }

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = plants,
        onValueChange = onPlantsChanged,
        label = { Text(text = "Plants used") },
        placeholder = { Text(text = "Chamomile, Lavender, ...") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = false,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(5.dp)
    ) {
        items(plantsSplit.size) { index ->
            val plant = plantsSplit[index]

            if (plant.isNotEmpty()) {
                InputChip(
                    onClick = {
                        // Removes the selected plant from the list and checks
                        // that there is no comma at the beginning or end of the word
                        val newPlants =
                            plantsSplit.filter { it != plant }.joinToString(", ")
                        onPlantsChanged(newPlants)
                    },
                    label = { Text(text = plantsSplit[index]) },
                    selected = false,
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        trailingIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        borderColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

// Composable for the symptoms field
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SymptomsField(symptoms: String, onSymptomsChanged: (String) -> Unit) {
    val symptomsSplit = symptoms.split(",").map { it.trim() }

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = symptoms,
        onValueChange = onSymptomsChanged,
        label = { Text(text = "Symptoms") },
        placeholder = { Text(text = "Headache, Cough, ...") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = false,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(5.dp)
    ) {
        items(symptomsSplit.size) { index ->
            val symptom = symptomsSplit[index]

            if (symptom.isNotEmpty()) {
                InputChip(
                    onClick = {
                        // Removes the selected symptom from the list and checks
                        // that there is no comma at the beginning or end of the string
                        val newSymptoms =
                            symptomsSplit.filter { it != symptom }.joinToString(", ")
                        onSymptomsChanged(newSymptoms)
                    },
                    label = { Text(text = symptomsSplit[index]) },
                    selected = false,
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        trailingIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        borderColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

// Composable for the edit content button
@Composable
private fun SaveButton(saveEnable: Boolean, onSaveClicked: () -> Unit) {
    Button(
        modifier = Modifier.width(150.dp),
        onClick = { onSaveClicked() },
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = saveEnable
    ) {
        Text(text = "Save")
    }
}

@Composable
private fun CancelButton(viewModel: BlogSharedViewModel) {
    Button(
        modifier = Modifier.width(150.dp),
        onClick = { viewModel.setEditContent(false) },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
        ),
        enabled = true
    ) {
        Text(text = "Cancel")
    }
}