package com.qti.ktor_sse

import java.io.Writer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object SseSessionManager {
    private val sessions = ConcurrentHashMap<String, CopyOnWriteArrayList<Writer>>()

    fun add(userId: String, writer: Writer) {
        sessions.computeIfAbsent(userId) { CopyOnWriteArrayList() }.add(writer)
    }

    fun remove(userId: String, writer: Writer) {
        sessions[userId]?.remove(writer)
        if (sessions[userId]?.isEmpty() == true) {
            sessions.remove(userId)
        }
    }

    /**
     * Mengirim data ke semua session user.
     * Mengembalikan true jika minimal ada satu session yang berhasil dikirimi data.
     */
    fun send(userId: String, data: String): Boolean {
        val userSessions = sessions[userId] ?: return false
        var anySuccess = false

        userSessions.forEach { writer ->
            try {
                writer.write("data: $data\n\n")
                writer.flush()
                anySuccess = true
            } catch (e: Exception) {
                // Channel putus (ChannelWriteException)
                println("Gagal menulis ke channel $userId, menghapus session.")
                remove(userId, writer)
            }
        }
        return anySuccess
    }
}