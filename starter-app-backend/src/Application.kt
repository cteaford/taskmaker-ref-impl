package com.example

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import io.ktor.util.generateNonce
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket

data class ExampleSession(val id: String)
data class CreateTask(override val n:String = "create", val message: String): Task()
data class Thing(val name: String)
data class ThingUI(val id: String, val name: String)

const val API_LEADER = "api"

fun main() {
    val tm = TaskMaker().start()
    val things = mutableMapOf<String, Thing>()

    val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            jackson { enable(SerializationFeature.INDENT_OUTPUT) }
        }
        install(WebSockets)
        install(Sessions) {
            cookie<ExampleSession>("LSM_TOKEN", storage = SessionStorageMemory()) {
                cookie.path = "/"
                cookie.domain = "localhost" //change this
            }
        }
        intercept(ApplicationCallPipeline.Features) {
            if (call.sessions.get<ExampleSession>() == null) {
                call.sessions.set(ExampleSession(generateNonce()))
            }
        }
        routing {
            get("/${API_LEADER}/things") {
                call.respond(things.map { e -> ThingUI(e.key, e.value.name) })
            }
            get("/${API_LEADER}/thing/{id}") {
                call.respond { things[call.parameters["id"]] }
            }
            post("/${API_LEADER}/thing") {
                val id = generateNonce()
                things[id] = call.receive()
                tm.make(CreateTask(call.sessions.get<ExampleSession>()?.id.orEmpty(), "created with id: $id"))
                call.respond(HttpStatusCode.Created)
            }
            static("/") {
                resources("static")
            }
            webSocket("/") {
                tm.register(SampleHandler(outgoing, call.sessions.get<ExampleSession>()?.id.orEmpty()))
                for (f in incoming) {} //ignore inbound messages on this channel for now
            }
        }
    }
    server.start(wait = true)
}

