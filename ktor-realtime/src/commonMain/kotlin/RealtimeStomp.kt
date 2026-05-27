import io.ktor.client.HttpClient

public suspend inline fun <reified Incoming> HttpClient.realtimeStomp(
    endpoint: RealtimeEndpoint.Stomp,
    crossinline onEvent: suspend (Incoming) -> Unit,
): Unit = realtimeStomp<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeStomp(
    endpoint: RealtimeEndpoint.Stomp,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    when (val result = realtimeStompResult(endpoint, onEvent, session)) {
        is RealtimeResult.Success -> Unit
        is RealtimeResult.Unsupported -> throw RealtimeProtocolNotYetSupportedException(result.protocol, result.message)
        is RealtimeResult.Failure -> throw result.cause
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtimeStompResult(
    endpoint: RealtimeEndpoint.Stomp,
    crossinline onEvent: suspend (Incoming) -> Unit,
): RealtimeResult<Unit> = realtimeStompResult<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeStompResult(
    endpoint: RealtimeEndpoint.Stomp,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return realtimeTextResult<Outgoing>(
        urlString = endpoint.url,
        json = endpoint.json,
        onText = { raw, realtimeSession ->
            val frames = raw.split('\u0000')
            for (frame in frames) {
                val normalized = frame.trim()
                if (normalized.isEmpty()) continue
                val lines = normalized.split('\n')
                val command = lines.firstOrNull()?.trim() ?: continue
                when (command) {
                    "CONNECTED" -> {
                        val subscribeFrame =
                            buildString {
                                append("SUBSCRIBE\n")
                                append("id:sub-0\n")
                                append("destination:${endpoint.destination}\n")
                                append("\n")
                                append('\u0000')
                            }
                        realtimeSession.sendText(subscribeFrame)
                    }
                    "MESSAGE" -> {
                        val splitMarker = normalized.indexOf("\n\n")
                        if (splitMarker == -1) continue
                        val body = normalized.substring(splitMarker + 2)
                        onEvent(endpoint.json.decodeFromString<Incoming>(body))
                    }
                }
            }
        },
        session = {
            val connect =
                buildString {
                    append("CONNECT\n")
                    append("accept-version:1.2\n")
                    endpoint.host?.let { append("host:$it\n") }
                    endpoint.login?.let { append("login:$it\n") }
                    endpoint.passcode?.let { append("passcode:$it\n") }
                    append("\n")
                    append('\u0000')
                }
            sendText(connect)
            session()
        },
    )
}
