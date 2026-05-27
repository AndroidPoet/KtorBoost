public sealed class RealtimeResult<out T> {
    public data class Success<T>(val value: T) : RealtimeResult<T>()

    public data class Unsupported(
        val protocol: RealtimeProtocol,
        val message: String,
    ) : RealtimeResult<Nothing>()

    public data class Failure(
        val protocol: RealtimeProtocol,
        val cause: Throwable,
    ) : RealtimeResult<Nothing>()
}

@PublishedApi
internal inline fun <T> realtimeSuccess(value: T): RealtimeResult<T> = RealtimeResult.Success(value)

@PublishedApi
internal inline fun realtimeUnsupported(
    protocol: RealtimeProtocol,
    message: String,
): RealtimeResult<Nothing> = RealtimeResult.Unsupported(protocol, message)

@PublishedApi
internal inline fun realtimeFailure(
    protocol: RealtimeProtocol,
    cause: Throwable,
): RealtimeResult<Nothing> = RealtimeResult.Failure(protocol, cause)
