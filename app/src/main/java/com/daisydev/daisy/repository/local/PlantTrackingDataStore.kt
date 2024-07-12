package com.daisydev.daisy.ui.compose.tracking

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.dataStore by preferencesDataStore("plant_tracking_data_store")

class PlantTrackingDataStore(context: Context) {
    private val dataStore = context.dataStore

    val plants: Flow<List<Message>> = dataStore.data
        .catch { exception ->
            // Handle read/write exceptions.
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Get the list of plant messages from preferences
            val plantsJson = preferences[PreferencesKeys.PLANTS] ?: "[]"
            val plantsArray = JSONArray(plantsJson)
            val plantMessages = mutableListOf<Message>()
            for (i in 0 until plantsArray.length()) {
                val plantObject = plantsArray.getJSONObject(i)
                val name = plantObject.getString("name")?: ""
                val healingProperties = plantObject.getString("climate")?: ""
                val url = plantObject.getString("url")?: ""
                val care = plantObject.getString("care")?: ""
                val nameC = plantObject.getString("nameC")?: ""
                val message = Message(name, healingProperties,url, nameC, care)
                plantMessages.add(message)
            }
            plantMessages
        }

    suspend fun saveplants(plants: List<Message>) {
        val plantsJson = JSONArray().apply {
            for (plant in plants) {
                val plantObject = JSONObject().apply {
                    put("name", plant.name)
                    put("climate", plant.body)
                    put("url", plant.url)
                    put("nameC", plant.nameC)
                    put("care", plant.care)
                }
                put(plantObject)
            }
        }.toString()

        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLANTS] = plantsJson
        }
    }

    private object PreferencesKeys {
        val PLANTS = stringPreferencesKey("plants")
    }
}