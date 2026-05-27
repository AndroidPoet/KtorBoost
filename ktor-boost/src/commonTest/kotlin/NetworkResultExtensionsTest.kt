import io.ktor.http.Headers
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NetworkResultExtensionsTest {
    @Test
    fun test_map_success_transformsBodyAndKeepsMetadata() {
        val result = NetworkResult.Success("body", 201, Headers.Empty)

        val mapped = result.map { it.length }

        assertTrue(mapped.isSuccess)
        assertEquals(4, mapped.getOrNull())
        assertEquals(201, mapped.statusCodeOrNull())
    }

    @Test
    fun test_mapError_httpError_transformsDecodedError() {
        val result =
            NetworkResult.HttpError(
                statusCode = 400,
                rawBody = "bad",
                errorBody = "bad",
                headers = Headers.Empty,
            )

        val mapped = result.mapError { it.uppercase() }

        assertTrue(mapped.isHttpError)
        assertEquals("BAD", mapped.errorOrNull())
        assertEquals(400, mapped.statusCodeOrNull())
    }

    @Test
    fun test_accessors_requestError_returnExpectedValues() {
        val cause = IllegalStateException("network")
        val result: NetworkResult<String, String> = NetworkResult.RequestError(cause)

        assertTrue(result.isRequestError)
        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
        assertNull(result.getOrNull())
        assertEquals(cause, result.causeOrNull())
        assertNull(result.statusCodeOrNull())
        assertNull(result.headersOrNull())
    }

    @Test
    fun test_onResponseDecodingError_callsOnlyForResponseDecodingError() {
        val responseDecodingError =
            NetworkResult.ResponseDecodingError(
                statusCode = 200,
                headers = Headers.Empty,
                cause = IllegalArgumentException("decode"),
            )
        var called = false

        responseDecodingError.onResponseDecodingError {
            called = true
            assertEquals(200, it.statusCode)
        }

        assertTrue(called)
    }

    @Test
    fun test_onRequestError_callsOnlyForRequestError() {
        val requestError = NetworkResult.RequestError(IllegalStateException("offline"))
        var called = false

        requestError.onRequestError {
            called = true
            assertEquals("offline", it.cause.message)
        }

        assertTrue(called)
    }

    @Test
    fun test_onFailureSuspend_successDoesNotInvokeAction() = runBlocking {
        val success: NetworkResult<String, String> = NetworkResult.Success("ok", 200, Headers.Empty)
        var called = false

        success.onFailureSuspend {
            called = true
        }

        assertFalse(called)
    }

    @Test
    fun test_onFailureSuspend_requestErrorInvokesAction() = runBlocking {
        val error: NetworkResult<String, String> = NetworkResult.RequestError(IllegalStateException("offline"))
        var called = false

        error.onFailureSuspend {
            called = true
        }

        assertTrue(called)
    }

    @Test
    fun test_onError_aliasInvokesForFailure() {
        val error: NetworkResult<String, String> = NetworkResult.RequestError(IllegalStateException("offline"))
        var called = false

        error.onError {
            called = true
        }

        assertTrue(called)
    }

    @Test
    fun test_onErrorSuspend_aliasInvokesForFailure() = runBlocking {
        val error: NetworkResult<String, String> = NetworkResult.RequestError(IllegalStateException("offline"))
        var called = false

        error.onErrorSuspend {
            called = true
        }

        assertTrue(called)
    }
}
