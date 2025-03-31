package com.gallery.http.response.album
import kotlinx.serialization.Serializable

@Serializable
data class Album (
    val uuid: String,
    val title: String,
    val cover_url: String,
)