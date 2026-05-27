public class RealtimeProtocolNotYetSupportedException(
    protocol: RealtimeProtocol,
    message: String,
) : UnsupportedOperationException("`$protocol` is not supported yet. $message")
