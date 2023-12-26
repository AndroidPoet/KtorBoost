import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// https://github.com/skydoves/retrofit-adapters/blob/main/retrofit-adapters-result/src/main/kotlin/com/skydoves/retrofit/adapters/result/ResultExtensions.kt

/**
 * Executes a suspend function [block] within a try-catch and returns the result as a [Result].
 *
 * @param block A suspend function that returns a result.
 * @return Result<R> representing the success or failure of the provided suspend function.
 */
public suspend inline fun <R> runCatchingSuspend(crossinline block: suspend () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Executes a suspend function [block] on the receiver [T] within a try-catch and returns the result as a [Result].
 *
 * @param block A suspend function that returns a result, acting on the receiver [T].
 * @return Result<R> representing the success or failure of the provided suspend function.
 */
public suspend inline fun <T, R> T.runCatchingSuspend(crossinline block: suspend T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Executes a suspend function [action] if the current result is a success.
 *
 * @param action A suspend function to be executed if the result is a success.
 * @return The current Result<T> instance.
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun <T> Result<T>.onSuccessSuspend(crossinline action: suspend (value: T) -> Unit): Result<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (isSuccess) action(getOrThrow())
    return this
}

/**
 * Executes a suspend function [action] if the current result is a failure.
 *
 * @param action A suspend function to be executed if the result is a failure.
 * @return The current Result<T> instance.
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun <T> Result<T>.onFailureSuspend(crossinline action: suspend (exception: Throwable) -> Unit): Result<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    exceptionOrNull()?.let { action(it) }
    return this
}

/**
 * Performs a fold operation on the Result, executing [onSuccess] if the Result is a success,
 * or [onFailure] if the Result is a failure.
 *
 * @param onSuccess A suspend function to be executed on success.
 * @param onFailure A suspend function to be executed on failure.
 * @return Result of applying the corresponding function based on the current Result state.
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun <R, T> Result<T>.foldSuspend(
    crossinline onSuccess: suspend (value: T) -> R,
    crossinline onFailure: suspend (exception: Throwable) -> R,
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    return when (val exception = exceptionOrNull()) {
        null -> onSuccess(getOrThrow())
        else -> onFailure(exception)
    }
}

/**
 * Performs a mapping operation on the Result, transforming the value if the Result is a success.
 *
 * @param transform A suspend function to transform the value on success.
 * @return Result<R> representing the transformed value or failure based on the current Result state.
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun <R, T> Result<T>.mapSuspend(crossinline transform: suspend (value: T) -> R): Result<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when {
        isSuccess -> Result.success(transform(getOrThrow()))
        else -> Result.failure(exceptionOrNull()!!)
    }
}

/**
 * Recovers from a failure by executing [transform] to create a new value if the Result is a failure.
 *
 * @param transform A suspend function to transform the exception on failure.
 * @return Result<R> representing the recovered value or original success based on the current Result state.
 */
@OptIn(ExperimentalContracts::class)
public suspend inline fun <R, T : R> Result<T>.recoverSuspend(crossinline transform: suspend (exception: Throwable) -> R): Result<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (val exception = exceptionOrNull()) {
        null -> this
        else -> Result.success(transform(exception))
    }
}
