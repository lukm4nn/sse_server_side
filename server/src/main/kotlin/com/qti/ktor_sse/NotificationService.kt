package com.qti.ktor_sse

object NotificationService {

    fun notifyUser(userId: String, message: String) {
        // Coba kirim lewat SSE dulu
        val sseSent = if (PresenceManager.isOnline(userId)) {
            SseSessionManager.send(userId, "From SSE")
        } else {
            false
        }

        // Jika SSE gagal atau user memang offline, gunakan FCM sebagai fallback
        if (!sseSent) {
            println("User $userId tidak terjangkau lewat SSE. Mengirim pesan bangun via FCM...")
            FcmService.sendNotification(userId, "From FCM")
        } else {
            println("Notifikasi berhasil dikirim ke $userId via SSE.")
        }
    }
}