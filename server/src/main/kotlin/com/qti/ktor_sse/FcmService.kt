package com.qti.ktor_sse

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification

object FcmService {
    fun sendNotification(userId: String, content: String) {
        val token = DeviceStore.get(userId) ?: return

        val message = Message.builder()
            .setToken(token)
            .putData("type", "WAKE_SSE")
            .putData("title", "Update Baru")
            .putData("content", content)
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH) // Tetap HIGH agar bangun
                .build())
            .build()

        try {
            FirebaseMessaging.getInstance().send(message)
            println("FCM Data Message terkirim ke $userId")
        } catch (e: Exception) {
            println("Gagal kirim FCM: ${e.message}")
        }
    }
}
