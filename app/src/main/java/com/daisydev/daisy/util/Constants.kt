package com.daisydev.daisy.util

// Application constants
data class Constants(
    val baseUrl: String = "https://cloud.appwrite.io/v1",
    val projectId: String = "env-project-key"
)

// Application constants (for use in other files)
object ExportConstants {
    const val baseUrl: String = "https://cloud.appwrite.io/v1"
    const val projectId: String = "env-project-key"
}