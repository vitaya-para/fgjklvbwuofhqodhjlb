package com.gallery.service

import java.util.*
import com.gallery.dto.Album as AlbumDto

class Album(private val db: Database, private val user: Int?) {

    suspend fun get(uuid: String): AlbumDto? {
        return db.query("SELECT uuid, title, cover_url FROM albums WHERE uuid = ? AND user_id = ?", UUID.fromString(uuid), user ?: 0) { ps ->
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    AlbumDto(
                        uuid = UUID.fromString(rs.getString("uuid")),
                        title = rs.getString("title"),
                        coverUrl = rs.getString("cover_url")
                    )
                } else null
            }
        }
    }

    suspend fun getList(): List<AlbumDto> {
        return db.query("SELECT uuid, title, cover_url FROM albums WHERE user_id = ?", user ?: 0) { ps ->
            ps.executeQuery().use { rs ->
                val albums = mutableListOf<AlbumDto>()
                while (rs.next()) {
                    albums.add(
                        AlbumDto(
                            uuid = UUID.fromString(rs.getString("uuid")),
                            title = rs.getString("title"),
                            coverUrl = rs.getString("cover_url")
                        )
                    )
                }
                albums
            }
        }
    }

    suspend fun create(album: AlbumDto): String {
        val uuid = UUID.randomUUID()
        db.update(
            "INSERT INTO albums (uuid, title, cover_url, user_id) VALUES (?, ?, ?, ?)",
            uuid, album.title, album.coverUrl ?: "", user ?: 0
        ) {}
        return uuid.toString()
    }

    suspend fun update(album: AlbumDto) {
        db.update(
            "UPDATE albums SET title = ?, cover_url = ? WHERE uuid = ? AND user_id = ?",
            album.title, album.coverUrl ?: "", album.uuid ?: throw IllegalArgumentException(), user ?: 0
        ) {}
    }

    suspend fun delete(uuid: String) {
        db.update(
            "DELETE FROM albums WHERE uuid = ? AND user_id = ?",
            UUID.fromString(uuid), user ?: 0
        ) {}
    }

    suspend fun existUrl(url: String): Boolean {
        return db.query("SELECT 1 FROM albums WHERE user_id = ? AND cover_url = ?", user ?: 0, url) { ps ->
            ps.executeQuery().use { rs ->  rs.next()}
        }
    }
}