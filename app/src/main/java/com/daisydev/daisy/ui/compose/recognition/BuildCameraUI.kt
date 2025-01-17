package com.daisydev.daisy.ui.compose.recognition

import android.content.Context
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.daisydev.daisy.R
import com.daisydev.daisy.models.DataPlant
import com.daisydev.daisy.ui.components.LoadingIndicator
import com.daisydev.daisy.ui.feature.recognition.RecognitionViewModel
import com.daisydev.daisy.ui.theme.md_theme_light_onPrimary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

/**
 * Creates the user interface for the camera screen
 * It is responsible for displaying the camera preview, the button to take the photo, and the button to
 * return to the previous screen
 *
 * @param navController
 * @param context
 * @param viewModel
 * @param snackbarHostState
 * @param scope
 */
@Composable
fun BuildCameraUI(
    navController: NavController,
    context: Context,
    viewModel: RecognitionViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val showResponse by viewModel.showResponse.observeAsState(initial = false)
    val response by viewModel.response.observeAsState()
    val imageConverted by viewModel.imageConverted.observeAsState()

    Box(Modifier.fillMaxSize()) {
        if (showResponse) {
            ResponseView(
                response = response,
                image = imageConverted!!,
                viewModel = viewModel
            )
        } else {
            val executor = remember(context) { ContextCompat.getMainExecutor(context) }
            val imageCapture: MutableState<ImageCapture?> = remember { mutableStateOf(null) }

            // Camera preview
            CameraView(
                modifier = Modifier.fillMaxSize(),
                imageCapture = imageCapture,
                executor = executor,
                context = context
            )

            // Loading indicator when image is being processed
            if (isLoading) {
                LoadingIndicator(backgroundColor = md_theme_light_onPrimary)
            }

            // Button for return to last screen
            Box(Modifier.padding(10.dp)) {
                IconButton(
                    enabled = !isLoading,
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_back),
                        contentDescription = "Regresar a pantalla anterior",
                        tint = Color.White,
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
            }

            // Button for take the photo and process it
            Button(
                enabled = !isLoading,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                onClick = {
                    imageCapture.value?.takePicture(
                        executor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                viewModel.analyzeImage(context, image, snackbarHostState)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = exception.message ?: "Error al tomar la foto",
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        })
                }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_search),
                        contentDescription = "Recognition",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp), text = "Escanear"
                    )
                }
            }
        }
    }
}

/**
 * Creates the camera view
 *
 * @param modifier View modifier
 * @param imageCapture Captured image by the camera
 * @param executor Executor for the camera
 * @param context Application context
 */
@Composable
private fun CameraView(
    modifier: Modifier,
    imageCapture: MutableState<ImageCapture?>,
    executor: Executor,
    context: Context
) {
    val previewCameraView = remember { PreviewView(context) }
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }
    var cameraSelector: CameraSelector? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(modifier = modifier, factory = {
        cameraProviderFuture.addListener(
            {
                cameraSelector =
                    CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                imageCapture.value = ImageCapture.Builder().build()

                cameraProvider.unbindAll()

                val prev = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewCameraView.surfaceProvider)
                }

                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector as CameraSelector, imageCapture.value, prev
                )
            }, executor
        )
        previewCameraView
    })
}

/**
 * Creates the response view for recognition
 * Displays the taken image, the list of recognized plants, and the button to return to the previous screen
 *
 * @param response
 * @param image
 * @param viewModel
 */
@Composable
fun ResponseView(
    response: List<DataPlant>?,
    viewModel: RecognitionViewModel,
    image: java.io.File
) {
    if (response != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(fraction = 0.9f)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .height(300.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            bitmap = BitmapFactory.decodeFile(image.absolutePath).asImageBitmap(),
                            contentDescription = "Scanned image",
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                item {
                    Column(
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Recognition results",
                            style = MaterialTheme.typography.displaySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Possible recognized plants:",
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (response.isEmpty()) {
                            Text(
                                text = "No matches found",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            response.forEachIndexed { index, plant ->
                                ListItem(
                                    colors = ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        headlineColor = MaterialTheme.colorScheme.onSurface,
                                        leadingIconColor = MaterialTheme.colorScheme.onSurface,
                                        supportingColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    headlineContent = {
                                        Text(text = plant.plant_name)
                                    },
                                    leadingContent = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_plant),
                                            contentDescription = "Plant"
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = "Probability: ${
                                                String.format(
                                                    "%.2f",
                                                    plant.probability * 100
                                                )
                                            }%"
                                        )
                                        if (plant.alt_names.isNotEmpty()) {
                                            Text(
                                                text = "Alternative names: ${
                                                    plant.alt_names.map { altName ->
                                                        altName.name
                                                    }
                                                }"
                                            )
                                        }
                                    }
                                )

                                // If it is not the last element, add a divider
                                if (index != response.size - 1) {
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = { viewModel.hideResponse() },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
            ) {
                Text(text = "Scan again")
            }
        }
    }
}