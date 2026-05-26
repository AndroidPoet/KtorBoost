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

class DownloadResultTest {
    @Test
    fun test_downloadBytes_success_returnsBytesAndProgress() {
        val httpClient =
            HttpClient(
                MockEngine {
                    respond(
                        content = "download",
                        status = HttpStatusCode.OK,
                        headers =
                            headersOf(
                                HttpHeaders.ContentType to listOf("text/plain"),
                                HttpHeaders.ContentLength to listOf("8"),
                            ),
                    )
                },
            )
        val progressEvents = mutableListOf<DownloadProgress>()

        val result =
            runBlocking {
                httpClient.downloadBytes(
                    urlString = "sample_download_url",
                    bufferSize = 3,
                    onProgress = { progressEvents += it },
                )
            }

        val success = assertIs<DownloadResult.Success>(result)
        assertEquals("download", success.content.bytes.decodeToString())
        assertEquals(8, success.content.size)
        assertEquals(8, success.content.contentLength)
        assertEquals("text/plain", success.content.contentType.toString())
        assertEquals(8, progressEvents.last().bytesRead)
        assertEquals(8, progressEvents.last().totalBytes)
    }

    @Test
    fun test_downloadBytes_httpError_returnsRawErrorBody() {
        val httpClient =
            HttpClient(
                MockEngine {
                    respondError(HttpStatusCode.NotFound, "missing")
                },
            )

        val result =
            runBlocking {
                httpClient.downloadBytes("sample_download_url")
            }

        val error = assertIs<DownloadResult.HttpError>(result)
        assertEquals(HttpStatusCode.NotFound.value, error.statusCode)
        assertEquals("missing", error.rawBody)
    }

    @Test
    fun test_downloadBytes_httpErrorWithExpectSuccess_returnsRawErrorBody() {
        val httpClient =
            HttpClient(
                MockEngine {
                    respondError(HttpStatusCode.Forbidden, "forbidden")
                },
            ) {
                expectSuccess = true
            }

        val result =
            runBlocking {
                httpClient.downloadBytes("sample_download_url")
            }

        val error = assertIs<DownloadResult.HttpError>(result)
        assertEquals(HttpStatusCode.Forbidden.value, error.statusCode)
        assertEquals("forbidden", error.rawBody)
    }

    @Test
    fun test_downloadBytes_cancellation_rethrowsCancellationException() {
        val httpClient =
            HttpClient(
                MockEngine {
                    throw CancellationException("cancelled")
                },
            )

        assertFailsWith<CancellationException> {
            runBlocking {
                httpClient.downloadBytes("sample_download_url")
            }
        }
    }
}
