import io.ktor.http.Headers

public val <T, E> NetworkResult<T, E>.isSuccess: Boolean
    get() = this is NetworkResult.Success

public val <T, E> NetworkResult<T, E>.isFailure: Boolean
    get() = this !is NetworkResult.Success

public val <T, E> NetworkResult<T, E>.isHttpError: Boolean
    get() = this is NetworkResult.HttpError

public val <T, E> NetworkResult<T, E>.isResponseDecodingError: Boolean
    get() = this is NetworkResult.ResponseDecodingError

public val <T, E> NetworkResult<T, E>.isRequestError: Boolean
    get() = this is NetworkResult.RequestError

public fun <T, E> NetworkResult<T, E>.getOrNull(): T? {
    return when (this) {
        is NetworkResult.Success -> body
        else -> null
    }
}

public fun <T, E> NetworkResult<T, E>.errorOrNull(): E? {
    return when (this) {
        is NetworkResult.HttpError -> errorBody
        else -> null
    }
}

public fun <T, E> NetworkResult<T, E>.causeOrNull(): Throwable? {
    return when (this) {
        is NetworkResult.HttpError -> decodeFailure
        is NetworkResult.ResponseDecodingError -> cause
        is NetworkResult.RequestError -> cause
        is NetworkResult.Success -> null
    }
}

public fun <T, E> NetworkResult<T, E>.statusCodeOrNull(): Int? {
    return when (this) {
        is NetworkResult.Success -> statusCode
        is NetworkResult.HttpError -> statusCode
        is NetworkResult.ResponseDecodingError -> statusCode
        is NetworkResult.RequestError -> null
    }
}

public fun <T, E> NetworkResult<T, E>.headersOrNull(): Headers? {
    return when (this) {
        is NetworkResult.Success -> headers
        is NetworkResult.HttpError -> headers
        is NetworkResult.ResponseDecodingError -> headers
        is NetworkResult.RequestError -> null
    }
}

public inline fun <T, E, R> NetworkResult<T, E>.map(transform: (T) -> R): NetworkResult<R, E> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(body), statusCode, headers)
        is NetworkResult.HttpError -> this
        is NetworkResult.ResponseDecodingError -> this
        is NetworkResult.RequestError -> this
    }
}

public inline fun <T, E, R> NetworkResult<T, E>.mapError(transform: (E) -> R): NetworkResult<T, R> {
    return when (this) {
        is NetworkResult.Success -> this
        is NetworkResult.HttpError ->
            NetworkResult.HttpError(
                statusCode = statusCode,
                rawBody = rawBody,
                errorBody = errorBody?.let(transform),
                headers = headers,
                decodeFailure = decodeFailure,
            )
        is NetworkResult.ResponseDecodingError -> this
        is NetworkResult.RequestError -> this
    }
}

public inline fun <T, E, R> NetworkResult<T, E>.fold(
    onSuccess: (NetworkResult.Success<T>) -> R,
    onHttpError: (NetworkResult.HttpError<E>) -> R,
    onResponseDecodingError: (NetworkResult.ResponseDecodingError) -> R,
    onRequestError: (NetworkResult.RequestError) -> R,
): R {
    return when (this) {
        is NetworkResult.Success -> onSuccess(this)
        is NetworkResult.HttpError -> onHttpError(this)
        is NetworkResult.ResponseDecodingError -> onResponseDecodingError(this)
        is NetworkResult.RequestError -> onRequestError(this)
    }
}

public inline fun <T, E> NetworkResult<T, E>.onSuccess(action: (NetworkResult.Success<T>) -> Unit): NetworkResult<T, E> {
    if (this is NetworkResult.Success) {
        action(this)
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.onHttpError(action: (NetworkResult.HttpError<E>) -> Unit): NetworkResult<T, E> {
    if (this is NetworkResult.HttpError) {
        action(this)
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.onResponseDecodingError(
    action: (NetworkResult.ResponseDecodingError) -> Unit,
): NetworkResult<T, E> {
    if (this is NetworkResult.ResponseDecodingError) {
        action(this)
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.onRequestError(action: (NetworkResult.RequestError) -> Unit): NetworkResult<T, E> {
    if (this is NetworkResult.RequestError) {
        action(this)
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.onFailure(action: (NetworkResult<T, E>) -> Unit): NetworkResult<T, E> {
    if (this !is NetworkResult.Success) {
        action(this)
    }
    return this
}

public inline fun <T, E> NetworkResult<T, E>.onError(action: (NetworkResult<T, E>) -> Unit): NetworkResult<T, E> =
    onFailure(action)
