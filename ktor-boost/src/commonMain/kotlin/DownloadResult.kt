import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlin.coroutines.cancellation.CancellationException

public data class DownloadProgress(
    val bytesRead: Long,
    val totalBytes: Long?,
) {
    public val fraction: Double?
        get() = totalBytes?.takeIf { it > 0L }?.let { bytesRead.toDouble() / it.toDouble() }
}

public data class DownloadedContent(
    val bytes: ByteArray,
    val statusCode: Int,
    val headers: Headers,
    val contentLength: Long?,
    val contentType: ContentType?,
) {
    public val size: Int
        get() = bytes.size
}

public sealed class DownloadResult {
    public data class Success(
        val content: DownloadedContent,
    ) : DownloadResult()

    public data class HttpError(
        val statusCode: Int,
        val rawBody: String?,
        val headers: Headers,
    ) : DownloadResult()

    public data class RequestError(
        val cause: Throwable,
    ) : DownloadResult()
}

public suspend fun HttpClient.downloadBytes(
    urlString: String,
    bufferSize: Int = DEFAULT_DOWNLOAD_BUFFER_SIZE,
    block: HttpRequestBuilder.() -> Unit = {},
    onProgress: suspend (DownloadProgress) -> Unit = {},
): DownloadResult {
    require(bufferSize > 0) { "bufferSize must be greater than 0." }

    return try {
        val response =
            request(urlString) {
                method = HttpMethod.Get
                block()
            }

        if (response.status.value !in 200..299) {
            return DownloadResult.HttpError(
                statusCode = response.status.value,
                rawBody = response.bodyAsText().takeIf { it.isNotEmpty() },
                headers = response.headers,
            )
        }

        val bytes = response.readBytesWithProgress(bufferSize, onProgress)
        DownloadResult.Success(
            DownloadedContent(
                bytes = bytes,
                statusCode = response.status.value,
                headers = response.headers,
                contentLength = response.contentLength(),
                contentType = response.headers[HttpHeaders.ContentType]?.let(ContentType::parse),
            ),
        )
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: ResponseException) {
        DownloadResult.HttpError(
            statusCode = exception.response.status.value,
            rawBody = exception.response.bodyAsText().takeIf { it.isNotEmpty() },
            headers = exception.response.headers,
        )
    } catch (cause: Throwable) {
        DownloadResult.RequestError(cause)
    }
}

public fun DownloadResult.getOrNull(): DownloadedContent? {
    return when (this) {
        is DownloadResult.Success -> content
        else -> null
    }
}

public fun DownloadResult.errorOrNull(): Throwable? {
    return when (this) {
        is DownloadResult.RequestError -> cause
        else -> null
    }
}

public fun DownloadResult.statusCodeOrNull(): Int? {
    return when (this) {
        is DownloadResult.Success -> content.statusCode
        is DownloadResult.HttpError -> statusCode
        is DownloadResult.RequestError -> null
    }
}

private suspend fun io.ktor.client.statement.HttpResponse.readBytesWithProgress(
    bufferSize: Int,
    onProgress: suspend (DownloadProgress) -> Unit,
): ByteArray {
    val channel = bodyAsChannel()
    val buffer = ByteArray(bufferSize)
    val chunks = mutableListOf<ByteArray>()
    val totalBytes = contentLength()
    var bytesRead = 0L

    while (!channel.isClosedForRead) {
        val read = channel.readAvailable(buffer, 0, buffer.size)
        if (read == -1) break
        if (read == 0) continue

        chunks += buffer.copyOf(read)
        bytesRead += read
        onProgress(DownloadProgress(bytesRead, totalBytes))
    }

    return chunks.toByteArray(bytesRead.toInt())
}

private fun List<ByteArray>.toByteArray(size: Int): ByteArray {
    val bytes = ByteArray(size)
    var offset = 0

    forEach { chunk ->
        chunk.copyInto(bytes, destinationOffset = offset)
        offset += chunk.size
    }

    return bytes
}

private const val DEFAULT_DOWNLOAD_BUFFER_SIZE = 8 * 1024
