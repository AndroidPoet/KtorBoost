import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod

@PublishedApi
internal suspend inline fun <reified T> HttpResponse.bodyOrUnit(): T {
    return if (T::class == Unit::class) {
        Unit as T
    } else {
        body()
    }
}

/**
 * Performs an HTTP request and returns the response body as [Result].
 *
 * This is the core primitive used by the verb-specific helpers.
 */
public suspend inline fun <reified T> HttpClient.requestResult(
    method: HttpMethod,
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> =
    runSafeSuspendCatching {
        request(urlString) {
            this.method = method
            block()
        }.bodyOrUnit()
    }

/**
 * Performs an HTTP GET request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the GET request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.getResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResult(HttpMethod.Get, urlString, block)

/**
 * Performs an HTTP POST request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the POST request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.postResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResult(HttpMethod.Post, urlString, block)

/**
 * Performs an HTTP PUT request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the PUT request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.putResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResult(HttpMethod.Put, urlString, block)

/**
 * Performs an HTTP DELETE request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the DELETE request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.deleteResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResult(HttpMethod.Delete, urlString, block)

/**
 * Performs an HTTP PATCH request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the PATCH request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.patchResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResult(HttpMethod.Patch, urlString, block)

/**
 * Performs an HTTP HEAD request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the HEAD request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.headResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResult(HttpMethod.Head, urlString, block)

/**
 * Performs an HTTP OPTIONS request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the OPTIONS request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.optionsResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestResult(HttpMethod.Options, urlString, block)
