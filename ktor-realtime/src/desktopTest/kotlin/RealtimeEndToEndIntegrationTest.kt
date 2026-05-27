import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
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
            val client =
                HttpClient(CIO) {
                    install(WebSockets)
                }
            var receivedId: Int? = null

            try {
                client.realtime<JsonObject, String>(
                    urlString = wsUrl,
                    json = json,
                    onEvent = { receivedId = it["id"]?.jsonPrimitive?.int },
                ) {
                    sendText("""{"id":42}""")
                    delay(100)
                    close()
                }
            } finally {
                client.close()
            }

            assertEquals(42, receivedId)
        }

    @Test
    fun test_sse_realtime_receivesServerEvent() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = HttpClient(CIO)
            var receivedId: Int? = null

            try {
                client.realtimeSseJson(
                    endpoint = RealtimeEndpoint.ServerSentEvents("$baseUrl/sse", json),
                    onEvent = { message: JsonObject -> receivedId = message["id"]?.jsonPrimitive?.int },
                )
            } finally {
                client.close()
            }

            assertEquals(7, receivedId)
        }

    @Test
    fun test_longPolling_realtimeResult_returnsFailureWhenCancelled() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = HttpClient(CIO)

            try {
                val result =
                    client.realtimeLongPollingResult<JsonObject>(
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

    @Test
    fun test_socketIo_realtime_receivesEvent() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = wsClient()
            var receivedId: Int? = null
            try {
                client.realtimeSocketIo<JsonObject, String>(
                    endpoint = RealtimeEndpoint.SocketIo(url = "ws://127.0.0.1:18080/socketio", eventName = "message"),
                    onEvent = { receivedId = it["id"]?.jsonPrimitive?.int },
                ) {
                    delay(100)
                    close()
                }
            } finally {
                client.close()
            }
            assertEquals(21, receivedId)
        }

    @Test
    fun test_stomp_realtime_receivesMessage() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = wsClient()
            var receivedId: Int? = null
            try {
                client.realtimeStomp<JsonObject, String>(
                    endpoint = RealtimeEndpoint.Stomp(url = "ws://127.0.0.1:18080/stomp", destination = "/topic/live"),
                    onEvent = { receivedId = it["id"]?.jsonPrimitive?.int },
                ) {
                    delay(120)
                    close()
                }
            } finally {
                client.close()
            }
            assertEquals(22, receivedId)
        }

    @Test
    fun test_graphQl_realtime_receivesNextPayload() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = wsClient()
            var receivedId: Int? = null
            try {
                client.realtimeGraphQlSubscriptions<JsonObject, String>(
                    endpoint =
                        RealtimeEndpoint.GraphQlSubscriptions(
                            url = "ws://127.0.0.1:18080/graphql",
                            query = "subscription { ping }",
                        ),
                    onEvent = { receivedId = it["id"]?.jsonPrimitive?.int },
                ) {
                    delay(120)
                    close()
                }
            } finally {
                client.close()
            }
            assertEquals(23, receivedId)
        }

    @Test
    fun test_reverb_realtime_receivesDataEvent() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = wsClient()
            var receivedId: Int? = null
            try {
                client.realtimeReverb<JsonObject, String>(
                    endpoint =
                        RealtimeEndpoint.LaravelReverb(
                            url = "ws://127.0.0.1:18080/reverb",
                            appKey = "local",
                            channel = "public-chat",
                        ),
                    onEvent = { receivedId = it["id"]?.jsonPrimitive?.int },
                ) {
                    delay(120)
                    close()
                }
            } finally {
                client.close()
            }
            assertEquals(24, receivedId)
        }

    @Test
    fun test_mqttOverWebSocket_realtime_receivesPayload() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = wsClient()
            var receivedId: Int? = null
            try {
                client.realtimeMqttOverWebSocket<JsonObject, String>(
                    endpoint = RealtimeEndpoint.MqttOverWebSocket(url = "ws://127.0.0.1:18080/mqtt", clientId = "client-1"),
                    onEvent = { receivedId = it["id"]?.jsonPrimitive?.int },
                ) {
                    delay(80)
                    close()
                }
            } finally {
                client.close()
            }
            assertEquals(25, receivedId)
        }

    @Test
    fun test_rsocketOverWebSocket_realtime_receivesPayload() =
        runBlocking {
            if (!integrationEnabled()) return@runBlocking
            val client = wsClient()
            var receivedId: Int? = null
            try {
                client.realtimeRSocket<JsonObject, String>(
                    endpoint = RealtimeEndpoint.RSocket(url = "ws://127.0.0.1:18080/rsocket"),
                    onEvent = { receivedId = it["id"]?.jsonPrimitive?.int },
                ) {
                    delay(80)
                    close()
                }
            } finally {
                client.close()
            }
            assertEquals(26, receivedId)
        }
}

private class IntegrationStop : RuntimeException("stop integration loop")

private fun integrationEnabled(): Boolean = System.getenv("KTORBOOST_RUN_INTEGRATION") == "true"

private fun wsClient(): HttpClient =
    HttpClient(CIO) {
        install(WebSockets)
    }
