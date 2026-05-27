import io.ktor.client.HttpClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

public suspend inline fun <reified Incoming> HttpClient.realtimeSocketIo(
    endpoint: RealtimeEndpoint.SocketIo,
    crossinline onEvent: suspend (Incoming) -> Unit,
): Unit = realtimeSocketIo<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeSocketIo(
    endpoint: RealtimeEndpoint.SocketIo,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    when (val result = realtimeSocketIoResult(endpoint, onEvent, session)) {
        is RealtimeResult.Success -> Unit
        is RealtimeResult.Unsupported -> throw RealtimeProtocolNotYetSupportedException(result.protocol, result.message)
        is RealtimeResult.Failure -> throw result.cause
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtimeSocketIoResult(
    endpoint: RealtimeEndpoint.SocketIo,
    crossinline onEvent: suspend (Incoming) -> Unit,
): RealtimeResult<Unit> = realtimeSocketIoResult<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeSocketIoResult(
    endpoint: RealtimeEndpoint.SocketIo,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return realtimeTextResult<Outgoing>(
        urlString = endpoint.url,
        json = endpoint.json,
        onText = { raw, realtimeSession ->
            when {
                raw.startsWith("0") -> {
                    val namespace = endpoint.namespace.takeIf { it != "/" } ?: ""
                    realtimeSession.sendText("40$namespace")
                }
                raw.startsWith("40") -> Unit
                raw.startsWith("2") -> realtimeSession.sendText("3")
                raw.startsWith("42") -> {
                    val payload = raw.removePrefix("42")
                    val eventArray = endpoint.json.decodeFromString<JsonArray>(payload)
                    if (eventArray.size < 2) return@realtimeTextResult
                    val eventName = (eventArray[0] as? JsonPrimitive)?.content
                    if (endpoint.eventName != null && endpoint.eventName != eventName) return@realtimeTextResult
                    val data = eventArray[1]
                    onEvent(endpoint.json.decodeFromJsonElement<Incoming>(data))
                }
            }
        },
        session = session,
    )
}
