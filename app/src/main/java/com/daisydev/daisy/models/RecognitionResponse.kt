package com.daisydev.daisy.models

data class AltName(
    val name: String
)

// Class representing the server response when sending an image for recognition
data class DataPlant(
    val plant_name: String,
    val probability: Double,
    val alt_names: List<AltName>,
)