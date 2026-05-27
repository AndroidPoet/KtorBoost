import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.errors.IOException

public sealed class BoostError {
    public abstract val message: String

    public data class Unauthorized(
        val statusCode: Int,
    ) : BoostError() {
        override val message: String = "Authentication is required."
    }

    public data class Forbidden(
        val statusCode: Int,
    ) : BoostError() {
        override val message: String = "The request is not allowed."
    }

    public data class NotFound(
        val statusCode: Int,
    ) : BoostError() {
        override val message: String = "The requested resource was not found."
    }

    public data class RateLimited(
        val statusCode: Int,
        val retryAfterSeconds: Long?,
    ) : BoostError() {
        override val message: String = "Too many requests. Try again later."
    }

    public data class ServerError(
        val statusCode: Int,
    ) : BoostError() {
        override val message: String = "The server is currently unavailable."
    }

    public data class HttpFailure(
        val statusCode: Int,
    ) : BoostError() {
        override val message: String = "The request failed with HTTP $statusCode."
    }

    public data class Timeout(
        val cause: Throwable,
    ) : BoostError() {
        override val message: String = "The request timed out."
    }

    public data class Offline(
        val cause: Throwable,
    ) : BoostError() {
        override val message: String = "The network connection is unavailable."
    }

    public data class ResponseDecodingFailed(
        val cause: Throwable,
    ) : BoostError() {
        override val message: String = "The response could not be decoded."
    }

    public data class ErrorBodyDecodingFailed(
        val cause: Throwable,
    ) : BoostError() {
        override val message: String = "The error response could not be decoded."
    }

    public data class Unknown(
        val cause: Throwable?,
    ) : BoostError() {
        override val message: String = "Something went wrong."
    }
}

public val NetworkResult<*, *>.boostError: BoostError?
    get() =
        when (this) {
            is NetworkResult.Success -> null
            is NetworkResult.HttpError<*> ->
                decodeFailure?.let { BoostError.ErrorBodyDecodingFailed(it) }
                    ?: classifyHttpError(statusCode, headers)
            is NetworkResult.ResponseDecodingError -> BoostError.ResponseDecodingFailed(cause)
            is NetworkResult.RequestError -> classifyRequestError(cause)
        }

public val NetworkResult<*, *>.messageOrNull: String?
    get() = boostError?.message

public fun classifyHttpError(
    statusCode: Int,
    headers: Headers = Headers.Empty,
): BoostError =
    when (statusCode) {
        401 -> BoostError.Unauthorized(statusCode)
        403 -> BoostError.Forbidden(statusCode)
        404 -> BoostError.NotFound(statusCode)
        429 -> BoostError.RateLimited(statusCode, headers.retryAfterSeconds())
        in 500..599 -> BoostError.ServerError(statusCode)
        else -> BoostError.HttpFailure(statusCode)
    }

public fun classifyRequestError(cause: Throwable): BoostError =
    when (cause) {
        is HttpRequestTimeoutException -> BoostError.Timeout(cause)
        is IOException -> BoostError.Offline(cause)
        else -> BoostError.Unknown(cause)
    }

private fun Headers.retryAfterSeconds(): Long? =
    get(HttpHeaders.RetryAfter)?.toLongOrNull()
