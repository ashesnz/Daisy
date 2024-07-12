@file:OptIn(ExperimentalPermissionsApi::class)

package com.daisydev.daisy.ui.compose.recognition

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.daisydev.daisy.R
import com.daisydev.daisy.ui.components.LoadingIndicator
import com.daisydev.daisy.ui.feature.recognition.RecognitionViewModel
import com.daisydev.daisy.ui.navigation.NavRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope

/**
 * Screen for the recognition section.
 *
 * @param navController Navigation controller.
 * @param snackbarHostState Snackbar state.
 * @param viewModel ViewModel for the recognition section.
 */
@Composable
fun CamaraScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    viewModel: RecognitionViewModel = hiltViewModel()
) {
    // For session
    val isUserLogged by viewModel.isUserLogged.observeAsState(true)
    val isSessionLoading by viewModel.isSessionLoading.observeAsState(true)

    // For plant recognition
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // If there is no session, redirect to the access screen
    if (!isUserLogged) {
        LaunchedEffect(Unit) {
            navController.navigate(NavRoute.Access.path) {
                popUpTo(NavRoute.Session.path) { inclusive = true }
            }
        }
    } else {
        ShowLoadingOrScreen(
            context,
            navController,
            snackbarHostState,
            cameraPermissionState,
            scope,
            viewModel,
            isSessionLoading
        )
    }
}

@Composable
private fun ShowLoadingOrScreen(
    context: Context,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    cameraPermissionState: PermissionState,
    scope: CoroutineScope,
    viewModel: RecognitionViewModel,
    isSessionLoading: Boolean
) {

    // Variable to show loading
    var shouldShowLoading by remember { mutableStateOf(true) }

    // This variable updates when isSessionLoading changes
    LaunchedEffect(isSessionLoading) {
        shouldShowLoading = isSessionLoading
    }

    // Show loading or recognition screen
    if (shouldShowLoading) {
        viewModel.isLogged()
        LoadingIndicator()
    } else {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            PermissionRequired(
                context,
                navController,
                cameraPermissionState,
                context.packageName,
                Modifier.align(Alignment.Center),
                viewModel,
                snackbarHostState,
                scope
            )
        }
    }
}

/**
 * Function that builds the camera interface, requests the necessary permissions, and
 * displays the corresponding error messages.
 *
 * @param navController Navigation controller.
 * @param context Application context.
 * @param viewModel ViewModel for the recognition section.
 * @param snackbarHostState Snackbar state.
 * @param scope Coroutine scope.
 */
@Composable
private fun PermissionRequired(
    context: Context,
    navController: NavController,
    cameraPermissionState: PermissionState,
    packageName: String,
    modifier: Modifier,
    viewModel: RecognitionViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val openAppSettingsLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {}

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            Column(
                modifier = modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_daisy_no_bg),
                    contentDescription = "Header Image",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(70.dp)
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = stringResource(R.string.camera_permission_info_0),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    Button(onClick = {
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text(stringResource(R.string.camera_permission_grantbutton_0))
                    }
                }
            }
        },
        permissionNotAvailableContent = {
            Column(
                modifier = modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_daisy_no_bg),
                    contentDescription = "Header Image",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(70.dp)
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = stringResource(R.string.camera_permission_info_1),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        openAppSettingsLauncher.launch(intent)
                    }) {
                        Text(stringResource(R.string.camera_permission_grantbutton_1))
                    }
                }
            }
        }
    ) {
        BuildCameraUI(
            navController = navController,
            context = context,
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            scope = scope
        )
    }
}