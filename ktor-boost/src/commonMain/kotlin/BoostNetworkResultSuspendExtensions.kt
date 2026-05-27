import io.ktor.http.Headers

public suspend inline fun <T, E> NetworkResult<T, E>.onUnauthorizedSuspend(
    crossinline action: suspend (NetworkResult.HttpError<E>) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.HttpError && statusCode == 401) {
        action(this)
    }
    return this
}

public suspend inline fun <T, E> NetworkResult<T, E>.onRateLimitedSuspend(
    crossinline action: suspend (BoostError.RateLimited, NetworkResult.HttpError<E>) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.HttpError) {
        val error = networkError
        if (error is BoostError.RateLimited) {
            action(error, this)
        }
    }
    return this
}

public suspend inline fun <T, E> NetworkResult<T, E>.recoverRequestErrorSuspend(
    crossinline fallback: suspend (Throwable) -> T,
): NetworkResult<T, E> =
    when (this) {
        is NetworkResult.RequestError -> NetworkResult.Success(fallback(cause), RECOVERED_STATUS_CODE, Headers.Empty)
        else -> this
    }
