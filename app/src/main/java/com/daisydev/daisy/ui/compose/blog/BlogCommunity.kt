package com.daisydev.daisy.ui.compose.blog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.daisydev.daisy.models.BlogEntry
import com.daisydev.daisy.ui.components.LoadingIndicator
import com.daisydev.daisy.ui.feature.blog.BlogSharedViewModel
import com.daisydev.daisy.ui.feature.blog.BlogViewModel
import com.daisydev.daisy.ui.navigation.NavRoute

// Functions that are responsible for displaying the content of each tab
@Composable
fun BlogCommunity(
    navController: NavController,
    response: MutableList<BlogEntry>?,
    firstLoading: Boolean,
    viewModel: BlogViewModel,
    sharedViewModel: BlogSharedViewModel,
) {
    val isContentLoading by viewModel.isContentLoading.observeAsState(false)

    // Information of the 10 most recent entries from the community JSON
    Column {
        BlogSearch(viewModel = viewModel)
        Text(
            text = "Community entries",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(15.dp)
        )
        // List of entries
        // Loading of the data
        if (firstLoading || isContentLoading) {
            if (firstLoading) {
                viewModel.getInitialBlogEntries()
            } else {
                viewModel.getFilteredBlogEntries()
            }

            LoadingIndicator()
        } else {

            if (response.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No blog entries",
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                itemsIndexed(response!!) { _, blogEntry ->
                    CardEntries(
                        navController = navController,
                        blogEntry = blogEntry,
                        sharedViewModel = sharedViewModel
                    )
                }
            }
        }
    }
}

// Function that is responsible for handling the page's search engine
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlogSearch(viewModel: BlogViewModel) {
    val currentText by viewModel.searchText.observeAsState("")
    val clickedSearch = remember { mutableStateOf(false) }
    val suggestionsPlants = listOf("Cinnamon", "Mint", "Roses")
    val suggestionsSymptoms = listOf("Headache", "Stomachache", "Sore throat")

    SearchBar(
        modifier = Modifier
            .padding(15.dp),
        leadingIcon = {
            IconButton(
                onClick = {
                    clickedSearch.value = false
                    viewModel.setIsContentLoading()
                },
                content = { Icon(Icons.Default.Search, contentDescription = null) },
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    clickedSearch.value = false
                    viewModel.setSearchText("")
                    viewModel.setIsContentLoading()
                },
                content = { Icon(Icons.Default.Clear, contentDescription = null) },
                // According to the state of the viable hides the icon
                modifier = if (clickedSearch.value) Modifier else Modifier.width(0.dp),
            )
        },
        colors = SearchBarDefaults.colors(),
        shape = SearchBarDefaults.dockedShape,
        active = clickedSearch.value,
        content = {

            Box(modifier = Modifier.padding(16.dp)) {
                // We add search suggestions
                if (currentText.isEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // We show the search suggestions from the list of plant suggestions
                        LazyColumn {
                            items(suggestionsPlants.size) { items ->
                                Card(
                                    onClick = {
                                        viewModel.setSearchText(suggestionsPlants[items])
                                        clickedSearch.value = false
                                        viewModel.setIsContentLoading()
                                    },
                                    content = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start

                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = null)
                                            Text(
                                                text = suggestionsPlants[items],
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    ),
                                )
                            }
                        }

                        Text(
                            text = "Symptoms",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 15.dp, bottom = 8.dp),
                            color = Color.Gray
                        )

                        // We show the search suggestions from the list of symptom suggestions
                        // through a LazyVerticalGrid and AssistChip

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            items(suggestionsSymptoms.size) { items ->
                                AssistChip(
                                    label = { Text(text = suggestionsSymptoms[items]) },
                                    onClick = {
                                        viewModel.setSearchText(suggestionsSymptoms[items])
                                        clickedSearch.value = false
                                        viewModel.setIsContentLoading()
                                    },
                                    interactionSource = remember { MutableInteractionSource() },
                                )
                            }
                        }
                    }
                }
            }
        },
        onActiveChange = {
            clickedSearch.value = it
            // S
        },
        onQueryChange = { viewModel.setSearchText(it) },
        query = currentText,
        onSearch = {
            clickedSearch.value = false
            viewModel.setIsContentLoading()
        },
        placeholder = { Text(text = "Search") },
    )
}

// Function that is responsible for displaying the content of the page,
// cards with information from the entries
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardEntries(
    navController: NavController,
    blogEntry: BlogEntry,
    sharedViewModel: BlogSharedViewModel
) {

    val textContent =
        if (blogEntry.entry_content.length > 36)
            "${blogEntry.entry_content.subSequence(0..35)}..."
        else
            blogEntry.entry_content

    Column() {
        ListItem(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable(
                    enabled = true,
                    onClick = {
                        sharedViewModel.setSelectBlogEntry(blogEntry)
                        navController.navigate(NavRoute.EntryBlog.path)
                    }
                ),
            overlineContent = { Text(blogEntry.name_user) },
            supportingContent = { Text(textContent) },
            headlineContent = { Text(blogEntry.entry_title) },
            leadingContent = {
                Avatar(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp),
                    blogEntry.name_user.first().toString().uppercase()
                )
            },
        )
    }
}

@Composable
private fun Avatar(modifier: Modifier, letter: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = modifier.size(40.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}