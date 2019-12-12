package com.example

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.http.content.*

fun main(args: Array<String>): Unit {
    val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("whats up", ContentType.Text.Plain)
            }
            get("/upper") {
                call.respondText("whates up".toUpperCase())
            }
            static("/") {
                resources("static")
            }
        }
    }
    server.start(wait = true)
}

