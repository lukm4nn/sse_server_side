package com.qti.ktor_sse

import java.io.Writer
import java.util.concurrent.ConcurrentHashMap

object SseSessionManager {

    private val sessions =
        ConcurrentHashMap<String, MutableList<Writer>>()

    fun add(userId: String, writer: Writer) {
        sessions.computeIfAbsent(userId) { mutableListOf() }.add(writer)
    }

    fun remove(userId: String, writer: Writer) {
        sessions[userId]?.remove(writer)
    }

    fun send(userId: String, data: String) {
        sessions[userId]?.forEach { writer ->
            writer.write("data: $data\n\n")
            writer.flush()
        }
    }
}

