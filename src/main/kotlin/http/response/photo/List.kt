package com.gallery.http.response.photo

import kotlinx.serialization.Serializable
import kotlin.collections.List as IList

@Serializable
data class List(
    val items: IList<Photo>,
    val count: Int
)