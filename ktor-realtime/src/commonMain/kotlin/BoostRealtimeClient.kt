import io.ktor.client.HttpClient

public suspend inline fun <reified Incoming> HttpClient.realtime(
    endpoint: RealtimeEndpoint,
    crossinline onEvent: suspend (Incoming) -> Unit,
): Unit =
    realtime<Incoming, Nothing>(
        endpoint = endpoint,
        onEvent = onEvent,
        session = {},
    )

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtime(
    endpoint: RealtimeEndpoint,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    when (val result = realtimeResult(endpoint, onEvent, session)) {
        is RealtimeResult.Success -> Unit
        is RealtimeResult.Unsupported -> throw RealtimeProtocolNotYetSupportedException(result.protocol, result.message)
        is RealtimeResult.Failure -> throw result.cause
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtimeResult(
    endpoint: RealtimeEndpoint,
    crossinline onEvent: suspend (Incoming) -> Unit,
): RealtimeResult<Unit> =
    realtimeResult<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeResult(
    endpoint: RealtimeEndpoint,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return when (endpoint) {
        is RealtimeEndpoint.WebSocket ->
            realtimeResult(
                urlString = endpoint.url,
                json = endpoint.json,
                onEvent = onEvent,
                session = session,
            )

        is RealtimeEndpoint.ServerSentEvents ->
            realtimeSseJsonResult(endpoint, onEvent)

        is RealtimeEndpoint.LaravelReverb ->
            realtimeReverbResult(endpoint, onEvent, session)

        is RealtimeEndpoint.SocketIo ->
            realtimeSocketIoResult(endpoint, onEvent, session)

        is RealtimeEndpoint.Stomp ->
            realtimeStompResult(endpoint, onEvent, session)

        is RealtimeEndpoint.GraphQlSubscriptions ->
            realtimeGraphQlSubscriptionsResult(endpoint, onEvent, session)

        is RealtimeEndpoint.MqttOverWebSocket ->
            realtimeMqttOverWebSocketResult(endpoint, onEvent, session)

        is RealtimeEndpoint.RSocket ->
            realtimeRSocketResult(endpoint, onEvent, session)

        is RealtimeEndpoint.LongPolling ->
            realtimeLongPollingResult(endpoint, onEvent, session)
    }
}
