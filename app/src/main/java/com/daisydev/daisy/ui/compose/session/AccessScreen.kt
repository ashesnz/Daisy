package com.daisydev.daisy.ui.compose.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.daisydev.daisy.R
import com.daisydev.daisy.ui.navigation.NavRoute

// Access screen that redirects to the registration or login screen
@Composable
fun AccessScreen(navController: NavController) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Access(Modifier.align(Alignment.Center), navController)
    }
}

// Content of the access screen
@Composable
private fun Access(modifier: Modifier, navController: NavController) {
    Column(modifier = modifier) {
        AccessHeader(Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.padding(26.dp))
        GoRegisterButton(navController)
        Spacer(modifier = Modifier.padding(10.dp))
        GoLoginButton(navController)
    }
}

// Button to go to the registration screen
@Composable
private fun GoRegisterButton(navController: NavController) {
    Button(
        onClick = {
            navController.navigate(NavRoute.Register.path) {
                popUpTo(NavRoute.Access.path) { inclusive = true }
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = "Register")
    }
}

// Button to go to the login screen
@Composable
private fun GoLoginButton(navController: NavController) {
    OutlinedButton(
        onClick = {
            navController.navigate(NavRoute.Login.path) {
                popUpTo(NavRoute.Access.path) { inclusive = true }
            }
        },
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = "Log in")
    }
}

// Header of the access screen
@Composable
private fun AccessHeader(modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_daisy_no_bg),
            contentDescription = "Daisy Logo",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(100.dp)

        )
        Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = "Daisy",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = "To enter this section, you must log in",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 26.dp)
        )
    }
}
