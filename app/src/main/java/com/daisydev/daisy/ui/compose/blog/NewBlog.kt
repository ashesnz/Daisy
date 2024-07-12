package com.daisydev.daisy.ui.compose.blog

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.daisydev.daisy.ui.components.LoadingIndicator
import com.daisydev.daisy.ui.feature.blog.BlogViewModel

/**
 * New blog post view entry screen
 * @param viewModel View's ViewModel
 * @param snackbarHostState Snackbar's state
 */
@Composable
fun NewBlog(viewModel: BlogViewModel, snackbarHostState: SnackbarHostState) {

    // State variables
    val entryTitle by viewModel.entryTitle.observeAsState("")
    val entryContent by viewModel.entryContent.observeAsState("")
    val plants by viewModel.plants.observeAsState("")
    val symptoms by viewModel.symptoms.observeAsState("")
    val imageUri by viewModel.imageUri.observeAsState(null)
    val saveEnable by viewModel.saveEnable.observeAsState(false)
    val isNewBlogEntryLoading by viewModel.isNewBlogEntryLoading.observeAsState(false)
    val isNewBlogEntrySuccess by viewModel.isNewBlogEntrySuccess.observeAsState(false)
    val isNewBlogEntryError by viewModel.isNewBlogEntryError.observeAsState(false)

    // If the publication has been created successfully
    if (isNewBlogEntrySuccess) {
        viewModel.setShowNewBlogEntry(false)
        viewModel.setIsSelfLoading()
        viewModel.showSnackbar(
            snackbarHostState = snackbarHostState,
            message = "Publication created successfully"
        )
    }

    // If an error occurred while creating the publication
    if (isNewBlogEntryError) {
        viewModel.setShowNewBlogEntry(false)
        viewModel.showSnackbar(
            snackbarHostState = snackbarHostState,
            message = "Error creating the publication, try again"
        )
    }

    // Gallery launcher to obtain the image
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uriList ->
        uriList?.let {
            viewModel.onNewBlogEntryChanged(
                entryTitle,
                entryContent,
                plants,
                symptoms,
                it
            )
        }
    }

    // If the new publication is loading, show the loading indicator
    if (isNewBlogEntryLoading) {
        LoadingIndicator()
    } else { // If not loading the new publication, display the screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.padding(16.dp))
            EntryTitleField(entryTitle = entryTitle) {
                viewModel.onNewBlogEntryChanged(
                    it,
                    entryContent,
                    plants,
                    symptoms,
                    imageUri
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            EntryContentField(entryContent = entryContent) {
                viewModel.onNewBlogEntryChanged(
                    entryTitle,
                    it,
                    plants,
                    symptoms,
                    imageUri
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            PlantsField(plants = plants) {
                viewModel.onNewBlogEntryChanged(
                    entryTitle,
                    entryContent,
                    it,
                    symptoms,
                    imageUri
                )
            }
            Spacer(modifier = Modifier.padding(5.dp))
            SymptomsField(symptoms = symptoms) {
                viewModel.onNewBlogEntryChanged(
                    entryTitle,
                    entryContent,
                    plants,
                    it,
                    imageUri
                )
            }
            Spacer(modifier = Modifier.padding(5.dp))
            ImageField(imageUri = imageUri, galleryLauncher = galleryLauncher) {
                viewModel.onNewBlogEntryChanged(
                    entryTitle,
                    entryContent,
                    plants,
                    symptoms,
                    null
                )
            }
            Spacer(modifier = Modifier.padding(16.dp))
            SaveButton(saveEnable = saveEnable) {
                viewModel.onSaveNewBlogEntryModel()
            }
        }
    }
}

// Composable for the publication title
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

// Composable for the publication content
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
        label = { Text(text = "plants used") },
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
                        // that there is no comma at the beginning or end of the word
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

// Composable for the image field
@Composable
private fun ImageField(
    imageUri: Uri?,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    onImageDeleted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = { // Launches the gallery to select an image
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = "Select image")
                }
                Spacer(modifier = Modifier.padding(3.dp))

                // Shows the delete image button if an image is selected
                if (imageUri != null) {
                    IconButton(onClick = {
                        onImageDeleted()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete image"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))

            // Displays the selected image, if there is one
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// Composable for the save button
@Composable
private fun SaveButton(saveEnable: Boolean, onSaveClicked: () -> Unit) {
    Button(
        onClick = { onSaveClicked() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = saveEnable
    ) {
        Text(text = "Create")
    }
}