import io.ktor.client.HttpClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

public suspend inline fun <reified Incoming> HttpClient.realtimeReverb(
    endpoint: RealtimeEndpoint.LaravelReverb,
    crossinline onEvent: suspend (Incoming) -> Unit,
): Unit = realtimeReverb<Incoming, Nothing>(endpoint, onEvent, session = {})
 

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeReverb(
    endpoint: RealtimeEndpoint.LaravelReverb,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    when (val result = realtimeReverbResult(endpoint, onEvent, session)) {
        is RealtimeResult.Success -> Unit
        is RealtimeResult.Unsupported -> throw RealtimeProtocolNotYetSupportedException(result.protocol, result.message)
        is RealtimeResult.Failure -> throw result.cause
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtimeReverbResult(
    endpoint: RealtimeEndpoint.LaravelReverb,
    crossinline onEvent: suspend (Incoming) -> Unit,
): RealtimeResult<Unit> = realtimeReverbResult<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeReverbResult(
    endpoint: RealtimeEndpoint.LaravelReverb,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return realtimeTextResult<Outgoing>(
        urlString = endpoint.url,
        json = endpoint.json,
        onText = { raw, realtimeSession ->
            val packet = endpoint.json.decodeFromString<kotlinx.serialization.json.JsonObject>(raw)
            val event = packet["event"]?.jsonPrimitive?.content
            val dataRaw = packet["data"]

            if (event == "pusher:ping") {
                realtimeSession.sendText("""{"event":"pusher:pong","data":{}}""")
                return@realtimeTextResult
            }
            if (event == "pusher:connection_established" && endpoint.channel != null) {
                val subscribe =
                    buildJsonObject {
                        put("event", JsonPrimitive("pusher:subscribe"))
                        put(
                            "data",
                            buildJsonObject {
                                put("channel", JsonPrimitive(endpoint.channel))
                                endpoint.auth?.let { put("auth", JsonPrimitive(it)) }
                                endpoint.channelData?.let { put("channel_data", JsonPrimitive(it)) }
                            },
                        )
                    }
                realtimeSession.sendText(subscribe.toString())
                return@realtimeTextResult
            }
            if (event != null && event.startsWith("pusher:")) return@realtimeTextResult

            val decoded: Incoming? =
                when (dataRaw) {
                    null -> null
                    is JsonPrimitive ->
                        if (dataRaw.isString) {
                            endpoint.json.decodeFromString<Incoming>(dataRaw.content)
                        } else {
                            endpoint.json.decodeFromJsonElement<Incoming>(dataRaw)
                        }
                    else -> endpoint.json.decodeFromJsonElement<Incoming>(dataRaw)
                }
            if (decoded != null) onEvent(decoded)
        },
        session = session,
    )
}
