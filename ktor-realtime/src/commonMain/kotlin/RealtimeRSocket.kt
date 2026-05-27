import io.ktor.client.HttpClient
import kotlinx.serialization.decodeFromString

public suspend inline fun <reified Incoming> HttpClient.realtimeRSocket(
    endpoint: RealtimeEndpoint.RSocket,
    crossinline onEvent: suspend (Incoming) -> Unit,
): Unit = realtimeRSocket<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeRSocket(
    endpoint: RealtimeEndpoint.RSocket,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    when (val result = realtimeRSocketResult(endpoint, onEvent, session)) {
        is RealtimeResult.Success -> Unit
        is RealtimeResult.Unsupported -> throw RealtimeProtocolNotYetSupportedException(result.protocol, result.message)
        is RealtimeResult.Failure -> throw result.cause
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtimeRSocketResult(
    endpoint: RealtimeEndpoint.RSocket,
    crossinline onEvent: suspend (Incoming) -> Unit,
): RealtimeResult<Unit> = realtimeRSocketResult<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeRSocketResult(
    endpoint: RealtimeEndpoint.RSocket,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return realtimeTextResult(
        urlString = endpoint.url,
        json = endpoint.json,
        onText = { raw, _ ->
            onEvent(endpoint.json.decodeFromString<Incoming>(raw))
        },
        session = session,
    ).let { result ->
        when (result) {
            is RealtimeResult.Failure ->
                realtimeFailure(RealtimeProtocol.RSocket, result.cause)
            else -> result
        }
    }
}
