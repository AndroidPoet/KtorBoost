import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.expectSuccess
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class NetworkResultTest {
    @Test
    fun test_requestNetworkResult_successResponse_returnsBodyStatusAndHeaders() {
        val httpClient =
            HttpClient(
                MockEngine {
                    respond(
                        content = "success",
                        status = HttpStatusCode.Accepted,
                        headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    )
                },
            )

        val result =
            runBlocking {
                httpClient.requestRawNetworkResult<String>(io.ktor.http.HttpMethod.Get, "sample_get_url")
            }

        val success = assertIs<NetworkResult.Success<String>>(result)
        assertEquals("success", success.body)
        assertEquals(HttpStatusCode.Accepted.value, success.statusCode)
        assertEquals("text/plain", success.headers[HttpHeaders.ContentType])
    }

    @Test
    fun test_requestNetworkResult_httpErrorWithoutExpectSuccess_returnsRawAndDecodedErrorBody() {
        val httpClient =
            HttpClient(
                MockEngine {
                    respondError(HttpStatusCode.BadRequest, content = "invalid request")
                },
            )

        val result =
            runBlocking {
                httpClient.getNetworkResult<String, String>(
                    urlString = "sample_get_url",
                    decodeErrorBody = { "decoded: $it" },
                )
            }

        val error = assertIs<NetworkResult.HttpError<String>>(result)
        assertEquals(HttpStatusCode.BadRequest.value, error.statusCode)
        assertEquals("invalid request", error.rawBody)
        assertEquals("decoded: invalid request", error.errorBody)
        assertNull(error.decodeFailure)
    }

    @Test
    fun test_requestNetworkResult_httpErrorWithExpectSuccess_returnsRawErrorBody() {
        val httpClient =
            HttpClient(
                MockEngine {
                    respondError(HttpStatusCode.Unauthorized, content = "missing token")
                },
            ) {
                expectSuccess = true
            }

        val result =
            runBlocking {
                httpClient.requestRawNetworkResult<String>(io.ktor.http.HttpMethod.Get, "sample_get_url")
            }

        val error = assertIs<NetworkResult.HttpError<Nothing>>(result)
        assertEquals(HttpStatusCode.Unauthorized.value, error.statusCode)
        assertEquals("missing token", error.rawBody)
    }

    @Test
    fun test_requestResult_emptyResponseWithUnit_returnsSuccess() {
        val httpClient =
            HttpClient(
                MockEngine {
                    respond(
                        content = "",
                        status = HttpStatusCode.NoContent,
                    )
                },
            )

        val result =
            runBlocking {
                httpClient.requestResult<Unit>(io.ktor.http.HttpMethod.Delete, "sample_delete_url")
            }

        assertEquals(Result.success(Unit), result)
    }

    @Test
    fun test_requestNetworkResult_cancellation_rethrowsCancellationException() {
        val httpClient =
            HttpClient(
                MockEngine {
                    throw CancellationException("cancelled")
                },
            )

        assertFailsWith<CancellationException> {
            runBlocking {
                httpClient.requestRawNetworkResult<String>(io.ktor.http.HttpMethod.Get, "sample_get_url")
            }
        }
    }
}
