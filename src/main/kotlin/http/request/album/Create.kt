package com.gallery.http.request.album
import kotlinx.serialization.Serializable

@Serializable
data class Create(
    val title: String,
)