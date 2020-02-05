package com.example

import io.ktor.http.cio.websocket.Frame
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class SampleHandler(val outgoing: SendChannel<Frame>, val name: String): Handler {
    override fun n(): String = name
    override fun handle(t: Task): Unit = when (t) {
        is CreateTask -> {
            GlobalScope.launch {
                outgoing.send(Frame.Text("created"))
            }
            Unit
        }
        else -> Unit
    }
}