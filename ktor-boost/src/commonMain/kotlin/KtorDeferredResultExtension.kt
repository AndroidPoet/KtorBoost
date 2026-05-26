import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext

/**
 * Executes an asynchronous HTTP GET request using the provided URL.
 *
 * @param urlString The URL for the GET request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return A Deferred<Result<T>> representing the asynchronous operation.
 */
public suspend inline fun <reified T> HttpClient.getResultAsync(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Deferred<Result<T>> = CoroutineScope(currentCoroutineContext()).async { getResult(urlString, block) }

/**
 * Executes an asynchronous HTTP POST request using the provided URL.
 *
 * @param urlString The URL for the POST request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return A Deferred<Result<T>> representing the asynchronous operation.
 */
public suspend inline fun <reified T> HttpClient.postResultAsync(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Deferred<Result<T>> = CoroutineScope(currentCoroutineContext()).async { postResult(urlString, block) }

/**
 * Executes an asynchronous HTTP PUT request using the provided URL.
 *
 * @param urlString The URL for the PUT request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return A Deferred<Result<T>> representing the asynchronous operation.
 */
public suspend inline fun <reified T> HttpClient.putResultAsync(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Deferred<Result<T>> = CoroutineScope(currentCoroutineContext()).async { putResult(urlString, block) }

/**
 * Executes an asynchronous HTTP DELETE request using the provided URL.
 *
 * @param urlString The URL for the DELETE request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return A Deferred<Result<T>> representing the asynchronous operation.
 */
public suspend inline fun <reified T> HttpClient.deleteResultAsync(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Deferred<Result<T>> = CoroutineScope(currentCoroutineContext()).async { deleteResult(urlString, block) }

/**
 * Executes an asynchronous HTTP PATCH request using the provided URL.
 *
 * @param urlString The URL for the PATCH request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return A Deferred<Result<T>> representing the asynchronous operation.
 */
public suspend inline fun <reified T> HttpClient.patchResultAsync(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Deferred<Result<T>> = CoroutineScope(currentCoroutineContext()).async { patchResult(urlString, block) }

/**
 * Executes an asynchronous HTTP HEAD request using the provided URL.
 *
 * @param urlString The URL for the HEAD request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return A Deferred<Result<T>> representing the asynchronous operation.
 */
public suspend inline fun <reified T> HttpClient.headResultAsync(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Deferred<Result<T>> = CoroutineScope(currentCoroutineContext()).async { headResult(urlString, block) }

/**
 * Executes an asynchronous HTTP OPTIONS request using the provided URL.
 *
 * @param urlString The URL for the OPTIONS request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return A Deferred<Result<T>> representing the asynchronous operation.
 */
public suspend inline fun <reified T> HttpClient.optionsResultAsync(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Deferred<Result<T>> = CoroutineScope(currentCoroutineContext()).async { optionsResult(urlString, block) }
