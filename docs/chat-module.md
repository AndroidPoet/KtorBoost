# KtorBoost Chat Module

`ktor-realtime` is the optional realtime module for typed WebSocket chat and event streams.
The main `ktor-boost` artifact includes HTTP helpers, Boost errors, and recovery operators.

## Install

```kotlin
implementation("io.github.androidpoet:ktor-realtime:$version")
```

## Typed chat stream

Your app owns the event and command models. KtorBoost only handles the WebSocket loop and JSON
send/receive ergonomics.

```kotlin
httpClient.realtimeChat<ChatEvent, ChatCommand>(
    urlString = "wss://example.com/chat",
    onMessage = { event ->
        render(event)
    },
) {
    sendJson(ChatCommand.Join(roomId = "general"))
}
```

## Unified protocol endpoint API

Use `RealtimeEndpoint` with `realtime` for protocol-neutral usage:

```kotlin
val endpoint =
    RealtimeEndpoint.WebSocket(
        url = "wss://example.com/realtime",
    )

httpClient.realtime<ChatEvent, ChatCommand>(
    endpoint = endpoint,
    onEvent = { event -> render(event) },
) {
    sendJson(ChatCommand.Join(roomId = "general"))
}
```

Currently implemented:

- `WebSocket`
- `ServerSentEvents`

Protocol adapters already modeled (planned next):

- `LaravelReverb`
- `SocketIo`
- `Stomp`
- `GraphQlSubscriptions`
- `MqttOverWebSocket`
- `RSocket`
- `LongPolling`

Each protocol has a separate handler API:

- `realtimeReverb(...)`
- `realtimeSocketIo(...)`
- `realtimeStomp(...)`
- `realtimeGraphQlSubscriptions(...)`
- `realtimeMqttOverWebSocket(...)`
- `realtimeRSocket(...)`
- `realtimeLongPolling(...)`

## Lower-level realtime stream

Use `realtime` when the stream is not strictly chat:

```kotlin
httpClient.realtime<PresenceEvent, PresenceCommand>(
    urlString = "wss://example.com/presence",
    onEvent = { event ->
        updatePresence(event)
    },
) {
    sendJson(PresenceCommand.Subscribe(teamId = "android"))
}
```
