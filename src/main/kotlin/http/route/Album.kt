package com.gallery.http.route

import com.gallery.service.Album
import com.gallery.service.Database
import com.gallery.service.File
import com.gallery.service.Photo
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import com.gallery.dto.Album as AlbumDto
import com.gallery.http.request.album.Create as CreateRequest
import com.gallery.http.request.album.Rename as RenameRequest
import com.gallery.http.response.album.Album as AlbumResponse
import com.gallery.http.response.album.AlbumTiny as AlbumTinyResponse
import com.gallery.http.response.photo.Photo as PhotoResponse
import com.gallery.http.response.photo.List as PhotoListResponse
import com.gallery.http.response.album.List as AlbumListResponse

fun Route.albums(db: Database) {

    authenticate("auth-jwt") {
        route("albums") {
            get {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                val albumService = Album(db, userId)

                val albums = albumService.getList()
                call.respond(
                    HttpStatusCode.OK,
                    AlbumListResponse(
                        albums.map { dto ->
                            AlbumResponse(
                                dto.uuid.toString(),
                                dto.title,
                                dto.coverUrl,
                            )
                        },
                        albums.count()
                    )
                )
            }

            post {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                val albumService = Album(db, userId)

                val request = call.receive<CreateRequest>()
                val id = albumService.create(
                    AlbumDto(
                        null,
                        request.title,
                    )
                )

                val album = albumService.get(id)?: return@post call.respond(HttpStatusCode.NotFound)
                call.respond(
                    HttpStatusCode.OK,
                    AlbumTinyResponse(
                        album.uuid.toString(),
                        album.title,
                    )
                )
            }

            route("/{id}") {
                get {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    val albumService = Album(db, userId)
                    val photoService = Photo(db, userId)

                    val id = call.parameters["id"]?: return@get call.respond(HttpStatusCode.BadRequest)
                    albumService.get(id)?: return@get call.respond(HttpStatusCode.NotFound)

                    val photos = photoService.getList(id )

                    call.respond(
                        HttpStatusCode.OK,
                        PhotoListResponse(
                            photos.map { dto ->
                                PhotoResponse(
                                    dto.uuid.toString(),
                                    dto.url
                                )
                            },
                            photos.count()
                        )
                    )
                }

                post {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val multipart = call.receiveMultipart()
                    val baseUrl = environment.config.propertyOrNull("ktor.application.baseUrl")?.getString() ?: "http://localhost:8080"

                    val albumService = Album(db, userId)
                    val fileService = File(baseUrl)

                    val album = albumService.get(id) ?: return@post call.respond(HttpStatusCode.NotFound)
                    val url = fileService.create(UUID.randomUUID().toString(), multipart) ?: return@post call.respond(HttpStatusCode.InternalServerError)
                    albumService.update(AlbumDto(
                        album.uuid,
                        album.title,
                        url
                    ))

                    call.respond(HttpStatusCode.OK)
                }

                delete {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    val albumService = Album(db, userId)

                    val id = call.parameters["id"]
                    albumService.delete(id ?: "")
                    call.respond(HttpStatusCode.OK)
                }

                route("rename") {
                    post {
                        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                        val albumService = Album(db, userId)

                        val id = call.parameters["id"]
                        val request = call.receive<RenameRequest>()

                        val album = albumService.get(id ?: "")?: return@post call.respond(HttpStatusCode.NotFound)
                        albumService.update(
                            AlbumDto(
                                album.uuid,
                                request.title,
                                album.coverUrl,
                            )
                        )

                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

    }
}