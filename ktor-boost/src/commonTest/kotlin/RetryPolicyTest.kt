import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.ZERO

class RetryPolicyTest {
    @Test
    fun test_getNetworkResultWithRetry_retryableHttpError_retriesUntilSuccess() {
        var requestCount = 0
        val httpClient =
            HttpClient(
                MockEngine {
                    requestCount++
                    if (requestCount == 1) {
                        respondError(HttpStatusCode.ServiceUnavailable, "try again")
                    } else {
                        respond(
                            content = "success",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                        )
                    }
                },
            )

        val result =
            runBlocking {
                httpClient.getNetworkResultWithRetry<String, Nothing>(
                    urlString = "sample_get_url",
                    retryPolicy = RetryPolicy(maxRetries = 1, initialDelay = ZERO),
                )
            }

        val success = assertIs<NetworkResult.Success<String>>(result)
        assertEquals("success", success.body)
        assertEquals(2, requestCount)
    }

    @Test
    fun test_requestResultWithRetry_requestFailure_retriesUntilSuccess() {
        var requestCount = 0
        val httpClient =
            HttpClient(
                MockEngine {
                    requestCount++
                    if (requestCount == 1) {
                        error("network")
                    } else {
                        respond(
                            content = "success",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                        )
                    }
                },
            )

        val result =
            runBlocking {
                httpClient.requestResultWithRetry<String>(
                    method = HttpMethod.Get,
                    urlString = "sample_get_url",
                    retryPolicy = RetryPolicy(maxRetries = 1, initialDelay = ZERO),
                )
            }

        assertEquals(Result.success("success"), result)
        assertEquals(2, requestCount)
    }
}
