package com.gallery.service

import com.gallery.dto.Photo
import java.util.*

class Photo(private val db: Database, private val user: Int?) {

    suspend fun get(uuid: String): Photo? {
        return db.query("SELECT uuid, url FROM photos WHERE uuid = ? AND user_id = ?", UUID.fromString(uuid), user ?: 0) { ps ->
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    Photo(
                        uuid = UUID.fromString(rs.getString("uuid")),
                        url = rs.getString("url"),
                    )
                } else null
            }
        }
    }

    suspend fun getList(albumId: String): List<Photo> {
        return db.query("SELECT uuid, url FROM photos WHERE user_id = ? AND album_id = ? AND url != '' ", user ?: 0, UUID.fromString(albumId)) { ps ->
            ps.executeQuery().use { rs ->
                val albums = mutableListOf<Photo>()
                while (rs.next()) {
                    albums.add(
                        Photo(
                            uuid = UUID.fromString(rs.getString("uuid")),
                            url = rs.getString("url")
                        )
                    )
                }
                albums
            }
        }
    }

    suspend fun create(albumId: String): String {
        val uuid = UUID.randomUUID()
        db.update(
            "INSERT INTO photos (uuid, url, album_id, user_id) VALUES (?, ?, ?, ?)",
            uuid,"", UUID.fromString(albumId), user ?: 0
        ) {}
        return uuid.toString()
    }

    suspend fun update(photo: Photo) {
        db.update(
            "UPDATE photos SET uuid = ?, url = ? WHERE uuid = ? AND user_id = ?",
            photo.uuid, photo.url, photo.uuid, user ?: 0
        ) {}
    }

    suspend fun delete(uuid: String) {
        db.update(
            "DELETE FROM photos WHERE uuid = ? AND user_id = ?",
            UUID.fromString(uuid), user ?: 0
        ) {}
    }
}