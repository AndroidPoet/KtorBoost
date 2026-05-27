import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString

public suspend inline fun <reified Incoming> HttpClient.realtimeLongPolling(
    endpoint: RealtimeEndpoint.LongPolling,
    crossinline onEvent: suspend (Incoming) -> Unit,
): Unit = realtimeLongPolling<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeLongPolling(
    endpoint: RealtimeEndpoint.LongPolling,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    when (val result = realtimeLongPollingResult(endpoint, onEvent, session)) {
        is RealtimeResult.Success -> Unit
        is RealtimeResult.Unsupported -> throw RealtimeProtocolNotYetSupportedException(result.protocol, result.message)
        is RealtimeResult.Failure -> throw result.cause
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtimeLongPollingResult(
    endpoint: RealtimeEndpoint.LongPolling,
    crossinline onEvent: suspend (Incoming) -> Unit,
): RealtimeResult<Unit> = realtimeLongPollingResult<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeLongPollingResult(
    endpoint: RealtimeEndpoint.LongPolling,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return try {
        while (true) {
            val response =
                request(endpoint.url) {
                    method = endpoint.method
                }
            val payload = response.body<String>()
            onEvent(decodeLongPollingIncoming(payload, endpoint.json))
            delay(endpoint.intervalMillis)
        }
        @Suppress("UNREACHABLE_CODE")
        realtimeSuccess(Unit)
    } catch (cause: Throwable) {
        realtimeFailure(RealtimeProtocol.LongPolling, cause)
    }
}

@PublishedApi
internal inline fun <reified Incoming> decodeLongPollingIncoming(
    payload: String,
    json: kotlinx.serialization.json.Json,
): Incoming = json.decodeFromString(payload)
