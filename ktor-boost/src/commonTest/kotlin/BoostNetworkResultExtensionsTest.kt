import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BoostNetworkResultExtensionsTest {
    @Test
    fun test_boostError_httpUnauthorized_returnsUnauthorized() {
        val result: NetworkResult<String, String> =
            NetworkResult.HttpError(
                statusCode = 401,
                rawBody = null,
                errorBody = null,
                headers = Headers.Empty,
            )

        assertIs<BoostError.Unauthorized>(result.boostError)
    }

    @Test
    fun test_boostError_rateLimited_readsRetryAfterHeader() {
        val headers =
            Headers.build {
                append(HttpHeaders.RetryAfter, "30")
            }
        val result: NetworkResult<String, String> =
            NetworkResult.HttpError(
                statusCode = 429,
                rawBody = null,
                errorBody = null,
                headers = headers,
            )

        val error = assertIs<BoostError.RateLimited>(result.boostError)

        assertEquals(30, error.retryAfterSeconds)
    }

    @Test
    fun test_onUnauthorized_http401_invokesAction() {
        val result: NetworkResult<String, String> =
            NetworkResult.HttpError(
                statusCode = 401,
                rawBody = null,
                errorBody = null,
                headers = Headers.Empty,
            )
        var called = false

        result.onUnauthorized {
            called = true
        }

        assertTrue(called)
    }

    @Test
    fun test_recoverRequestError_requestError_returnsSuccessFallback() {
        val result: NetworkResult<String, String> =
            NetworkResult.RequestError(IllegalStateException("offline"))

        val recovered = result.recoverRequestError { "cached" }
        val success = assertIs<NetworkResult.Success<String>>(recovered)

        assertEquals("cached", success.body)
        assertEquals(RECOVERED_STATUS_CODE, success.statusCode)
    }
}
