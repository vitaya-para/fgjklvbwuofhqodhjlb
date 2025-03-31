package com.gallery.http.response.photo
import kotlinx.serialization.Serializable

@Serializable
data class PhotoTiny (
    val uuid: String,
)