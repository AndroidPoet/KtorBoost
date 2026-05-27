public data class KtorBoostPolicy(
    val successStatusCodes: IntRange = 200..299,
)

public typealias NetworkPolicy = KtorBoostPolicy
