package com.gallery.http.route

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gallery.http.request.CredentialsRequest
import com.gallery.service.Database
import com.gallery.service.User
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*


fun Route.user(db: Database) {
    val userService = User(db)
    val secret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: "secret"
    val issuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "http://0.0.0.0:8080/"

    post("/register") {
        try {
            val request = call.receive<CredentialsRequest>()
            val user = userService.createUser(
                request.login,
                request.password
            )

            user?.let {
                call.respond(mapOf("id" to it.id))
            } ?: call.respond(
                HttpStatusCode.Conflict,
                mapOf("error" to "User already exists")
            )

        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    post("/login") {
        val request = call.receive<CredentialsRequest>()
        val user = userService.authenticate(
            request.login,
            request.password
        )

        user?.let {
            val token = JWT.create()
                .withIssuer(issuer)
                .withClaim("id", it.id)
                .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 15))
                .sign(Algorithm.HMAC256(secret))
            call.respond(hashMapOf("token" to token, ))
        } ?: call.respond(
            HttpStatusCode.Unauthorized,
            mapOf("error" to "Invalid credentials or inactive account")
        )
    }
}