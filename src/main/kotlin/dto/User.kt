package com.gallery.dto

data class User(
    val id: Int,
    val login: String,
    val passwordHash: String,
    val isActive: Boolean
)