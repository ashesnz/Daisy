package com.daisydev.daisy.repository.remote

import android.content.Context
import android.net.Uri
import com.daisydev.daisy.models.AltName
import com.daisydev.daisy.models.BlogDocumentModel
import com.daisydev.daisy.models.BlogEntry
import com.daisydev.daisy.models.DataPlant
import com.daisydev.daisy.models.toBlogEntry
import com.daisydev.daisy.util.getFilenameFromUri
import com.daisydev.daisy.util.removeAccents
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.extensions.toJson
import io.appwrite.models.File
import io.appwrite.models.InputFile
import io.appwrite.models.Session
import io.appwrite.models.User
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Functions
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for all operations related to AppWrite.
 */
@Singleton
class AppWriteRepository @Inject constructor(
    private val context: Context,
    private val client: Client,
    private val dispatcher: CoroutineDispatcher
) {
    // -- Services --

    private val account = Account(client)
    private val databases = Databases(client)
    private val storage = Storage(client)
    private val functions = Functions(client)

    // -- Account --

    // Log in with email and password
    suspend fun login(email: String, password: String): Session {
        return withContext(dispatcher) {
            account.createEmailSession(email, password)
        }
    }

    // Register user with username, email, and password
    suspend fun register(
        email: String, password: String,
        name: String
    ): User<Map<String, Any>> {
        return withContext(dispatcher) {
            account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )
        }
    }

    // To log out
    suspend fun logout() {
        return withContext(dispatcher) {
            account.deleteSession("current")
        }
    }

    // To get user information
    suspend fun getAccount(): User<Map<String, Any>> {
        return withContext(dispatcher) {
            account.get()
        }
    }

    // To get the current session
    suspend fun isLoggedIn(): Session {
        return withContext(dispatcher) {
            account.getSession("current")
        }
    }

    // -- Storage --

    // To upload an image to AppWrite
    suspend fun uploadImage(image: java.io.File): File {
        return withContext(dispatcher) {
            storage.createFile(
                bucketId = "bucket-id",
                fileId = ID.unique(),
                file = InputFile.fromFile(image),
            )
        }
    }

    // To upload a blog image to AppWrite
    suspend fun uploadBlogImage(imageUri: Uri): File {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                val mimeType = context.contentResolver.getType(imageUri)
                val filename = getFilenameFromUri(context, imageUri)

                storage.createFile(
                    bucketId = "bucket-id",
                    fileId = ID.unique(),
                    file = InputFile.fromBytes(
                        bytes = stream.readBytes(),
                        filename = filename!!,
                        mimeType = mimeType!!
                    ),
                )
            } ?: throw IOException("Failed to open InputStream for imageUri: $imageUri")
        }
    }

    suspend fun deleteBlogImage(imageId: String) {
        return withContext(dispatcher) {
            storage.deleteFile(
                bucketId = "bucket-id",
                fileId = imageId
            )
        }
    }

    // -- Functions --

    // To recognize an image through Google Cloud Vision and GPT-3.5
    suspend fun recognizeImage(imageId: String): List<DataPlant> {
        return withContext(Dispatchers.IO) {
            // Execute the function
            val execution = functions.createExecution(
                functionId = "function-id",
                data = mapOf(
                    "image" to imageId
                ).toJson(),
                async = true
            )

            // Wait for the function to finish executing
            var executionResult = functions.getExecution(
                functionId = "function-id",
                executionId = execution.id
            )

            while (executionResult.status != "completed" && executionResult.status != "failed") {
                Thread.sleep(1000)
                executionResult = functions.getExecution(
                    functionId = "function-id",
                    executionId = execution.id
                )
            }

            // Get the function result
            val jsonString = executionResult.response

            // Variable to store the result
            var result = listOf<DataPlant>()

            // Convert the result to a list of DataPlant
            val jsonArray = JSONArray(jsonString)

            // If the result is not empty
            if (jsonArray.length() > 0) {
                val arrRange = 0 until jsonArray.length()

                // Convert the result to a list of DataPlant
                result = arrRange.map { i ->
                    val jsonObject = jsonArray.getJSONObject(i)

                    val altNamesArr = jsonObject.getJSONArray("alt_names")
                    val altNamesRange = 0 until altNamesArr.length()

                    val altNames: List<AltName> = altNamesRange.map {
                        val alt_name = altNamesArr.getJSONObject(it)
                        AltName(
                            name = alt_name.getString("name")
                        )
                    }

                    DataPlant(
                        plant_name = jsonObject.getString("plant_name"),
                        probability = jsonObject.getDouble("probability"),
                        alt_names = altNames
                    )
                }
            }

            // Return the result
            result
        }
    }

    // -- Databases --

    // To list all documents in the AppWrite database
    suspend fun listDocuments(): MutableList<BlogEntry> {
        return withContext(dispatcher) {
            databases.listDocuments(
                "database-id",
                "collection-id"
            ).documents.map {
                toBlogEntry(it)
            }.toMutableList()
        }
    }

    // To list the documents that meet the given filter
    suspend fun listDocumentsWithFilter(filter: String): MutableList<BlogEntry> {
        // Clean the string with the keywords and split it using spaces as delimiter
        val keywords = filter.trim().split(" ").map { removeAccents(it.lowercase()) }

        return withContext(dispatcher) {
            databases.listDocuments(
                databaseId = "database-id",
                collectionId = "collection-id"
            ).documents.map {
                toBlogEntry(it)
            }.filter { blogEntry ->

                // convert everything to lowercase and clean it
                val plants = blogEntry.plants.map { removeAccents(it.lowercase().trim()) }
                val symptoms = blogEntry.symptoms.map { removeAccents(it.lowercase().trim()) }

                // If there is at least one match
                keywords.any { keyword ->
                    plants.any { it.contains(keyword) }
                } || keywords.any { keyword ->
                    symptoms.any { it.contains(keyword) }
                }
            }.toMutableList()
        }
    }

    // To list the documents of the logged-in user
    suspend fun listDocumentsOfUser(userId: String): MutableList<BlogEntry> {
        return withContext(dispatcher) {
            databases.listDocuments(
                databaseId = "database-id",
                collectionId = "collection-id",
                queries = listOf(
                    Query.equal("id_user", listOf(userId))
                )
            ).documents.map {
                toBlogEntry(it)
            }.toMutableList()
        }
    }

    // To create a document in the AppWrite database
    suspend fun createDocument(documentModel: BlogDocumentModel) {
        return withContext(dispatcher) {
            databases.createDocument(
                databaseId = "database-id",
                collectionId = "collection-id",
                documentId = ID.unique(),
                data = documentModel.toJson()
            )
        }
    }

    suspend fun updateDocument(docId: String, documentModel: BlogDocumentModel) {
        return withContext(dispatcher) {
            databases.updateDocument(
                databaseId = "database-id",
                collectionId = "collection-id",
                documentId = docId,
                data = documentModel.toJson()
            )
        }
    }

    suspend fun deleteDocument(docId: String) {
        return withContext(dispatcher) {
            databases.deleteDocument(
                databaseId = "database-id",
                collectionId = "collection-id",
                documentId = docId
            )
        }
    }
}