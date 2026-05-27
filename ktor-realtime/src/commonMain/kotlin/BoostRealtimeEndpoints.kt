import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json

public sealed class RealtimeEndpoint {
    public abstract val url: String
    public abstract val json: Json

    public data class WebSocket(
        override val url: String,
        override val json: Json = Json { ignoreUnknownKeys = true },
    ) : RealtimeEndpoint()

    public data class ServerSentEvents(
        override val url: String,
        override val json: Json = Json { ignoreUnknownKeys = true },
        val lastEventId: String? = null,
    ) : RealtimeEndpoint()

    public data class LaravelReverb(
        override val url: String,
        override val json: Json = Json { ignoreUnknownKeys = true },
        val appKey: String,
        val channel: String? = null,
        val auth: String? = null,
        val channelData: String? = null,
        val authEndpoint: String? = null,
    ) : RealtimeEndpoint()

    public data class SocketIo(
        override val url: String,
        override val json: Json = Json { ignoreUnknownKeys = true },
        val namespace: String = "/",
        val eventName: String? = null,
    ) : RealtimeEndpoint()

    public data class Stomp(
        override val url: String,
        override val json: Json = Json { ignoreUnknownKeys = true },
        val destination: String = "/topic/realtime",
        val login: String? = null,
        val passcode: String? = null,
        val host: String? = null,
    ) : RealtimeEndpoint()

    public data class GraphQlSubscriptions(
        override val url: String,
        override val json: Json = Json { ignoreUnknownKeys = true },
        val protocol: String = "graphql-transport-ws",
        val operationId: String = "1",
        val query: String = "subscription { ping }",
        val operationName: String? = null,
        val variablesJson: String? = null,
    ) : RealtimeEndpoint()

    public data class MqttOverWebSocket(
        override val url: String,
        override val json: Json = Json { ignoreUnknownKeys = true },
        val clientId: String,
    ) : RealtimeEndpoint()

    public data class RSocket(
        override val url: String,
        override val json: Json = Json { ignoreUnknownKeys = true },
        val route: String? = null,
    ) : RealtimeEndpoint()

    public data class LongPolling(
        override val url: String,
        override val json: Json = Json { ignoreUnknownKeys = true },
        val method: HttpMethod = HttpMethod.Get,
        val intervalMillis: Long = 2_000L,
    ) : RealtimeEndpoint()
}

public sealed class RealtimeFeatureSupport(
    public val protocol: RealtimeProtocol,
) {
    public data object Ready : RealtimeFeatureSupport(RealtimeProtocol.WebSocket)
    public data object ReadySse : RealtimeFeatureSupport(RealtimeProtocol.ServerSentEvents)
    public data class Planned(
        val target: RealtimeProtocol,
        val note: String,
    ) : RealtimeFeatureSupport(target)
}
