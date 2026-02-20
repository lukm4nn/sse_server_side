package com.qti.ktor_sse

object DeviceStore {

    private val tokens = mutableMapOf<String, String>()

    fun save(userId: String, token: String) {
        tokens[userId] = token
    }

    fun get(userId: String): String? {
        return tokens[userId]
    }
}