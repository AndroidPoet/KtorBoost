import io.ktor.client.HttpClient
import kotlinx.serialization.decodeFromString

public suspend inline fun <reified Incoming> HttpClient.realtimeMqttOverWebSocket(
    endpoint: RealtimeEndpoint.MqttOverWebSocket,
    crossinline onEvent: suspend (Incoming) -> Unit,
): Unit = realtimeMqttOverWebSocket<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeMqttOverWebSocket(
    endpoint: RealtimeEndpoint.MqttOverWebSocket,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    when (val result = realtimeMqttOverWebSocketResult(endpoint, onEvent, session)) {
        is RealtimeResult.Success -> Unit
        is RealtimeResult.Unsupported -> throw RealtimeProtocolNotYetSupportedException(result.protocol, result.message)
        is RealtimeResult.Failure -> throw result.cause
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtimeMqttOverWebSocketResult(
    endpoint: RealtimeEndpoint.MqttOverWebSocket,
    crossinline onEvent: suspend (Incoming) -> Unit,
): RealtimeResult<Unit> = realtimeMqttOverWebSocketResult<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeMqttOverWebSocketResult(
    endpoint: RealtimeEndpoint.MqttOverWebSocket,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return realtimeTextResult(
        urlString = endpoint.url,
        json = endpoint.json,
        onText = { raw, _ ->
            onEvent(decodeMqttIncoming(raw, endpoint.json))
        },
        session = session,
    ).let { result ->
        when (result) {
            is RealtimeResult.Failure ->
                realtimeFailure(RealtimeProtocol.MqttOverWebSocket, result.cause)
            else -> result
        }
    }
}

@PublishedApi
internal inline fun <reified Incoming> decodeMqttIncoming(
    raw: String,
    json: kotlinx.serialization.json.Json,
): Incoming = json.decodeFromString(raw)
