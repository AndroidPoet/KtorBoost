import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readUTF8Line
import kotlinx.serialization.decodeFromString

public data class RealtimeServerSentEvent(
    val id: String?,
    val event: String?,
    val data: String,
    val retryMillis: Long?,
)

public suspend inline fun <reified T> HttpClient.realtimeSseJson(
    endpoint: RealtimeEndpoint.ServerSentEvents,
    crossinline onEvent: suspend (T) -> Unit,
) {
    realtimeSse(endpoint) { event ->
        onEvent(endpoint.json.decodeFromString<T>(event.data))
    }
}

public suspend inline fun <reified T> HttpClient.realtimeSseJsonResult(
    endpoint: RealtimeEndpoint.ServerSentEvents,
    crossinline onEvent: suspend (T) -> Unit,
): RealtimeResult<Unit> {
    return try {
        realtimeSseJson(endpoint, onEvent)
        realtimeSuccess(Unit)
    } catch (cause: Throwable) {
        realtimeFailure(RealtimeProtocol.ServerSentEvents, cause)
    }
}

public suspend fun HttpClient.realtimeSse(
    endpoint: RealtimeEndpoint.ServerSentEvents,
    onEvent: suspend (RealtimeServerSentEvent) -> Unit,
) {
    prepareGet(endpoint.url) {
        header(HttpHeaders.Accept, "text/event-stream")
        endpoint.lastEventId?.let { header("Last-Event-ID", it) }
    }.execute { response ->
        val channel = response.body<io.ktor.utils.io.ByteReadChannel>()
        var id: String? = null
        var event: String? = null
        var retry: Long? = null
        val data = mutableListOf<String>()

        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            if (line.isEmpty()) {
                if (data.isNotEmpty()) {
                    onEvent(
                        RealtimeServerSentEvent(
                            id = id,
                            event = event,
                            data = data.joinToString("\n"),
                            retryMillis = retry,
                        ),
                    )
                }
                id = null
                event = null
                retry = null
                data.clear()
                continue
            }
            if (line.startsWith(":")) continue

            val separatorIndex = line.indexOf(':')
            val field = if (separatorIndex == -1) line else line.substring(0, separatorIndex)
            val rawValue = if (separatorIndex == -1) "" else line.substring(separatorIndex + 1).trimStart()

            when (field) {
                "id" -> id = rawValue
                "event" -> event = rawValue
                "data" -> data += rawValue
                "retry" -> retry = rawValue.toLongOrNull()
            }
        }
    }
}

public suspend fun HttpClient.realtimeSseResult(
    endpoint: RealtimeEndpoint.ServerSentEvents,
    onEvent: suspend (RealtimeServerSentEvent) -> Unit,
): RealtimeResult<Unit> {
    return try {
        realtimeSse(endpoint, onEvent)
        realtimeSuccess(Unit)
    } catch (cause: Throwable) {
        realtimeFailure(RealtimeProtocol.ServerSentEvents, cause)
    }
}
