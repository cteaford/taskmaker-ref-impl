package com.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor

abstract class Task {
    abstract val n: String
}

interface Handler {
    fun n(): String
    fun handle(t: Task): Unit
}

typealias Handlers = MutableMap<String, Handler>
typealias Worker = SendChannel<Task>
typealias Workers = MutableMap<String, Worker>
class TaskMaker {
    private var muxer: SendChannel<Message<*>>? = null
    fun make(t: Task) = GlobalScope.launch {
        muxer?.send(HandleTaskMsg(t))
    }
    fun register(h: Handler) {
        GlobalScope.launch { muxer?.send(AddHandlerMsg(h)) }
    }

    @Synchronized fun start(): TaskMaker {
        GlobalScope.launch(Dispatchers.Default) {
            muxer = taskMuxer()
        }
        return this
    }

    @Synchronized fun stop(): Unit {
        muxer?.let { it.close() }
    }
    companion object {
        /**
         * A class representing multiple user created handlers as 1 handler.
         * The functions are not composed, just called sequentially
         */
        private data class CompoundHandler(val f: Handler, val g: Handler): Handler {
            override fun n(): String = f.n()
            override fun handle(t: Task) {
                f.handle(t)
                g.handle(t)
            }
        }

        /**
         * Messages are used to coordinate tasks between 'user threads'
         * and the worker threads. All messaging is async so methods
         * sending messages need to release immediately(not block the thread).
         */
        sealed class Message<T> {
            abstract val value: T
            abstract class HandlerMessage<T>: Message<T>()
            abstract class TaskMessage<T>: Message<T>()
        }
        data class AddHandlerMsg(override val value: Handler): Message.HandlerMessage<Handler>()
        data class HandleTaskMsg(override val value: Task): Message.TaskMessage<Task>()

        /**
         * Workers are kotlin actors( a coroutine linked to a recieve channel)
         * A worker holds a handler and passes messages to it (potentially blocking)
         */
        private fun CoroutineScope.makeWorker(handler: Handler) = actor<Task>(
                capacity = 1000,
                start = CoroutineStart.LAZY) {
            for (msg in channel) {
                handler.handle(msg)
            }
        }

        /**
         * the TaskMuxer(Multiplexer) is the core of task maker.
         * It coordinates inbound messages on its own coroutine.
         * the muxer is just an actor like the other workers but
         * it hands out tasks to the appropriate worker.
         * consider moving handler registration to its own worker
         * to increase concurrency and free up this routine for pure
         * task handling
         */
        private fun CoroutineScope.taskMuxer() = actor<Message<*>> {
            val ws: Workers = mutableMapOf()
            val hs: Handlers = mutableMapOf<String, Handler>()
            for (msg in channel) {
                when(msg) {
                    is HandleTaskMsg -> {
                        val n = msg.value.n
                        val t = msg.value
                        val h = hs[n]
                        h?.let { //silently ignore tasks without handlers for now
                            fetchWorker(ws, n, h) {
                                hndlr: Handler ->
                                    val w = makeWorker(hndlr)
                                    ws[n] = w
                                    w
                            }.send(t)
                        }
                    }
                    is AddHandlerMsg -> {
                        addHandler(msg.value, hs)
                    }
                }
            }
        }

        private fun fetchWorker(ws: Workers, n: String, h: Handler, makeWorker: (Handler) -> Worker): Worker {
            val w = ws[n]
            return if(w != null) w
            else makeWorker(h)
        }

        private fun addHandler(h: Handler, hs: Handlers) {
            val n = h.n()
            val _h = hs[n]
            if (_h == null) hs[n] = h
            else hs[n] = CompoundHandler(h, _h)
        }
    }
}
