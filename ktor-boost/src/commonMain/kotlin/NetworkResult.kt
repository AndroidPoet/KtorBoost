import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlin.coroutines.cancellation.CancellationException

public sealed class NetworkResult<out T, out E> {
    public data class Success<T>(
        public val body: T,
        public val statusCode: Int,
        public val headers: Headers,
    ) : NetworkResult<T, Nothing>()

    public data class HttpError<E>(
        public val statusCode: Int,
        public val rawBody: String?,
        public val errorBody: E?,
        public val headers: Headers,
        public val decodeFailure: Throwable? = null,
    ) : NetworkResult<Nothing, E>()

    public data class ResponseDecodingError(
        public val statusCode: Int,
        public val headers: Headers,
        public val cause: Throwable,
    ) : NetworkResult<Nothing, Nothing>()

    public data class RequestError(
        public val cause: Throwable,
    ) : NetworkResult<Nothing, Nothing>()
}

public suspend inline fun <reified T, E> HttpClient.requestNetworkResult(
    method: HttpMethod,
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
    successStatusCodes: IntRange = 200..299,
): NetworkResult<T, E> {
    return try {
        val response =
            request(urlString) {
                this.method = method
                block()
            }

        if (response.status.value !in successStatusCodes) {
            return response.toHttpError(decodeErrorBody)
        }

        try {
            NetworkResult.Success(
                body = response.bodyOrUnit(),
                statusCode = response.status.value,
                headers = response.headers,
            )
        } catch (cause: Throwable) {
            NetworkResult.ResponseDecodingError(
                statusCode = response.status.value,
                headers = response.headers,
                cause = cause,
            )
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: ResponseException) {
        exception.response.toHttpError(decodeErrorBody)
    } catch (cause: Throwable) {
        NetworkResult.RequestError(cause)
    }
}

public suspend inline fun <reified T> HttpClient.requestRawNetworkResult(
    method: HttpMethod,
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, Nothing> = requestNetworkResult(method, urlString, block)

public suspend inline fun <reified T, E> HttpClient.getNetworkResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
    successStatusCodes: IntRange = 200..299,
): NetworkResult<T, E> = requestNetworkResult(HttpMethod.Get, urlString, block, decodeErrorBody, successStatusCodes)

public suspend inline fun <reified T, E> HttpClient.postNetworkResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
    successStatusCodes: IntRange = 200..299,
): NetworkResult<T, E> = requestNetworkResult(HttpMethod.Post, urlString, block, decodeErrorBody, successStatusCodes)

public suspend inline fun <reified T, E> HttpClient.putNetworkResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
    successStatusCodes: IntRange = 200..299,
): NetworkResult<T, E> = requestNetworkResult(HttpMethod.Put, urlString, block, decodeErrorBody, successStatusCodes)

public suspend inline fun <reified T, E> HttpClient.deleteNetworkResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
    successStatusCodes: IntRange = 200..299,
): NetworkResult<T, E> = requestNetworkResult(HttpMethod.Delete, urlString, block, decodeErrorBody, successStatusCodes)

public suspend inline fun <reified T, E> HttpClient.patchNetworkResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
    successStatusCodes: IntRange = 200..299,
): NetworkResult<T, E> = requestNetworkResult(HttpMethod.Patch, urlString, block, decodeErrorBody, successStatusCodes)

public suspend inline fun <reified T, E> HttpClient.headNetworkResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
    successStatusCodes: IntRange = 200..299,
): NetworkResult<T, E> = requestNetworkResult(HttpMethod.Head, urlString, block, decodeErrorBody, successStatusCodes)

public suspend inline fun <reified T, E> HttpClient.optionsNetworkResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
    successStatusCodes: IntRange = 200..299,
): NetworkResult<T, E> = requestNetworkResult(HttpMethod.Options, urlString, block, decodeErrorBody, successStatusCodes)

@PublishedApi
internal suspend inline fun <E> io.ktor.client.statement.HttpResponse.toHttpError(
    decodeErrorBody: suspend (String) -> E?,
): NetworkResult.HttpError<E> {
    val rawBody = bodyAsText().takeIf { it.isNotEmpty() }
    val decodedError =
        if (rawBody == null) {
            DecodedError<E>(value = null, failure = null)
        } else {
            try {
                DecodedError(value = decodeErrorBody(rawBody), failure = null)
            } catch (cause: Throwable) {
                DecodedError(value = null, failure = cause)
            }
        }

    return NetworkResult.HttpError(
        statusCode = status.value,
        rawBody = rawBody,
        errorBody = decodedError.value,
        headers = headers,
        decodeFailure = decodedError.failure,
    )
}

@PublishedApi
internal data class DecodedError<E>(
    val value: E?,
    val failure: Throwable?,
)
