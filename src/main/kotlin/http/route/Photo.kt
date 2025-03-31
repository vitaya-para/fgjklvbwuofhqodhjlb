package com.gallery.http.route

import com.gallery.http.request.photo.Create
import com.gallery.http.response.photo.PhotoTiny
import com.gallery.service.Album
import com.gallery.service.Database
import com.gallery.service.Photo
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.gallery.dto.Photo as PhotoDto
import com.gallery.service.File as FileService

fun Route.photos(db: Database) {

    authenticate("auth-jwt") {
        route("photos") {

            post {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                val albumService = Album(db, userId)
                val photoService = Photo(db, userId)

                val request = call.receive<Create>()
                albumService.get(request.album_uuid) ?: call.respond(HttpStatusCode.BadRequest)
                val id = photoService.create(request.album_uuid)

                call.respond(
                    HttpStatusCode.OK,
                    PhotoTiny(id)
                )
            }

            route("/{id}") {

                get {
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    val baseUrl = environment.config.propertyOrNull("ktor.application.baseUrl")?.getString() ?: "http://localhost:8080"

                    val fileService = FileService(baseUrl)
                    val albumService = Album(db, userId)
                    val photoService = Photo(db, userId)

                    val file = fileService.get(id)
                    val url = "$baseUrl/photos/$id"

                    if (file != null && (albumService.existUrl(url) || photoService.existUrl(url))) {
                        call.respondFile(file)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "File not found")
                    }
                }

                post {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val multipart = call.receiveMultipart()
                    val baseUrl = environment.config.propertyOrNull("ktor.application.baseUrl")?.getString() ?: "http://localhost:8080"

                    val photoService = Photo(db, userId)
                    val fileService = FileService(baseUrl)

                    val photo = photoService.get(id) ?: return@post call.respond(HttpStatusCode.NotFound)

                    if (photo.url != "") {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }

                    val url = fileService.create(photo.uuid.toString(), multipart)
                    photoService.update(
                        PhotoDto(
                            photo.uuid,
                            url
                        )
                    )
                    call.respond(HttpStatusCode.OK)
                }

                delete {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    val photoService = Photo(db, userId)

                    val id = call.parameters["id"]
                    photoService.delete(id ?: "")
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}