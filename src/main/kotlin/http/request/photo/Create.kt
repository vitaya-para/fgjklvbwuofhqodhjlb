package com.gallery.http.request.photo
import kotlinx.serialization.Serializable

@Serializable
data class Create(
    val album_uuid: String
)