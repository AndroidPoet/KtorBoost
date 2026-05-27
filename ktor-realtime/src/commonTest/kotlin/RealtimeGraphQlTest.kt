import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RealtimeGraphQlTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun test_handleGraphQlFrame_connectionAck_invokesHook() {
        var called = false
        val hooks = GraphQlFrameHooks(onConnectionAck = { called = true })

        runSuspend {
            handleGraphQlFrame<JsonObject>(
                raw = """{"type":"connection_ack"}""",
                json = json,
                onEvent = {},
                hooks = hooks,
            )
        }

        assertTrue(called)
    }

    @Test
    fun test_handleGraphQlFrame_next_decodesIncomingPayload() {
        var id = -1

        runSuspend {
            handleGraphQlFrame<JsonObject>(
                raw = """{"type":"next","payload":{"data":{"id":7}}}""",
                json = json,
                onEvent = { id = it["id"]?.jsonPrimitive?.int ?: -1 },
                hooks = GraphQlFrameHooks(),
            )
        }

        assertEquals(7, id)
    }

    @Test
    fun test_handleGraphQlFrame_error_invokesProtocolErrorHook() {
        var hasErrorType = false
        val hooks =
            GraphQlFrameHooks(
                onProtocolError = { packet: JsonObject ->
                    hasErrorType = packet["type"]?.toString()?.contains("error") == true
                },
            )

        runSuspend {
            handleGraphQlFrame<JsonObject>(
                raw = """{"type":"error","payload":{"message":"boom"}}""",
                json = json,
                onEvent = {},
                hooks = hooks,
            )
        }

        assertTrue(hasErrorType)
    }

    @Test
    fun test_handleGraphQlFrame_complete_invokesCompleteHook() {
        var called = false
        val hooks = GraphQlFrameHooks(onComplete = { called = true })

        runSuspend {
            handleGraphQlFrame<JsonObject>(
                raw = """{"type":"complete"}""",
                json = json,
                onEvent = {},
                hooks = hooks,
            )
        }

        assertTrue(called)
    }
}

private fun runSuspend(block: suspend () -> Unit) {
    block.startCoroutine(
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                result.getOrThrow()
            }
        },
    )
}
