import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public data class RetryPolicy(
    val maxRetries: Int = 3,
    val initialDelay: Duration = 500.seconds / 1000,
    val maxDelay: Duration = 5.seconds,
    val backoffFactor: Double = 2.0,
    val retryOnStatusCodes: Set<Int> = setOf(408, 429, 500, 502, 503, 504),
    val retryOnRequestError: Boolean = true,
)

public suspend inline fun <reified T> HttpClient.requestResultWithRetry(
    method: HttpMethod,
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> {
    return retryResult(retryPolicy) {
        if (timeout == null) {
            requestResult(method, urlString, block)
        } else {
            withTimeout(timeout) {
                requestResult(method, urlString, block)
            }
        }
    }
}

public suspend inline fun <reified T, E> HttpClient.requestNetworkResultWithRetry(
    method: HttpMethod,
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> {
    return retryNetworkResult(retryPolicy) {
        if (timeout == null) {
            requestNetworkResult(method, urlString, block, decodeErrorBody)
        } else {
            withTimeout(timeout) {
                requestNetworkResult(method, urlString, block, decodeErrorBody)
            }
        }
    }
}

public suspend inline fun <reified T> HttpClient.getResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResultWithRetry(HttpMethod.Get, urlString, retryPolicy, timeout, block)

public suspend inline fun <reified T> HttpClient.postResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResultWithRetry(HttpMethod.Post, urlString, retryPolicy, timeout, block)

public suspend inline fun <reified T> HttpClient.putResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResultWithRetry(HttpMethod.Put, urlString, retryPolicy, timeout, block)

public suspend inline fun <reified T> HttpClient.deleteResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResultWithRetry(HttpMethod.Delete, urlString, retryPolicy, timeout, block)

public suspend inline fun <reified T> HttpClient.patchResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResultWithRetry(HttpMethod.Patch, urlString, retryPolicy, timeout, block)

public suspend inline fun <reified T> HttpClient.headResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResultWithRetry(HttpMethod.Head, urlString, retryPolicy, timeout, block)

public suspend inline fun <reified T> HttpClient.optionsResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResultWithRetry(HttpMethod.Options, urlString, retryPolicy, timeout, block)

public suspend inline fun <reified T, E> HttpClient.getNetworkResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> = requestNetworkResultWithRetry(HttpMethod.Get, urlString, retryPolicy, timeout, block, decodeErrorBody)

public suspend inline fun <reified T, E> HttpClient.postNetworkResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> = requestNetworkResultWithRetry(HttpMethod.Post, urlString, retryPolicy, timeout, block, decodeErrorBody)

public suspend inline fun <reified T, E> HttpClient.putNetworkResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> = requestNetworkResultWithRetry(HttpMethod.Put, urlString, retryPolicy, timeout, block, decodeErrorBody)

public suspend inline fun <reified T, E> HttpClient.deleteNetworkResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> = requestNetworkResultWithRetry(HttpMethod.Delete, urlString, retryPolicy, timeout, block, decodeErrorBody)

public suspend inline fun <reified T, E> HttpClient.patchNetworkResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> = requestNetworkResultWithRetry(HttpMethod.Patch, urlString, retryPolicy, timeout, block, decodeErrorBody)

public suspend inline fun <reified T, E> HttpClient.headNetworkResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> = requestNetworkResultWithRetry(HttpMethod.Head, urlString, retryPolicy, timeout, block, decodeErrorBody)

public suspend inline fun <reified T, E> HttpClient.optionsNetworkResultWithRetry(
    urlString: String,
    retryPolicy: RetryPolicy = RetryPolicy(),
    timeout: Duration? = null,
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> = requestNetworkResultWithRetry(HttpMethod.Options, urlString, retryPolicy, timeout, block, decodeErrorBody)

@PublishedApi
internal suspend inline fun <T> retryResult(
    retryPolicy: RetryPolicy,
    block: () -> Result<T>,
): Result<T> {
    var attempt = 0
    var latestResult: Result<T>

    while (true) {
        latestResult = block()
        val shouldRetry = latestResult.isFailure && attempt < retryPolicy.maxRetries

        if (!shouldRetry) {
            return latestResult
        }

        delay(retryPolicy.delayForAttempt(attempt))
        attempt++
    }
}

@PublishedApi
internal suspend inline fun <T, E> retryNetworkResult(
    retryPolicy: RetryPolicy,
    block: () -> NetworkResult<T, E>,
): NetworkResult<T, E> {
    var attempt = 0
    var latestResult: NetworkResult<T, E>

    while (true) {
        latestResult = block()
        val shouldRetry =
            when (latestResult) {
                is NetworkResult.HttpError -> latestResult.statusCode in retryPolicy.retryOnStatusCodes
                is NetworkResult.RequestError -> retryPolicy.retryOnRequestError
                is NetworkResult.ResponseDecodingError -> false
                is NetworkResult.Success -> false
            } && attempt < retryPolicy.maxRetries

        if (!shouldRetry) {
            return latestResult
        }

        delay(retryPolicy.delayForAttempt(attempt))
        attempt++
    }
}

@PublishedApi
internal fun RetryPolicy.delayForAttempt(attempt: Int): Duration {
    val multiplier = backoffFactor.pow(attempt)
    val nextDelay = initialDelay * multiplier
    return minOf(nextDelay, maxDelay)
}
