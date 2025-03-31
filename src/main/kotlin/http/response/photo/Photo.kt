package com.gallery.http.response.photo
import kotlinx.serialization.Serializable

@Serializable
data class Photo (
    val uuid: String,
    val url: String,
)