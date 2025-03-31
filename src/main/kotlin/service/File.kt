package com.gallery.service

import io.ktor.http.content.*
import java.io.File as RawFile
import io.ktor.utils.io.jvm.javaio.toInputStream


class File(private val baseUrl: String) {

    suspend fun get(id: String): RawFile? {
        val file = RawFile("uploads/$id")
        return if (file.exists()) file else null
    }

    suspend fun create(id: String, multipart: MultiPartData): String {
        var fullName = id
        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                fullName += "-" + part.originalFileName
                val file = RawFile("uploads/$fullName")

                file.outputStream().buffered().use { output ->
                    part.provider().toInputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }
            part.dispose()
        }

        return "$baseUrl/photos/$fullName"
    }

}