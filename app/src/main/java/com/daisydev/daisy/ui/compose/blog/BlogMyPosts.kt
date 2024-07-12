package com.daisydev.daisy.ui.compose.blog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.daisydev.daisy.R
import com.daisydev.daisy.models.BlogEntry
import com.daisydev.daisy.ui.components.LoadingIndicator
import com.daisydev.daisy.ui.feature.blog.BlogSharedViewModel
import com.daisydev.daisy.ui.feature.blog.BlogViewModel
import com.daisydev.daisy.ui.navigation.NavRoute

/**
 * Entry screen for viewing one's own blog posts
 */
@Composable
fun BlogMyPosts(
    viewModel: BlogViewModel,
    response: MutableList<BlogEntry>?,
    navController: NavController,
    sharedViewModel: BlogSharedViewModel,
    showNewBlogEntry: Boolean,
    snackbarHostState: SnackbarHostState
) {
    val isSelfLoading by viewModel.isSelfLoading.observeAsState()

    if (isSelfLoading!!) {
        viewModel.getSelfBlogEntries()
        LoadingIndicator()
    } else {
        if (showNewBlogEntry) {
            NewBlog(viewModel = viewModel, snackbarHostState = snackbarHostState)
        } else {
            MainContent(
                response = response,
                navController = navController,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

// Function responsible for displaying the page content
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    response: MutableList<BlogEntry>?,
    navController: NavController,
    sharedViewModel: BlogSharedViewModel,
    viewModel: BlogViewModel,
    snackbarHostState: SnackbarHostState
) {
    val openDialog = remember { mutableStateOf(false) }
    val toDeleteItemIndex = remember { mutableStateOf(0) }
    val toDeleteItemData = remember { mutableStateOf<BlogEntry?>(null) }
    val isDeleteSuccess by viewModel.isDeleteSuccess.observeAsState(false)
    val isDeleteError by viewModel.isDeleteError.observeAsState(false)

    if (isDeleteSuccess) {
        viewModel.showSnackbar(
            snackbarHostState = snackbarHostState,
            message = "Entry deleted successfully"
        )
        viewModel.setIsDeleteSuccess(false)
    }

    if (isDeleteError) {
        viewModel.showSnackbar(
            snackbarHostState = snackbarHostState,
            message = "Error deleting entry, please try again"
        )
        viewModel.setIsDeleteError(false)
    }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
                viewModel.deleteBlogEntry(
                    toDeleteItemData.value!!, toDeleteItemIndex.value
                )
            },
            title = {
                Text(
                    text = "Delete entry",
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this entry?",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        viewModel.deleteBlogEntry(
                            toDeleteItemData.value!!, toDeleteItemIndex.value
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = "Delete",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        )
    }

    Column {
        Button(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 20.dp, end = 35.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            onClick = { viewModel.setShowNewBlogEntry(true) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 5.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_post_add),
                    contentDescription = "Add post",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp), text = "Create"
                )
            }
        }
        Text(
            text = "My entries",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(15.dp)
        )

        if (response.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You have no entries",
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
            itemsIndexed(response!!) { index, blogEntry ->
                val dismissState = rememberDismissState(
                    confirmValueChange = {
                        if (it == DismissValue.DismissedToStart) {
                            toDeleteItemData.value = blogEntry
                            toDeleteItemIndex.value = index
                            openDialog.value = true
                            true
                        } else {
                            false
                        }
                    }
                )

                if (dismissState.currentValue != DismissValue.Default) {
                    LaunchedEffect(Unit) {
                        dismissState.reset()
                    }
                }

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "Delete",
                                color = Color.White,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                        }
                    },
                    dismissContent = {
                        CardEntries(
                            navController = navController,
                            blogEntry = blogEntry,
                            sharedViewModel = sharedViewModel
                        )
                    }
                )
            }
        }
    }
}

// Function responsible for displaying the page content,
// cards with information from the entries
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
                        sharedViewModel.setIsSelfContent(true)
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

// Function that generates the post's avatar
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