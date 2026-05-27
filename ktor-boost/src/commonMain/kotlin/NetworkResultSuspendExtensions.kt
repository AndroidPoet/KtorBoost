public suspend inline fun <T, E> NetworkResult<T, E>.onSuccessSuspend(
    crossinline action: suspend (NetworkResult.Success<T>) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.Success) {
        action(this)
    }
    return this
}

public suspend inline fun <T, E> NetworkResult<T, E>.onHttpErrorSuspend(
    crossinline action: suspend (NetworkResult.HttpError<E>) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.HttpError) {
        action(this)
    }
    return this
}

public suspend inline fun <T, E> NetworkResult<T, E>.onResponseDecodingErrorSuspend(
    crossinline action: suspend (NetworkResult.ResponseDecodingError) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.ResponseDecodingError) {
        action(this)
    }
    return this
}

public suspend inline fun <T, E> NetworkResult<T, E>.onRequestErrorSuspend(
    crossinline action: suspend (NetworkResult.RequestError) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.RequestError) {
        action(this)
    }
    return this
}

public suspend inline fun <T, E> NetworkResult<T, E>.onFailureSuspend(
    crossinline action: suspend (NetworkResult<T, E>) -> Unit,
): NetworkResult<T, E> {
    if (this !is NetworkResult.Success) {
        action(this)
    }
    return this
}

public suspend inline fun <T, E> NetworkResult<T, E>.onErrorSuspend(
    crossinline action: suspend (NetworkResult<T, E>) -> Unit,
): NetworkResult<T, E> = onFailureSuspend(action)
