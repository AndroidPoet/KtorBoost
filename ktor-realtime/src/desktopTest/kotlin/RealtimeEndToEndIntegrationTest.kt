import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RealtimeEndToEndIntegrationTest {
    private val baseUrl = "http://127.0.0.1:18080"
    private val wsUrl = "ws://127.0.0.1:18080/ws"
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun test_webSocket_realtime_receivesEchoPayload() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = HttpClient(CIO)
            var received: Message? = null

            try {
                client.realtime<Message, Message>(
                    urlString = wsUrl,
                    json = json,
                    onEvent = { received = it },
                ) {
                    sendJson(Message(42))
                    close()
                }
            } finally {
                client.close()
            }

            assertEquals(42, received?.id)
        }

    @Test
    fun test_sse_realtime_receivesServerEvent() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = HttpClient(CIO)
            var received: Message? = null

            try {
                client.realtimeSseJson(
                    endpoint = RealtimeEndpoint.ServerSentEvents("$baseUrl/sse", json),
                    onEvent = { message: Message -> received = message },
                )
            } finally {
                client.close()
            }

            assertEquals(7, received?.id)
        }

    @Test
    fun test_longPolling_realtimeResult_returnsFailureWhenCancelled() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = HttpClient(CIO)

            try {
                val result =
                    client.realtimeLongPollingResult<Message>(
                        endpoint = RealtimeEndpoint.LongPolling(
                            url = "$baseUrl/poll",
                            json = json,
                            intervalMillis = 5,
                        ),
                        onEvent = { throw IntegrationStop() },
                    )

                assertTrue(result is RealtimeResult.Failure)
            } finally {
                client.close()
            }
        }
}

@Serializable
private data class Message(
    val id: Int,
)

private class IntegrationStop : RuntimeException("stop integration loop")

private fun integrationEnabled(): Boolean = System.getenv("KTORBOOST_RUN_INTEGRATION") == "true"
