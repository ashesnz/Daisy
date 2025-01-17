package com.daisydev.daisy.ui.compose.tracking

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.daisydev.daisy.ui.navigation.NavRoute
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.DatePicker
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import com.daisydev.daisy.R
import java.time.LocalDate

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantTracking(
    navController: NavController,
    name: String,
    nameC: String,
    body: String,
    care: List<String>,
    url: String
) {
    val context = LocalContext.current

    // Function to show a notification
    fun showNotification(title: String, content: String) {
        val channelId = "80"
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Display the notification
        notificationManager.notify(1, builder.build())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Information",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // When the button is pressed, navigate to the symptoms screen
                        navController.navigate(NavRoute.Symptoms.path)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = {
            Card(
                onClick = { /* Do something */ },
                modifier = Modifier
                    .size(width = 400.dp, height = 740.dp)
                    .padding(start = 15.dp, top = 75.dp),
            ) {
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(Modifier.padding(8.dp)) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                val showDialog = remember { mutableStateOf(false) }
                                val titleText = remember { mutableStateOf("Reminder title") }
                                val showDatePicker = remember { mutableStateOf(false) }
                                val selectedDate = remember { mutableStateOf(LocalDate.now()) }
                                val titleDescriptionText = remember { mutableStateOf("Reminder description") }

                                Text(text = "Common name: $name", modifier = Modifier.weight(1f))
                                Button(
                                    onClick = { showDialog.value = true }
                                ) {
                                    Text(text = "Reminder")
                                }

                                if (showDialog.value) {
                                    AlertDialog(
                                        onDismissRequest = { showDialog.value = false },
                                        properties = DialogProperties(
                                            dismissOnClickOutside = false // Optional: to prevent closing the dialog by clicking outside of it
                                        ),
                                        title = {
                                            Column {
                                                TextField(
                                                    value = titleText.value,
                                                    onValueChange = { titleText.value = it },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                TextField(
                                                    value = titleDescriptionText.value,
                                                    onValueChange = { titleDescriptionText.value = it },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = { showDialog.value = false
                                                    // Call the function showNotification to display the notification
                                                    showNotification(titleText.value, titleDescriptionText.value)
                                                },
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                    //backgroundColor = MaterialTheme.colorScheme.background
                                                )
                                            ) {
                                                Text(text = "Save")
                                            }
                                        },
                                        dismissButton = {
                                            Button(
                                                onClick = { showDialog.value = false },
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                    //backgroundColor = MaterialTheme.colorScheme.background
                                                )
                                            ) {
                                                Text(text = "Cancel")
                                            }
                                        }
                                    )
                                }

                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Scientific name: $nameC",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = nameC,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Climate:",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(text = body, textAlign = TextAlign.Justify)
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Care:",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }

                        care.forEach { careItem ->
                            item {
                                Text(text = careItem, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    )

}
