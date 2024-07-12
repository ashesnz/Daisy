package com.daisydev.daisy.ui.compose.blog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.daisydev.daisy.models.BlogEntry
import com.daisydev.daisy.ui.components.LoadingIndicator
import com.daisydev.daisy.ui.feature.blog.BlogSharedViewModel
import com.daisydev.daisy.ui.feature.blog.BlogViewModel
import com.daisydev.daisy.ui.navigation.NavRoute


/**
 * Main screen of the blog section
 * @param navController navigation controller
 * @param sharedViewModel ViewModel shared between blog screens
 * @param viewModel ViewModel of the screen
 */
@Composable
fun BlogScreen(
    navController: NavController,
    sharedViewModel: BlogSharedViewModel,
    viewModel: BlogViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {

    // For session
    val isUserLogged by viewModel.isUserLogged.observeAsState(true)
    val isSessionLoading by viewModel.isSessionLoading.observeAsState(true)

    // If there is no session, redirect to the access screen
    if (!isUserLogged) {
        LaunchedEffect(Unit) {
            navController.navigate(NavRoute.Access.path) {
                popUpTo(NavRoute.Session.path) { inclusive = true }
            }
        }
    } else {
        ShowLoadingOrScreen(
            navController,
            sharedViewModel,
            viewModel,
            isSessionLoading,
            snackbarHostState
        )
    }
}

/**
 * Function that shows the loading or the blog screen
 * @param navController navigation controller
 * @param sharedViewModel ViewModel shared between blog screens
 * @param viewModel ViewModel of the screen
 * @param isSessionLoading variable that indicates if the session is loading
 */
@Composable
private fun ShowLoadingOrScreen(
    navController: NavController,
    sharedViewModel: BlogSharedViewModel,
    viewModel: BlogViewModel,
    isSessionLoading: Boolean,
    snackbarHostState: SnackbarHostState
) {

    // Variable to show the loading
    var shouldShowLoading by remember { mutableStateOf(true) }

    // This variable updates when isSessionLoading changes
    LaunchedEffect(isSessionLoading) {
        shouldShowLoading = isSessionLoading
    }

    // Show loading or blog screen
    if (shouldShowLoading) {
        viewModel.isLogged()
        LoadingIndicator()
    } else {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            StartBlogScreen(
                navController = navController,
                viewModel = viewModel,
                sharedViewModel = sharedViewModel,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

/**
 * Main screen of the blog section
 * @param navController navigation controller
 * @param sharedViewModel ViewModel shared between blog screens
 * @param viewModel ViewModel of the screen
 */
@Composable
fun StartBlogScreen(
    navController: NavController,
    viewModel: BlogViewModel,
    sharedViewModel: BlogSharedViewModel,
    snackbarHostState: SnackbarHostState
) {
    val tabs = listOf("Community", "My entries")
    val selectedTabIndex by viewModel.selectedTabIndex.observeAsState()

    val response by viewModel.response.observeAsState()
    val isFirstLoading by viewModel.isFirstLoading.observeAsState(true)

    val showNewBlogEntry by viewModel.showNewBlogEntry.observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopCenter)
    ) {
        // Page title centered at the top
        Card(
            colors = CardDefaults.cardColors(),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .wrapContentSize(Alignment.TopCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(0)
        ) {
            TopAppBar(viewModel = viewModel, showNewBlogEntry = showNewBlogEntry)
        }
        BlogTabs(
            tabs = tabs, selectedTabIndex = selectedTabIndex!!,
            navController = navController,
            response = response,
            loading = isFirstLoading,
            viewModel = viewModel,
            sharedViewModel = sharedViewModel,
            showNewBlogEntry = showNewBlogEntry,
            snackbarHostState = snackbarHostState
        )
    }
}

// Function responsible for displaying the page title
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(showNewBlogEntry: Boolean, viewModel: BlogViewModel) {
    if (showNewBlogEntry) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "New entry",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    viewModel.setShowNewBlogEntry(false)
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
    } else {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "Blog",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
}

// Page Tabs control
@Composable
private fun BlogTabs(
    tabs: List<String> = listOf(),
    selectedTabIndex: Int,
    navController: NavController,
    response: MutableList<BlogEntry>?,
    loading: Boolean,
    viewModel: BlogViewModel,
    sharedViewModel: BlogSharedViewModel,
    showNewBlogEntry: Boolean,
    snackbarHostState: SnackbarHostState
) {
    // Page tabs
    MaterialTheme() {

        if (!showNewBlogEntry) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(text = { Text(text = title) },
                        selected = selectedTabIndex == index,
                        onClick = {
                            if (selectedTabIndex != index) {
                                when (index) {
                                    0 -> {
                                        viewModel.setSearchText("")
                                        viewModel.setIsContentLoading()
                                    }

                                    1 -> {
                                        viewModel.setIsSelfLoading()
                                    }
                                }
                            }

                            viewModel.setSelectedTabIndex(index)
                        })
                }
            }
        }

        BlogContent(
            selectedTabIndex = selectedTabIndex,
            navController = navController,
            response = response,
            loading = loading,
            viewModel = viewModel,
            sharedViewModel = sharedViewModel,
            showNewBlogEntry = showNewBlogEntry,
            snackbarHostState = snackbarHostState
        )
    }
}

// Function responsible for displaying the page content
// based on the selected tab
@Composable
private fun BlogContent(
    selectedTabIndex: Int,
    navController: NavController,
    response: MutableList<BlogEntry>?,
    loading: Boolean,
    viewModel: BlogViewModel,
    sharedViewModel: BlogSharedViewModel,
    showNewBlogEntry: Boolean,
    snackbarHostState: SnackbarHostState
) {
    when (selectedTabIndex) {
        0 -> BlogCommunity(
            navController = navController,
            response = response,
            firstLoading = loading,
            viewModel = viewModel,
            sharedViewModel = sharedViewModel
        )

        1 -> BlogMyPosts(
            navController = navController,
            viewModel = viewModel,
            response = response,
            sharedViewModel = sharedViewModel,
            showNewBlogEntry = showNewBlogEntry,
            snackbarHostState = snackbarHostState
        )
    }
}