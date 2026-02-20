package com.qti.ktor_sse

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.http.CacheControl
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.header
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import java.io.FileInputStream

fun main() {
    initFirebase()
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)

}

fun initFirebase() {
    val serviceAccount =
        FileInputStream("firebase-service-account.json")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    FirebaseApp.initializeApp(options)
}

fun Application.module() {

    routing {

        // =========================
        // SSE CONNECT
        // =========================
        get("/sse/notifications") {

            val userId = call.request.header("X-USER-ID")
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            call.response.cacheControl(CacheControl.NoCache(null))
            call.response.headers.append(HttpHeaders.ContentType, "text/event-stream")
            call.response.headers.append(HttpHeaders.Connection, "keep-alive")

            call.respondTextWriter {

                PresenceManager.setOnline(userId)
                SseSessionManager.add(userId, this)

                try {
                    while (true) {
                        write("data: ping\n\n")
                        flush()
                        delay(30_000)
                    }
                } finally {
                    PresenceManager.setOffline(userId)
                    SseSessionManager.remove(userId, this)
                }
            }
        }

        // =========================
        // REGISTER FCM TOKEN
        // =========================
        post("/device/fcm") {
            val userId = call.request.queryParameters["userId"]!!
            val token = call.request.queryParameters["token"]!!

            DeviceStore.save(userId, token)

            call.respond("Saved")
        }

        // =========================
        // TRIGGER NOTIFICATION
        // =========================
        post("/notify/{userId}") {

            val userId = call.parameters["userId"]!!

            NotificationService.notifyUser(
                userId = userId,
                message = "Notifikasi baru 🚀"
            )

            call.respond("Triggered")
        }
    }
}