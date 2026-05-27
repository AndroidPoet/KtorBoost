import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

public suspend inline fun <reified T, reified E> HttpClient.requestResult(
    method: HttpMethod,
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> {
    val result =
        requestNetworkResult<T, E>(
            method = method,
            urlString = urlString,
            block = block,
            decodeErrorBody = { raw -> json.decodeFromString<E>(raw) },
            successStatusCodes = policy.successStatusCodes,
        )

    return result
}

public suspend inline fun <reified T, reified E> HttpClient.getResult(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestResult(HttpMethod.Get, urlString, json, policy, block)

public suspend inline fun <reified T, reified E> HttpClient.postResult(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestResult(HttpMethod.Post, urlString, json, policy, block)

public suspend inline fun <reified T, reified E> HttpClient.putResult(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestResult(HttpMethod.Put, urlString, json, policy, block)

public suspend inline fun <reified T, reified E> HttpClient.deleteResult(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestResult(HttpMethod.Delete, urlString, json, policy, block)

public suspend inline fun <reified T, reified E> HttpClient.patchResult(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestResult(HttpMethod.Patch, urlString, json, policy, block)

@Deprecated(
    message = "Use requestResult for neutral API naming.",
    replaceWith = ReplaceWith("requestResult(method, urlString, json, policy, block)"),
)
public suspend inline fun <reified T, reified E> HttpClient.requestBoost(
    method: HttpMethod,
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestResult(method, urlString, json, policy, block)

@Deprecated(
    message = "Use getResult for neutral API naming.",
    replaceWith = ReplaceWith("getResult(urlString, json, policy, block)"),
)
public suspend inline fun <reified T, reified E> HttpClient.getBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = getResult(urlString, json, policy, block)

@Deprecated(
    message = "Use postResult for neutral API naming.",
    replaceWith = ReplaceWith("postResult(urlString, json, policy, block)"),
)
public suspend inline fun <reified T, reified E> HttpClient.postBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = postResult(urlString, json, policy, block)

@Deprecated(
    message = "Use putResult for neutral API naming.",
    replaceWith = ReplaceWith("putResult(urlString, json, policy, block)"),
)
public suspend inline fun <reified T, reified E> HttpClient.putBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = putResult(urlString, json, policy, block)

@Deprecated(
    message = "Use deleteResult for neutral API naming.",
    replaceWith = ReplaceWith("deleteResult(urlString, json, policy, block)"),
)
public suspend inline fun <reified T, reified E> HttpClient.deleteBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = deleteResult(urlString, json, policy, block)

@Deprecated(
    message = "Use patchResult for neutral API naming.",
    replaceWith = ReplaceWith("patchResult(urlString, json, policy, block)"),
)
public suspend inline fun <reified T, reified E> HttpClient.patchBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = patchResult(urlString, json, policy, block)
