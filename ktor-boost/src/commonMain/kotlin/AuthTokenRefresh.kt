import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import kotlin.coroutines.cancellation.CancellationException

public interface BearerTokenProvider {
    public suspend fun currentToken(): String?

    public suspend fun refreshToken(): String?

    public suspend fun clearToken() {
    }
}

public data class AuthRefreshPolicy(
    val refreshOnStatusCodes: Set<Int> = setOf(401),
    val maxRefreshAttempts: Int = 1,
)

public class AuthenticationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

public suspend inline fun <reified T> HttpClient.requestAuthenticatedResult(
    method: HttpMethod,
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> {
    return when (
        val networkResult =
            requestAuthenticatedNetworkResult<T, Nothing>(
                method = method,
                urlString = urlString,
                tokenProvider = tokenProvider,
                authRefreshPolicy = authRefreshPolicy,
                block = block,
            )
    ) {
        is NetworkResult.Success -> Result.success(networkResult.body)
        is NetworkResult.HttpError ->
            Result.failure(
                AuthenticationException(
                    "Authenticated request failed with HTTP ${networkResult.statusCode}.",
                    networkResult.decodeFailure,
                ),
            )
        is NetworkResult.ResponseDecodingError -> Result.failure(networkResult.cause)
        is NetworkResult.RequestError -> Result.failure(networkResult.cause)
    }
}

public suspend inline fun <reified T, E> HttpClient.requestAuthenticatedNetworkResult(
    method: HttpMethod,
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> {
    require(authRefreshPolicy.maxRefreshAttempts >= 0) {
        "maxRefreshAttempts must be 0 or greater."
    }

    try {
        var token = tokenProvider.currentToken() ?: tokenProvider.refreshToken()
        var refreshAttempts = 0

        while (true) {
            val result =
                requestNetworkResult<T, E>(
                    method = method,
                    urlString = urlString,
                    block = {
                        block()
                        token?.let(::bearerToken)
                    },
                    decodeErrorBody = decodeErrorBody,
                )

            val shouldRefresh =
                result is NetworkResult.HttpError &&
                    result.statusCode in authRefreshPolicy.refreshOnStatusCodes &&
                    refreshAttempts < authRefreshPolicy.maxRefreshAttempts

            if (!shouldRefresh) {
                if (result is NetworkResult.HttpError && result.statusCode in authRefreshPolicy.refreshOnStatusCodes) {
                    tokenProvider.clearToken()
                }
                return result
            }

            refreshAttempts++
            token = tokenProvider.refreshToken()

            if (token == null) {
                tokenProvider.clearToken()
                return result
            }
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (cause: Throwable) {
        return NetworkResult.RequestError(cause)
    }
}

public suspend inline fun <reified T> HttpClient.getAuthenticatedResult(
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestAuthenticatedResult(HttpMethod.Get, urlString, tokenProvider, authRefreshPolicy, block)

public suspend inline fun <reified T> HttpClient.postAuthenticatedResult(
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestAuthenticatedResult(HttpMethod.Post, urlString, tokenProvider, authRefreshPolicy, block)

public suspend inline fun <reified T> HttpClient.putAuthenticatedResult(
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestAuthenticatedResult(HttpMethod.Put, urlString, tokenProvider, authRefreshPolicy, block)

public suspend inline fun <reified T> HttpClient.deleteAuthenticatedResult(
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = requestAuthenticatedResult(HttpMethod.Delete, urlString, tokenProvider, authRefreshPolicy, block)

public suspend inline fun <reified T, E> HttpClient.getAuthenticatedNetworkResult(
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> =
    requestAuthenticatedNetworkResult(HttpMethod.Get, urlString, tokenProvider, authRefreshPolicy, block, decodeErrorBody)

public suspend inline fun <reified T, E> HttpClient.postAuthenticatedNetworkResult(
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> =
    requestAuthenticatedNetworkResult(HttpMethod.Post, urlString, tokenProvider, authRefreshPolicy, block, decodeErrorBody)

public suspend inline fun <reified T, E> HttpClient.putAuthenticatedNetworkResult(
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> =
    requestAuthenticatedNetworkResult(HttpMethod.Put, urlString, tokenProvider, authRefreshPolicy, block, decodeErrorBody)

public suspend inline fun <reified T, E> HttpClient.deleteAuthenticatedNetworkResult(
    urlString: String,
    tokenProvider: BearerTokenProvider,
    authRefreshPolicy: AuthRefreshPolicy = AuthRefreshPolicy(),
    noinline block: HttpRequestBuilder.() -> Unit = {},
    noinline decodeErrorBody: suspend (String) -> E? = { null },
): NetworkResult<T, E> =
    requestAuthenticatedNetworkResult(HttpMethod.Delete, urlString, tokenProvider, authRefreshPolicy, block, decodeErrorBody)
