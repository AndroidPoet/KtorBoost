import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public class RealtimeSession<Outgoing>(
    @PublishedApi internal val session: DefaultClientWebSocketSession,
    @PublishedApi internal val json: Json,
) {
    public suspend inline fun <reified T : Outgoing> sendJson(value: T) {
        session.send(Frame.Text(json.encodeToString(value)))
    }

    public suspend fun sendText(text: String) {
        session.send(Frame.Text(text))
    }

    public suspend fun close(
        reason: CloseReason = CloseReason(CloseReason.Codes.NORMAL, "Closed by client."),
    ) {
        session.close(reason)
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtime(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    crossinline onEvent: suspend (Incoming) -> Unit,
): Unit =
    realtime<Incoming, Nothing>(
        urlString = urlString,
        json = json,
        onEvent = onEvent,
        session = {},
    )

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtime(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    when (val result = realtimeResult(urlString, json, onEvent, session)) {
        is RealtimeResult.Success -> Unit
        is RealtimeResult.Unsupported -> throw RealtimeProtocolNotYetSupportedException(result.protocol, result.message)
        is RealtimeResult.Failure -> throw result.cause
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtimeChat(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    crossinline onMessage: suspend (Incoming) -> Unit,
): Unit =
    realtime<Incoming>(
        urlString = urlString,
        json = json,
        onEvent = onMessage,
    )

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeChat(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    crossinline onMessage: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    realtime(urlString, json, onMessage, session)
}

public suspend inline fun <reified Incoming> HttpClient.realtimeResult(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    crossinline onEvent: suspend (Incoming) -> Unit,
): RealtimeResult<Unit> =
    realtimeResult<Incoming, Nothing>(
        urlString = urlString,
        json = json,
        onEvent = onEvent,
        session = {},
    )

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeResult(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return try {
        webSocket(urlString = urlString) {
            coroutineScope {
                val realtimeSession = RealtimeSession<Outgoing>(this@webSocket, json)
                val receiver =
                    launch {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                onEvent(json.decodeFromString(frame.readText()))
                            }
                        }
                    }

                try {
                    realtimeSession.session()
                } finally {
                    receiver.cancelAndJoin()
                }
            }
        }
        realtimeSuccess(Unit)
    } catch (cause: Throwable) {
        realtimeFailure(RealtimeProtocol.WebSocket, cause)
    }
}

@PublishedApi
internal suspend inline fun <Outgoing> HttpClient.realtimeTextResult(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    crossinline onText: suspend (String, RealtimeSession<Outgoing>) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return try {
        webSocket(urlString = urlString) {
            coroutineScope {
                val realtimeSession = RealtimeSession<Outgoing>(this@webSocket, json)
                val receiver =
                    launch {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                onText(frame.readText(), realtimeSession)
                            }
                        }
                    }

                try {
                    realtimeSession.session()
                } finally {
                    receiver.cancelAndJoin()
                }
            }
        }
        realtimeSuccess(Unit)
    } catch (cause: Throwable) {
        realtimeFailure(RealtimeProtocol.WebSocket, cause)
    }
}
