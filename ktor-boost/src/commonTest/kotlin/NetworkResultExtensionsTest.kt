import io.ktor.http.Headers
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
        assertFalse(result.isSuccess)
        assertNull(result.getOrNull())
        assertEquals(cause, result.causeOrNull())
        assertNull(result.statusCodeOrNull())
        assertNull(result.headersOrNull())
    }
}
