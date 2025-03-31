package com.gallery

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gallery.http.route.albums
import com.gallery.http.route.user
import com.gallery.http.route.photos
import com.gallery.service.createDatabase
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = environment.config
    val db = createDatabase(config)

    install(ContentNegotiation) {
        json()
    }

    val secret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: "secret"
    val issuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "http://0.0.0.0:8080/"
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("id").asInt() != 0 ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired.")
            }
        }
    }

    routing {
        albums(db)
        photos(db)
        user(db)
    }
}
