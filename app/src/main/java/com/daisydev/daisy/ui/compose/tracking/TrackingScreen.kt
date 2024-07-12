package com.daisydev.daisy.ui.compose.tracking


import android.graphics.Rect
import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
//OpenAI
import androidx.compose.runtime.rememberCoroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import okhttp3.Headers.Companion.toHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import java.util.Properties
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.daisydev.daisy.ui.feature.tracking.TrackingViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.first
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class Message(
    val name: String,
    val body: String,
    val url: String,
    val nameC: String,
    val care: String
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TrackingScreen(navController: NavController) {
    val viewModel = viewModel<TrackingViewModel>()
    val searchText by viewModel.searchText.collectAsState()
    val plants by viewModel.care.collectAsState()
    //val isSearching by viewModel.isSearching.collectAsState()
    val sampleData by viewModel.sampleData.observeAsState(emptyArray())
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardOpen by keyboardAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val plantsDataStore = PlantTrackingDataStore(context)
    // Search section
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopCenter)
    ) {
        Text(
            text = "Tracking",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Search section
        Box(
            modifier = Modifier
                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                .width(350.dp)
                .wrapContentSize(Alignment.TopCenter)
                .align(Alignment.CenterHorizontally)
                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
        ) {
            TextField(
                value = searchText,
                onValueChange = viewModel::onSearchTextChange,
                textStyle = TextStyle(fontSize = 17.sp),
                leadingIcon = {
                    IconButton(
                        onClick = {
                            //Asynchronously call the search function
                            coroutineScope.launch(Dispatchers.IO) {
                                search(searchText, context) { plantMessages ->
                                    coroutineScope.launch(Dispatchers.Main) {
                                        viewModel.setSampleData(plantMessages)
                                    }
                                }
                            }
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(Icons.Filled.Search, null, tint = Color.Gray)
                    }
                },
                modifier = Modifier
                    .padding(11.dp)
                    .background(Color.White, CircleShape)
                    .fillMaxWidth(),
                placeholder = { Text(text = "Look for plants") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search, keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    coroutineScope.launch(Dispatchers.IO) {
                        search(searchText, context) { plantMessages ->
                            coroutineScope.launch(Dispatchers.Main) {
                                viewModel.setSampleData(plantMessages)
                            }
                        }
                    }
                }),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Transparent,
                    containerColor = MaterialTheme.colorScheme.outlineVariant
                ),
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Display the list of plants while searching
        if (isKeyboardOpen == Keyboard.Closed) {
            LaunchedEffect(Unit) {
                // Load plants from DataStore
                val plantMessages = plantsDataStore.plants.first()
                if (plantMessages.isEmpty()) {
                    // If plants not available in DataStore, fetch from API and save to DataStore
                    coroutineScope.launch(Dispatchers.IO) {
                        getCommonPlants(context) { plantMessages ->
                            coroutineScope.launch(Dispatchers.Main) {
                                viewModel.setSampleData(plantMessages)
                                // Save plants to DataStore
                                plantsDataStore.saveplants(plantMessages.toList())
                            }
                        }
                    }
                } else {
                    // If plants available in DataStore, update UI
                    viewModel.setSampleData(plantMessages.toTypedArray())
                }
            }
            // Section of most common symptoms
            Text(
                text = "Most common plants",
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            //List of common items
            Conversation(messages = sampleData, navController)
            Spacer(modifier = Modifier.height(-16.dp))
        }
        // When the search is performed, it shows the list of plants
        if (isKeyboardOpen == Keyboard.Opened) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Display the list of symptoms
                items(plants) { plant ->
                    Text(
                        // When an option is pressed, the search is performed and the keyboard is hidden
                        text = "${plant.plant}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clickable {
                                keyboardController?.hide()
                                coroutineScope.launch(Dispatchers.IO) {
                                    search("${plant.plant}", context) { plantMessages ->
                                        coroutineScope.launch(Dispatchers.Main) {
                                            viewModel.setSampleData(plantMessages)
                                        }
                                    }
                                }
                            },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

enum class Keyboard {
    Opened, Closed
}

@Composable
fun keyboardAsState(): State<Keyboard> {
    val keyboardState = remember { mutableStateOf(Keyboard.Closed) }
    val view = LocalView.current
    DisposableEffect(view) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = if (keypadHeight > screenHeight * 0.15) {
                Keyboard.Opened
            } else {
                Keyboard.Closed
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }
    return keyboardState
}

suspend fun search(value: String, context: Context, onComplete: (Array<Message>) -> Unit) {
    try {
        val properties = Properties()
        val inputStream = context.assets.open("api.properties")
        properties.load(inputStream)
        val apiKey = properties.getProperty("API_KEY") //Retrieve the API KEY from api.properties
        val endpoint = "https://api.openai.com"
        val prompt =
            "Just as an example, I need a list of care for the plant $value, give me the response in JSON following the format idea that will contain the plants:" +
                    "{\"plants\": [{\"name\": \"plant name\", \"scientific_name\": \"scientific name of the plant\", " +
                    "\"care\": [{\"plant care 1\": \"care 1\",\"plant care 2\": \"care 2\",\"plant care 3\": \"care 3\",\"plant care 4\": \"care 4\",}], " +
                    "\"climate\": \"climate of the plant\"}, {\"the same here for the next plant and so on\"}]}"

        val maxTokens = 800 //Maximum allowed characters

        val url = "$endpoint/v1/chat/completions"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer $apiKey"
        )
        val requestBody = JSONObject()
            .put(
                "messages",
                JSONArray().put(JSONObject().put("role", "system").put("content", prompt))
            )
            .put("max_tokens", maxTokens)
            .put("model", "gpt-3.5-turbo")
            .toString()
        val mediaType = "application/json".toMediaType()
        val request = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())
            .post(requestBody.toRequestBody(mediaType))
            .build()

        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(40, TimeUnit.SECONDS) //Set the connection timeout to 30 seconds
                .readTimeout(40, TimeUnit.SECONDS) // Set the read timeout to 30 seconds
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    // Process and handle the list of healing plants
                    val responseJson = JSONObject(responseData)
                    val choicesArray = responseJson.getJSONArray("choices")

                    if (choicesArray.length() > 0) {
                        val messageObject = choicesArray.getJSONObject(0).getJSONObject("message")
                        val content = messageObject.getString("content")

                        // Extract the part of the JSON that contains the plants
                        val startIndex = content.indexOf('[')
                        val endIndex = content.lastIndexOf(']')
                        val plantsJson = content.substring(startIndex, endIndex + 1)

                        val plantsArray = JSONArray(plantsJson)

                        val plantMessages = mutableListOf<Message>()

                        for (i in 0 until plantsArray.length()) {
                            val plantObject = plantsArray.getJSONObject(i)
                            val name = plantObject.getString("name")
                            val scientificName = plantObject.getString("scientific_name")
                            val careJSON = plantObject.getJSONArray("care")
                            val climate = plantObject.getString("climate")
                            val url = get_URL(context, scientificName)
                            val careList = mutableListOf<String>()

                            for (j in 0 until careJSON.length()) {
                                val careObject = careJSON.getJSONObject(j)
                                val careKey = careObject.keys().next()
                                val careValue = careObject.getString(careKey)
                                val care = "$careKey: $careValue"
                                careList.add(care)
                            }

                            val message = Message(
                                name, "$climate", "$url", "$scientificName", careList.joinToString("\n")
                            )
                            plantMessages.add(message)
                        }

                        // Call the onComplete function and pass the list of plantMessages
                        onComplete(plantMessages.toTypedArray())
                    } else {
                        // Handle the case when no options are available
                        Log.e("TAG", "No options found in the response")
                    }
                } else {
                    // Handle the case when the response is not successful
                    val statusCode = response.code
                    val errorBody = response.body?.string()
                    Log.e("TAG", "Status code: $statusCode")
                    Log.e("TAG", "Error body: $errorBody")
                }
            }
        }
    } catch (e: Exception) {
        // Handle any exceptions that occur during the search
        Log.e("TAG", "Error in the search: ${e.message}", e)
    }
}

