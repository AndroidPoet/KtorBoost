import io.ktor.http.Headers

public inline fun <T, E> NetworkResult<T, E>.onUnauthorized(
    action: (NetworkResult.HttpError<E>) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.HttpError && statusCode == 401) {
        action(this)
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.onRateLimited(
    action: (BoostError.RateLimited, NetworkResult.HttpError<E>) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.HttpError) {
        val error = boostError
        if (error is BoostError.RateLimited) {
            action(error, this)
        }
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.onServerError(
    action: (NetworkResult.HttpError<E>) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.HttpError && statusCode in 500..599) {
        action(this)
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.onOffline(
    action: (NetworkResult.RequestError) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.RequestError && boostError is BoostError.Offline) {
        action(this)
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.onTimeout(
    action: (NetworkResult.RequestError) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.RequestError && boostError is BoostError.Timeout) {
        action(this)
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.recover(
    fallback: () -> T,
): NetworkResult<T, E> =
    when (this) {
        is NetworkResult.Success -> this
        else -> NetworkResult.Success(fallback(), RECOVERED_STATUS_CODE, Headers.Empty)
    }

public inline fun <T, E> NetworkResult<T, E>.recoverRequestError(
    fallback: (Throwable) -> T,
): NetworkResult<T, E> =
    when (this) {
        is NetworkResult.RequestError -> NetworkResult.Success(fallback(cause), RECOVERED_STATUS_CODE, Headers.Empty)
        else -> this
    }

public inline fun <T, E> NetworkResult<T, E>.recoverHttpError(
    fallback: (NetworkResult.HttpError<E>) -> T,
): NetworkResult<T, E> =
    when (this) {
        is NetworkResult.HttpError -> NetworkResult.Success(fallback(this), RECOVERED_STATUS_CODE, Headers.Empty)
        else -> this
    }

public inline fun <T, E> NetworkResult<T, E>.recoverIf(
    predicate: (BoostError) -> Boolean,
    fallback: (BoostError) -> T,
): NetworkResult<T, E> {
    val error = boostError ?: return this
    return if (predicate(error)) {
        NetworkResult.Success(fallback(error), RECOVERED_STATUS_CODE, Headers.Empty)
    } else {
        this
    }
}

public const val RECOVERED_STATUS_CODE: Int = 0
