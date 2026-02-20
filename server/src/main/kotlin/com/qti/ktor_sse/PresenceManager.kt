package com.qti.ktor_sse

import java.util.concurrent.ConcurrentHashMap

object PresenceManager {

    private val onlineUsers = ConcurrentHashMap<String, Boolean>()

    fun setOnline(userId: String) {
        onlineUsers[userId] = true
    }

    fun setOffline(userId: String) {
        onlineUsers.remove(userId)
    }

    fun isOnline(userId: String): Boolean {
        return onlineUsers[userId] == true
    }
}
