package com.qti.ktor_sse

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification

object FcmService {
    fun sendNotification(userId: String, content: String) {
        val token = DeviceStore.get(userId) ?: return

        val message = Message.builder()
            .setToken(token)
            // Gunakan .setNotification untuk muncul di tray,
            // atau .putData untuk background processing
            .setNotification(Notification.builder()
                .setTitle("Update Baru")
                .setBody(content)
                .build())
            .putData("type", "WAKE_SSE")
            .build()

        try {
            FirebaseMessaging.getInstance().send(message)
        } catch (e: Exception) {
            println("Gagal kirim FCM: ${e.message}")
        }
    }
}
