import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs a suspending function [block] safely, catching any exceptions that occur during its execution.
 * Returns a [Result] indicating success or failure of the function.
 *
 * @param block the suspending function to be executed safely
 * @return a [Result] indicating success ([Result.success]) or failure ([Result.failure]) of the function
 * @throws CancellationException if the coroutine is cancelled during the execution of [block]
 */
public suspend inline fun <R> runSafeSuspendCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
