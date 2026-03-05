package com.qti.ktor_sse

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.header
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.io.FileInputStream

fun main() {
    initFirebase()
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)

}

fun initFirebase() {
    val serviceAccount =
        FileInputStream("server/firebase-service-account.json")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    FirebaseApp.initializeApp(options)
}

fun Application.module() {
    routing {
        // 1. KONEKSI SSE
        get("/sse/notifications") {
            val userId = call.request.header("X-USER-ID")
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Missing User ID")
                return@get
            }

            // Set headers untuk SSE
            call.response.cacheControl(CacheControl.NoCache(null))

            try {
                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    println("User $userId terhubung via SSE")

                    // Tandai user sebagai online dan simpan writer-nya
                    PresenceManager.setOnline(userId)
                    SseSessionManager.add(userId, this)

                    try {
                        // Keep-alive loop
                        while (coroutineContext.isActive) {
                            try {
                                // Kirim ping untuk menjaga koneksi tetap hidup
                                write("event: ping\ndata: ${System.currentTimeMillis()}\n\n")
                                flush()
                            } catch (e: Exception) {
                                // Jika gagal menulis ping, berarti client sudah disconnect (ChannelWriteException)
                                println("Gagal menulis ke channel $userId (ping failed), menghentikan stream.")
                                break // Keluar dari loop while
                            }
                            delay(20_000) // Ping setiap 20 detik
                        }
                    } finally {
                        // Bagian ini sangat penting: dieksekusi saat loop berhenti atau error terjadi
                        PresenceManager.setOffline(userId)
                        SseSessionManager.remove(userId, this)
                        println("Koneksi SSE user $userId dibersihkan.")
                    }
                }
            } catch (e: Exception) {
                // Menangkap error di level respondTextWriter jika ada masalah inisialisasi
                PresenceManager.setOffline(userId)
                println("Exception pada SSE $userId: ${e.message}")
            }
        }

        // 2. REGISTER DEVICE TOKEN (Ubah ke Body JSON jika untuk produksi)
        post("/device/fcm") {
            val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val token = call.parameters["token"] ?: return@post call.respond(HttpStatusCode.BadRequest)

            DeviceStore.save(userId, token)
            call.respond(HttpStatusCode.OK, "Token FCM berhasil disimpan")
        }

        // 3. TRIGGER NOTIFIKASI
        post("/notify/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val msg = call.receiveText().ifBlank { "Halo dari Server! 🚀" }

            NotificationService.notifyUser(userId, msg)
            call.respond(HttpStatusCode.OK, "Notifikasi diproses")
        }
    }
}