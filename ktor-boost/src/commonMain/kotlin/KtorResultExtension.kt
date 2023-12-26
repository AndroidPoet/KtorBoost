import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.options
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put

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
): Result<T> = runCatching { get(urlString, block).body() }

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
): Result<T> = runCatching { post(urlString, block).body() }

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
): Result<T> = runCatching { put(urlString, block).body() }

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
): Result<T> = runCatching { delete(urlString, block).body() }

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
): Result<T> = runCatching { patch(urlString, block).body() }

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
): Result<T> = runCatching { head(urlString, block).body() }

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
): Result<T> = runCatching { options(urlString, block).body() }
