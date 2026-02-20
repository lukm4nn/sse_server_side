package com.qti.ktor_sse

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message

object FcmService {

    fun sendWake(userId: String) {

        val token = DeviceStore.get(userId) ?: return

        val message = Message.builder()
            .setToken(token)
            .putData("type", "WAKE_SSE")
            .build()

        FirebaseMessaging.getInstance().send(message)
    }
}
