import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

public suspend inline fun <reified T, reified E> HttpClient.requestBoost(
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

public suspend inline fun <reified T, reified E> HttpClient.getBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestBoost(HttpMethod.Get, urlString, json, policy, block)

public suspend inline fun <reified T, reified E> HttpClient.postBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestBoost(HttpMethod.Post, urlString, json, policy, block)

public suspend inline fun <reified T, reified E> HttpClient.putBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestBoost(HttpMethod.Put, urlString, json, policy, block)

public suspend inline fun <reified T, reified E> HttpClient.deleteBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestBoost(HttpMethod.Delete, urlString, json, policy, block)

public suspend inline fun <reified T, reified E> HttpClient.patchBoost(
    urlString: String,
    json: Json = Json { ignoreUnknownKeys = true },
    policy: KtorBoostPolicy = KtorBoostPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<T, E> = requestBoost(HttpMethod.Patch, urlString, json, policy, block)
