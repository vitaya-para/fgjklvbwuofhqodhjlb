package com.gallery.http.response.album
import kotlinx.serialization.Serializable

@Serializable
data class AlbumTiny (
    val uuid: String,
    val title: String,
)