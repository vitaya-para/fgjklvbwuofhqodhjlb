package com.gallery.dto

import java.util.UUID

data class Album (
    val uuid: UUID?,
    var title: String,
    val coverUrl: String = "",
)