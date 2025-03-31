package com.gallery.http.request

import kotlinx.serialization.Serializable

@Serializable
data class CredentialsRequest(
    val login: String,
    val password: String
)
