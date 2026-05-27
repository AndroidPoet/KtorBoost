import io.ktor.client.HttpClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
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
            when (val action = handleReverbFrame(raw, endpoint, endpoint.json)) {
                is ReverbFrameAction.SendText -> realtimeSession.sendText(action.text)
                is ReverbFrameAction.EmitEvent ->
                    onEvent(
                        decodeReverbIncoming(
                            action.dataRaw,
                            endpoint.json,
                        ),
                    )
                ReverbFrameAction.Ignore -> Unit
            }
        },
        session = session,
    )
}

@PublishedApi
internal sealed class ReverbFrameAction {
    data object Ignore : ReverbFrameAction()
    data class SendText(val text: String) : ReverbFrameAction()
    data class EmitEvent(val dataRaw: JsonElement) : ReverbFrameAction()
}

@PublishedApi
internal fun handleReverbFrame(
    raw: String,
    endpoint: RealtimeEndpoint.LaravelReverb,
    json: kotlinx.serialization.json.Json,
): ReverbFrameAction {
    val packet = json.decodeFromString<JsonObject>(raw)
    val event = packet["event"]?.jsonPrimitive?.content
    val dataRaw = packet["data"]

    if (event == "pusher:ping") {
        return ReverbFrameAction.SendText("""{"event":"pusher:pong","data":{}}""")
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
        return ReverbFrameAction.SendText(subscribe.toString())
    }
    if (event != null && event.startsWith("pusher:")) return ReverbFrameAction.Ignore
    if (dataRaw == null) return ReverbFrameAction.Ignore
    return ReverbFrameAction.EmitEvent(dataRaw)
}

@PublishedApi
internal inline fun <reified Incoming> decodeReverbIncoming(
    dataRaw: JsonElement,
    json: kotlinx.serialization.json.Json,
): Incoming {
    return when (dataRaw) {
        is JsonPrimitive ->
            if (dataRaw.isString) {
                json.decodeFromString<Incoming>(dataRaw.content)
            } else {
                json.decodeFromJsonElement<Incoming>(dataRaw)
            }
        else -> json.decodeFromJsonElement<Incoming>(dataRaw)
    }
}
