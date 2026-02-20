package com.qti.ktor_sse

object NotificationService {

    fun notifyUser(userId: String, message: String) {

        if (PresenceManager.isOnline(userId)) {
            SseSessionManager.send(userId, message)

        } else {
            FcmService.sendWake(userId)
        }
    }
}