suspend fun getCommonPlants(context: Context, onComplete: (Array<Message>) -> Unit) {
    try {
        val properties = Properties()
        val inputStream = context.assets.open("api.properties")
        properties.load(inputStream)
        val apiKey = properties.getProperty("API_KEY")
        val endpoint = "https://api.openai.com"
        val prompt =
            "Just as an example I need the care of common plants, give me the response in JSON following the format idea that will contain the plants:" +
                    "{\"plants\": [{\"name\": \"name of the plant\", \"scientific_name\": \"scientific name of the plant\", " +
                    "\"care\": [{\"care 1 of the plant\": \"care 1\",\"care 2 of the plant\": \"care 2\",\"care 3 of the plant\": \"care 3\",\"care 4 of the plant\": \"care 4\",}], " +
                    "\"climate\": \"climate of the plant\"}, {\"the same here for the next plant and so on\"}]}"

        val maxTokens = 800

        val url = "$endpoint/v1/chat/completions"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer $apiKey"
        )
        val requestBody = JSONObject()
            .put(
                "messages",
                JSONArray().put(JSONObject().put("role", "system").put("content", prompt))
            )
            .put("max_tokens", maxTokens)
            .put("model", "gpt-3.5-turbo")
            .toString()
        val mediaType = "application/json".toMediaType()
        val request = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())
            .post(requestBody.toRequestBody(mediaType))
            .build()

        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(40, TimeUnit.SECONDS) // Set the connection timeout to 30 seconds
                .readTimeout(40, TimeUnit.SECONDS) // Set the read timeout to 30 seconds
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("TAG", "Response: $responseData")
                    // Process and handle the list of healing plants
                    val responseJson = JSONObject(responseData)
                    val choicesArray = responseJson.getJSONArray("choices")

                    if (choicesArray.length() > 0) {
                        val messageObject = choicesArray.getJSONObject(0).getJSONObject("message")
                        val content = messageObject.getString("content")

                        // Extract the part of the JSON that contains the plants
                        val startIndex = content.indexOf('[')
                        val endIndex = content.lastIndexOf(']')
                        val plantsJson = content.substring(startIndex, endIndex + 1)

                        val plantsArray = JSONArray(plantsJson)

                        val plantMessages = mutableListOf<Message>()

                        for (i in 0 until plantsArray.length()) {
                            val plantObject = plantsArray.getJSONObject(i)
                            val name = plantObject.getString("name")
                            val scientificName = plantObject.getString("scientific_name")
                            val careJSON = plantObject.getJSONArray("care")
                            val climate = plantObject.getString("climate")
                            val url = get_URL(context, scientificName)
                            val careList = mutableListOf<String>()

                            for (j in 0 until careJSON.length()) {
                                val careObject = careJSON.getJSONObject(j)
                                val careKey = careObject.keys().next()
                                val careValue = careObject.getString(careKey)
                                val careItem = "$careValue: $careValue"
                                careList.add(careItem)
                            }

                            val message = Message(
                                name, "$climate", "$url", "$scientificName", careList.joinToString("\n")
                            )
                            plantMessages.add(message)
                        }

                        // Call the onComplete function and pass the list of plantMessages
                        onComplete(plantMessages.toTypedArray())
                    } else {
                        // Handle the case when no options are available
                        Log.e("TAG", "No options found in the response")
                    }
                } else {
                    // Handle the case when the response is not successful
                    val statusCode = response.code
                    val errorBody = response.body?.string()
                    Log.e("TAG", "Status code: $statusCode")
                    Log.e("TAG", "Error body: $errorBody")
                }
            }
        }
    } catch (e: Exception) {
        // Handle any exceptions that occur during the search
        Log.e("TAG", "Error in the common plants search: ${e.message}", e)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessageCard(msg: Message, navController: NavController) {
    // Add padding around our message
    Row(
        modifier = Modifier
            .padding(all = 10.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20))
            .fillMaxWidth()
            .clickable {
                // Navigate to the plant care screen
                val encodedUrl = URLEncoder.encode(msg.url, StandardCharsets.UTF_8.toString())
                try {
                    navController.navigate(
                        "plantCare/${msg.name}/${msg.nameC}/${msg.body}/${msg.care}/${encodedUrl}"
                    )
                } catch (e: Exception) {
                    println("Error" + e)
                }
            },
    ) {
        Box(
            modifier = Modifier.padding(start = 20.dp)
        ) {
            Row() {
                // Internet image
                Image(
                    painter = rememberAsyncImagePainter(msg.url),
                    contentDescription = msg.name,
                    modifier = Modifier
                        // Set image size
                        .size(90.dp)
                        // Clip image to be shaped as a circle
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                        .align(Alignment.CenterVertically),
                )
                // Add a horizontal space between the image and the column
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Text(
                        text = msg.name,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    // Add a vertical space between the author and message texts
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = msg.body,
                        modifier = Modifier.padding(all = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }
    }
}

@Composable
fun Conversation(messages: Array<Message>, navController: NavController) {
    LazyColumn(
    ) {
        items(messages) { message ->
            MessageCard(message, navController)
            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}

suspend fun get_URL(context: Context, name: String): String? {
    return try {
        val baseUrl = "https://serpapi.com"
        val searchEndpoint = "google_images"
        val query = "Medicinal plant $name"
        val ijn = "0"
        val properties = Properties()
        val inputStream = context.assets.open("api.properties")
        properties.load(inputStream)
        val apiKey = properties.getProperty("S_API_KEY")

        val url = "$baseUrl/search.json?engine=$searchEndpoint&q=$query&ijn=$ijn&api_key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val gson = Gson()
                    val jsonObject = gson.fromJson(responseData, JsonObject::class.java)

                    val imagesResultsArray = jsonObject.getAsJsonArray("images_results")
                    if (imagesResultsArray.size() > 0) {
                        val firstImageResult = imagesResultsArray[0].asJsonObject
                        val thumbnail = firstImageResult.get("thumbnail").asString
                        return@withContext thumbnail // Return the value of the thumbnail
                    } else {
                        Log.d("TAG", "Thumbnail error")
                        return@withContext null // Return null in case of an error
                    }
                } else {
                    // Handle the case when the response is not successful
                    val statusCode = response.code
                    val errorBody = response.body?.string()
                    Log.e("TAG", "URL status code: $statusCode")
                    Log.e("TAG", "URL error body: $errorBody")
                    return@withContext null // Return null in case of an error
                }
            }
        }
    } catch (e: Exception) {
        // Handle any exceptions that occur during the search
        Log.e("TAG", "Error obtaining URL: ${e.message}", e)
        null // Return null in case of an error
    }
}


