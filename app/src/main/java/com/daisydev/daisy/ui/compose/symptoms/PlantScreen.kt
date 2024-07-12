package com.daisydev.daisy.ui.compose.symptoms

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantScreen(
    navController: NavController,
    name: String,
    scientificName: String,
    body: String,
    uses: String,
    url: String
) {
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
                        // When the button is pressed, it goes to the symptoms screen
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
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 75.dp, start = 10.dp, end = 10.dp, bottom = 10.dp)

            ) {
                Box(
                    Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)

                ) {
                    Column() {
                        LazyColumn() {
                            item {

                                Text(
                                    text = "Scientific name: ${scientificName}",
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            item {
                                Text(
                                    text = "Common name: ${name}",
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.titleMedium
                                )

                            }
                            item {
                                // Image from internet
                                Image(
                                    painter = rememberAsyncImagePainter(url),
                                    contentDescription = scientificName,
                                    modifier = Modifier
                                        // Set image size
                                        .size(400.dp)
                                        .fillMaxHeight(1f),
                                )
                            }
                            item {
                                Text(
                                    "Healing properties:",
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            item {
                                Text(
                                    text = "${body}",
                                    textAlign = TextAlign.Start
                                )
                            }
                            item {
                                Text(
                                    text = "Uses:",
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            item {
                                Text(
                                    text = "${uses}",
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            }
        }
    )

}