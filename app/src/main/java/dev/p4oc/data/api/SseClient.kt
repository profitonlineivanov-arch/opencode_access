package dev.p4oc.data.api

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SseClient @Inject constructor() {

    private var client: OkHttpClient? = null
    private var call: Call? = null

    fun connect(
        url: String,
        username: String,
        password: String,
        onMessage: (String) -> Unit,
        onError: (Throwable) -> Unit
    ): Flow<String> = callbackFlow {
        client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("$url/event")
            .addHeader("Accept", "text/event-stream")
            .addHeader("Cache-Control", "no-cache")
            .apply {
                if (username.isNotBlank()) {
                    val credentials = okhttp3.Credentials.basic(username, password)
                    addHeader("Authorization", credentials)
                }
            }
            .build()

        call = client?.newCall(request)
        
        call?.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    val source = body.source()
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.startsWith("data: ")) {
                            val data = line.substring(6)
                            if (data.isNotBlank()) {
                                trySend(data)
                                onMessage(data)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onError(e)
                close(e)
            }
        })

        awaitClose {
            call?.cancel()
            client = null
            call = null
        }
    }

    fun disconnect() {
        call?.cancel()
        client = null
        call = null
    }

    fun isConnected(): Boolean = call != null && call!!.isExecuted() && !call!!.isCanceled()
}
