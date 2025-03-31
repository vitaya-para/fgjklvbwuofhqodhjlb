package com.gallery.http.response.album

import kotlinx.serialization.Serializable
import kotlin.collections.List as IList

@Serializable
data class List(
    val items: IList<Album>,
    val count: Int
)