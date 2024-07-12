package com.daisydev.daisy.ui.compose.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.daisydev.daisy.models.Session
import com.daisydev.daisy.ui.components.LoadingIndicator
import com.daisydev.daisy.ui.feature.session.SessionViewModel
import com.daisydev.daisy.ui.navigation.NavRoute

// Screen to view the logged-in user's data
@Composable
fun SessionScreen(
    navController: NavController,
    viewModel: SessionViewModel = hiltViewModel()
) {

    val userData by viewModel.userData.observeAsState()
    val isUserLogged by viewModel.isUserLogged.observeAsState(true)
    val isLoading by viewModel.isLoading.observeAsState(true)

    // If the user is not logged in, redirect them to the access screen
    if (!isUserLogged) {
        LaunchedEffect(Unit) {
            navController.navigate(NavRoute.Access.path) {
                popUpTo(NavRoute.Session.path) { inclusive = true }
            }
        }
    } else {
        ShowLoadingOrScreen(viewModel, isLoading, userData)
    }
}

// Display the loading screen or the user data screen
@Composable
private fun ShowLoadingOrScreen(
    viewModel: SessionViewModel,
    isLoading: Boolean,
    userData: Session?
) {
    // Variable to show the loading screen
    var shouldShowLoading by remember { mutableStateOf(true) }

    // This variable updates when isLoading changes
    LaunchedEffect(isLoading) {
        shouldShowLoading = isLoading
    }

    // Display the loading screen or the user data screen
    if (shouldShowLoading) {
        viewModel.isLogged()
        LoadingIndicator()
    } else {
        ScreenView(viewModel, userData)
    }
}

// User data screen
@Composable
private fun ScreenView(viewModel: SessionViewModel, userData: Session?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopCenter)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Personal data",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
        Avatar(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            userData?.name?.first().toString().uppercase()
        )
        Text(
            text = "Username: ${userData?.name}",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        Text(
            text = "Email: ${userData?.email}",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            onClick = { viewModel.closeSession() }) {
            Text(text = "Close Session")
        }
    }
}

// Generates the user's avatar
@Composable
private fun Avatar(modifier: Modifier, letter: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = modifier.size(128.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                fontSize = 64.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}