import io.ktor.client.HttpClient
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

public suspend inline fun <reified Incoming> HttpClient.realtimeGraphQlSubscriptions(
    endpoint: RealtimeEndpoint.GraphQlSubscriptions,
    crossinline onEvent: suspend (Incoming) -> Unit,
): Unit = realtimeGraphQlSubscriptions<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeGraphQlSubscriptions(
    endpoint: RealtimeEndpoint.GraphQlSubscriptions,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
) {
    when (val result = realtimeGraphQlSubscriptionsResult(endpoint, onEvent, session)) {
        is RealtimeResult.Success -> Unit
        is RealtimeResult.Unsupported -> throw RealtimeProtocolNotYetSupportedException(result.protocol, result.message)
        is RealtimeResult.Failure -> throw result.cause
    }
}

public suspend inline fun <reified Incoming> HttpClient.realtimeGraphQlSubscriptionsResult(
    endpoint: RealtimeEndpoint.GraphQlSubscriptions,
    crossinline onEvent: suspend (Incoming) -> Unit,
): RealtimeResult<Unit> = realtimeGraphQlSubscriptionsResult<Incoming, Nothing>(endpoint, onEvent, session = {})

public suspend inline fun <reified Incoming, Outgoing> HttpClient.realtimeGraphQlSubscriptionsResult(
    endpoint: RealtimeEndpoint.GraphQlSubscriptions,
    crossinline onEvent: suspend (Incoming) -> Unit,
    crossinline session: suspend RealtimeSession<Outgoing>.() -> Unit,
): RealtimeResult<Unit> {
    return realtimeTextResult<Outgoing>(
        urlString = endpoint.url,
        json = endpoint.json,
        onText = { raw, _ ->
            val packet = endpoint.json.decodeFromString<kotlinx.serialization.json.JsonObject>(raw)
            val type = packet["type"]?.jsonPrimitive?.content ?: return@realtimeTextResult
            if (type == "next") {
                val payload = packet["payload"]?.jsonObject ?: return@realtimeTextResult
                val dataElement = payload["data"] ?: return@realtimeTextResult
                onEvent(endpoint.json.decodeFromJsonElement<Incoming>(dataElement))
            }
        },
        session = {
            val init =
                buildJsonObject {
                    put("type", JsonPrimitive("connection_init"))
                    put("payload", buildJsonObject {})
                }
            sendText(init.toString())

            val subscribePayload =
                buildJsonObject {
                    put("query", JsonPrimitive(endpoint.query))
                    endpoint.operationName?.let { put("operationName", JsonPrimitive(it)) }
                    endpoint.variablesJson?.let { vars ->
                        put("variables", endpoint.json.decodeFromString<kotlinx.serialization.json.JsonElement>(vars))
                    }
                }
            val subscribe =
                buildJsonObject {
                    put("id", JsonPrimitive(endpoint.operationId))
                    put("type", JsonPrimitive("subscribe"))
                    put("payload", subscribePayload)
                }
            sendText(subscribe.toString())
            session()
        },
    )
}
