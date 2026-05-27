import io.ktor.client.HttpClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
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
            val action = handleSocketIoFrame(raw, endpoint, endpoint.json)
            when (action) {
                is SocketIoFrameAction.SendText -> realtimeSession.sendText(action.text)
                is SocketIoFrameAction.EmitEvent -> onEvent(endpoint.json.decodeFromJsonElement(action.payload))
                SocketIoFrameAction.Ignore -> Unit
            }
        },
        session = session,
    )
}

@PublishedApi
internal sealed class SocketIoFrameAction {
    data object Ignore : SocketIoFrameAction()
    data class SendText(val text: String) : SocketIoFrameAction()
    data class EmitEvent(val payload: JsonElement) : SocketIoFrameAction()
}

@PublishedApi
internal fun handleSocketIoFrame(
    raw: String,
    endpoint: RealtimeEndpoint.SocketIo,
    json: kotlinx.serialization.json.Json,
): SocketIoFrameAction {
    return when {
        raw.startsWith("0") -> {
            val namespace = endpoint.namespace.takeIf { it != "/" } ?: ""
            SocketIoFrameAction.SendText("40$namespace")
        }
        raw.startsWith("2") -> SocketIoFrameAction.SendText("3")
        raw.startsWith("42") -> {
            val payload = raw.removePrefix("42")
            val eventArray = json.decodeFromString<JsonArray>(payload)
            if (eventArray.size < 2) return SocketIoFrameAction.Ignore
            val eventName = (eventArray[0] as? JsonPrimitive)?.content
            if (endpoint.eventName != null && endpoint.eventName != eventName) {
                SocketIoFrameAction.Ignore
            } else {
                SocketIoFrameAction.EmitEvent(eventArray[1])
            }
        }
        else -> SocketIoFrameAction.Ignore
    }
}
