import io.ktor.client.HttpClient
import kotlinx.serialization.decodeFromString

@PublishedApi
internal data class StompFrameAction(
    val outboundText: String? = null,
    val messageBody: String? = null,
)

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
            val actions = handleStompFrames(raw, endpoint)
            for (action in actions) {
                action.outboundText?.let { realtimeSession.sendText(it) }
                action.messageBody?.let { onEvent(endpoint.json.decodeFromString<Incoming>(it)) }
            }
        },
        session = {
            sendText(buildStompConnectFrame(endpoint))
            session()
        },
    )
}

@PublishedApi
internal fun buildStompConnectFrame(endpoint: RealtimeEndpoint.Stomp): String =
    buildString {
        append("CONNECT\n")
        append("accept-version:1.2\n")
        endpoint.host?.let { append("host:$it\n") }
        endpoint.login?.let { append("login:$it\n") }
        endpoint.passcode?.let { append("passcode:$it\n") }
        append("\n")
        append('\u0000')
    }

@PublishedApi
internal fun handleStompFrames(
    raw: String,
    endpoint: RealtimeEndpoint.Stomp,
): List<StompFrameAction> {
    val actions = mutableListOf<StompFrameAction>()
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
                actions += StompFrameAction(outboundText = subscribeFrame)
            }
            "MESSAGE" -> {
                val splitMarker = normalized.indexOf("\n\n")
                if (splitMarker != -1) {
                    actions += StompFrameAction(messageBody = normalized.substring(splitMarker + 2))
                }
            }
        }
    }
    return actions
}
