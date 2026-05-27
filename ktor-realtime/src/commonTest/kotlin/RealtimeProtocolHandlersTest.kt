import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RealtimeProtocolHandlersTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun test_socketIo_openFrame_returnsNamespaceConnectMessage() {
        val endpoint = RealtimeEndpoint.SocketIo(url = "wss://example.com", namespace = "/chat")
        val action = handleSocketIoFrame("0{\"sid\":\"x\"}", endpoint, json)

        val send = assertIs<SocketIoFrameAction.SendText>(action)
        assertEquals("40/chat", send.text)
    }

    @Test
    fun test_socketIo_eventFrame_emitsPayload() {
        val endpoint = RealtimeEndpoint.SocketIo(url = "wss://example.com", eventName = "message")
        val action = handleSocketIoFrame("42[\"message\",{\"text\":\"hi\"}]", endpoint, json)

        assertIs<SocketIoFrameAction.EmitEvent>(action)
    }

    @Test
    fun test_reverb_ping_returnsPongMessage() {
        val endpoint = RealtimeEndpoint.LaravelReverb(url = "wss://example.com", appKey = "app")
        val action = handleReverbFrame("""{"event":"pusher:ping","data":{}}""", endpoint, json)

        val send = assertIs<ReverbFrameAction.SendText>(action)
        assertTrue(send.text.contains("pusher:pong"))
    }

    @Test
    fun test_reverb_dataFrame_decodesIncoming() {
        val endpoint = RealtimeEndpoint.LaravelReverb(url = "wss://example.com", appKey = "app")
        val action =
            handleReverbFrame(
                """{"event":"client-message","data":{"id":5}}""",
                endpoint,
                json,
            )

        val emit = assertIs<ReverbFrameAction.EmitEvent>(action)
        val payload = decodeReverbIncoming<JsonObject>(emit.dataRaw, json)
        assertEquals(5, payload["id"]?.jsonPrimitive?.int)
    }

    @Test
    fun test_stomp_connectedFrame_returnsSubscribeAction() {
        val endpoint = RealtimeEndpoint.Stomp(url = "wss://example.com", destination = "/topic/live")
        val actions = handleStompFrames("CONNECTED\nversion:1.2\n\n\u0000", endpoint)

        assertEquals(1, actions.size)
        assertTrue(actions[0].outboundText?.contains("SUBSCRIBE") == true)
        assertTrue(actions[0].outboundText?.contains("/topic/live") == true)
    }

    @Test
    fun test_stomp_messageFrame_extractsBody() {
        val endpoint = RealtimeEndpoint.Stomp(url = "wss://example.com")
        val actions = handleStompFrames("MESSAGE\nsubscription:0\n\n{\"id\":9}\u0000", endpoint)

        assertEquals("""{"id":9}""", actions.first().messageBody)
    }

    @Test
    fun test_sse_parseBlock_buildsEvent() {
        val event =
            parseSseEventBlock(
                listOf(
                    "id: 1",
                    "event: update",
                    "data: line1",
                    "data: line2",
                    "retry: 3000",
                ),
            )

        assertEquals("1", event?.id)
        assertEquals("update", event?.event)
        assertEquals("line1\nline2", event?.data)
        assertEquals(3000L, event?.retryMillis)
    }

    @Test
    fun test_sse_parseBlock_withoutData_returnsNull() {
        val event = parseSseEventBlock(listOf("id: 1", "event: ping"))
        assertNull(event)
    }

    @Test
    fun test_mqtt_decodeIncoming_decodesPayload() {
        val decoded = decodeMqttIncoming<JsonObject>("""{"id":3}""", json)
        assertEquals(3, decoded["id"]?.jsonPrimitive?.int)
    }

    @Test
    fun test_rsocket_decodeIncoming_decodesPayload() {
        val decoded = decodeRSocketIncoming<JsonObject>("""{"id":4}""", json)
        assertEquals(4, decoded["id"]?.jsonPrimitive?.int)
    }

    @Test
    fun test_longPolling_decodeIncoming_decodesPayload() {
        val decoded = decodeLongPollingIncoming<JsonObject>("""{"id":6}""", json)
        assertEquals(6, decoded["id"]?.jsonPrimitive?.int)
}
}
