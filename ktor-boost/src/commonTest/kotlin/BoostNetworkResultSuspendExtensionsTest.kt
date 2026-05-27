import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BoostNetworkResultSuspendExtensionsTest {
    @Test
    fun test_onUnauthorizedSuspend_http401_invokesAction() {
        val result: NetworkResult<String, String> =
            NetworkResult.HttpError(
                statusCode = 401,
                rawBody = null,
                errorBody = null,
                headers = Headers.Empty,
            )
        var called = false

        runSuspend {
            result.onUnauthorizedSuspend {
                called = true
            }
        }

        assertTrue(called)
    }

    @Test
    fun test_onRateLimitedSuspend_http429_invokesActionWithRetryAfter() {
        val headers =
            Headers.build {
                append(HttpHeaders.RetryAfter, "12")
            }
        val result: NetworkResult<String, String> =
            NetworkResult.HttpError(
                statusCode = 429,
                rawBody = null,
                errorBody = null,
                headers = headers,
            )
        var retryAfter = -1L

        runSuspend {
            result.onRateLimitedSuspend { rateLimited, _ ->
                retryAfter = rateLimited.retryAfterSeconds ?: -1L
            }
        }

        assertEquals(12L, retryAfter)
    }

    @Test
    fun test_recoverRequestErrorSuspend_requestError_returnsSuccessFallback() {
        val result: NetworkResult<String, String> =
            NetworkResult.RequestError(IllegalStateException("offline"))
        lateinit var recovered: NetworkResult<String, String>

        runSuspend {
            recovered =
                result.recoverRequestErrorSuspend {
                    "cached"
                }
        }

        val success = assertIs<NetworkResult.Success<String>>(recovered)
        assertEquals("cached", success.body)
        assertEquals(RECOVERED_STATUS_CODE, success.statusCode)
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
