import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class AuthTokenRefreshTest {
    @Test
    fun test_getAuthenticatedNetworkResult_unauthorizedRefreshesTokenAndReplaysRequest() {
        val tokenProvider = FakeBearerTokenProvider(currentToken = "expired", refreshedToken = "fresh")
        val seenAuthorizationHeaders = mutableListOf<String?>()
        val httpClient =
            HttpClient(
                MockEngine { request ->
                    seenAuthorizationHeaders += request.headers[HttpHeaders.Authorization]
                    if (seenAuthorizationHeaders.size == 1) {
                        respondError(HttpStatusCode.Unauthorized, "expired")
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
                httpClient.getAuthenticatedNetworkResult<String, String>(
                    urlString = "sample_auth_url",
                    tokenProvider = tokenProvider,
                    decodeErrorBody = { it },
                )
            }

        val success = assertIs<NetworkResult.Success<String>>(result)
        assertEquals("success", success.body)
        assertEquals(listOf<String?>("Bearer expired", "Bearer fresh"), seenAuthorizationHeaders)
        assertEquals(1, tokenProvider.refreshCount)
        assertEquals(0, tokenProvider.clearCount)
    }

    @Test
    fun test_getAuthenticatedNetworkResult_missingTokenRefreshesBeforeFirstRequest() {
        val tokenProvider = FakeBearerTokenProvider(currentToken = null, refreshedToken = "fresh")
        val seenAuthorizationHeaders = mutableListOf<String?>()
        val httpClient =
            HttpClient(
                MockEngine { request ->
                    seenAuthorizationHeaders += request.headers[HttpHeaders.Authorization]
                    respond(
                        content = "success",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    )
                },
            )

        val result =
            runBlocking {
                httpClient.getAuthenticatedNetworkResult<String, Nothing>(
                    urlString = "sample_auth_url",
                    tokenProvider = tokenProvider,
                )
            }

        val success = assertIs<NetworkResult.Success<String>>(result)
        assertEquals("success", success.body)
        assertEquals(listOf<String?>("Bearer fresh"), seenAuthorizationHeaders)
        assertEquals(1, tokenProvider.refreshCount)
    }

    @Test
    fun test_getAuthenticatedNetworkResult_repeatedUnauthorizedClearsTokenAndReturnsHttpError() {
        val tokenProvider = FakeBearerTokenProvider(currentToken = "expired", refreshedToken = "still-expired")
        val httpClient =
            HttpClient(
                MockEngine {
                    respondError(HttpStatusCode.Unauthorized, "expired")
                },
            )

        val result =
            runBlocking {
                httpClient.getAuthenticatedNetworkResult<String, String>(
                    urlString = "sample_auth_url",
                    tokenProvider = tokenProvider,
                    decodeErrorBody = { it },
                )
            }

        val error = assertIs<NetworkResult.HttpError<String>>(result)
        assertEquals(HttpStatusCode.Unauthorized.value, error.statusCode)
        assertEquals("expired", error.rawBody)
        assertEquals(1, tokenProvider.refreshCount)
        assertEquals(1, tokenProvider.clearCount)
    }

    @Test
    fun test_getAuthenticatedResult_successReturnsResultSuccess() {
        val tokenProvider = FakeBearerTokenProvider(currentToken = "fresh", refreshedToken = "fresh")
        val httpClient =
            HttpClient(
                MockEngine {
                    respond(
                        content = "success",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    )
                },
            )

        val result =
            runBlocking {
                httpClient.getAuthenticatedResult<String>(
                    urlString = "sample_auth_url",
                    tokenProvider = tokenProvider,
                )
            }

        assertEquals(Result.success("success"), result)
    }

    @Test
    fun test_getAuthenticatedNetworkResult_refreshExceptionReturnsRequestError() {
        val tokenProvider =
            FakeBearerTokenProvider(
                currentToken = null,
                refreshedToken = null,
                refreshFailure = IllegalStateException("refresh failed"),
            )
        val httpClient =
            HttpClient(
                MockEngine {
                    respond("should not request")
                },
            )

        val result =
            runBlocking {
                httpClient.getAuthenticatedNetworkResult<String, Nothing>(
                    urlString = "sample_auth_url",
                    tokenProvider = tokenProvider,
                )
            }

        val error = assertIs<NetworkResult.RequestError>(result)
        assertEquals("refresh failed", error.cause.message)
    }

    @Test
    fun test_getAuthenticatedNetworkResult_cancellationRethrowsCancellationException() {
        val tokenProvider =
            FakeBearerTokenProvider(
                currentToken = null,
                refreshedToken = null,
                refreshFailure = CancellationException("cancelled"),
            )
        val httpClient =
            HttpClient(
                MockEngine {
                    respond("should not request")
                },
            )

        assertFailsWith<CancellationException> {
            runBlocking {
                httpClient.getAuthenticatedNetworkResult<String, Nothing>(
                    urlString = "sample_auth_url",
                    tokenProvider = tokenProvider,
                )
            }
        }
    }

    private class FakeBearerTokenProvider(
        private var currentToken: String?,
        private val refreshedToken: String?,
        private val refreshFailure: Throwable? = null,
    ) : BearerTokenProvider {
        var refreshCount = 0
        var clearCount = 0

        override suspend fun currentToken(): String? = currentToken

        override suspend fun refreshToken(): String? {
            refreshCount++
            refreshFailure?.let { throw it }
            currentToken = refreshedToken
            return refreshedToken
        }

        override suspend fun clearToken() {
            clearCount++
            currentToken = null
        }
    }
}
