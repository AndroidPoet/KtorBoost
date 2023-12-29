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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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
): Deferred<Result<T>> =
    coroutineScope {
        async { runSafeSuspendCatching { get(urlString, block).body() } }
    }

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
): Deferred<Result<T>> =
    coroutineScope {
        async { runSafeSuspendCatching { post(urlString, block).body() } }
    }

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
): Deferred<Result<T>> =
    coroutineScope {
        async { runSafeSuspendCatching { put(urlString, block).body() } }
    }

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
): Deferred<Result<T>> =
    coroutineScope {
        async { runSafeSuspendCatching { delete(urlString, block).body() } }
    }

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
): Deferred<Result<T>> =
    coroutineScope {
        async { runSafeSuspendCatching { patch(urlString, block).body() } }
    }

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
): Deferred<Result<T>> =
    coroutineScope {
        async { runSafeSuspendCatching { head(urlString, block).body() } }
    }

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
): Deferred<Result<T>> =
    coroutineScope {
        async { runSafeSuspendCatching { options(urlString, block).body() } }
    }
