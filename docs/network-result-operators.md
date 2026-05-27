# NetworkResult Operators (Ktor)

This page documents the Ktor-focused `NetworkResult` operator APIs in KtorBoost.

## Why this exists

`NetworkResult<T, E>` gives typed success/error handling for Ktor calls, including:

- success body
- HTTP status code and headers
- raw error body
- decoded API error body
- transport/decoding failures

## Result model

- `NetworkResult.Success<T>`
- `NetworkResult.HttpError<E>`
- `NetworkResult.ResponseDecodingError`
- `NetworkResult.RequestError`

## Request entry points

- `requestNetworkResult`
- `getNetworkResult`
- `postNetworkResult`
- `putNetworkResult`
- `deleteNetworkResult`
- `patchNetworkResult`
- `headNetworkResult`
- `optionsNetworkResult`

## Core operators

- `isSuccess`
- `isFailure`
- `isHttpError`
- `isResponseDecodingError`
- `isRequestError`
- `getOrNull()`
- `errorOrNull()`
- `causeOrNull()`
- `statusCodeOrNull()`
- `headersOrNull()`
- `map { ... }`
- `mapError { ... }`
- `fold(...)`

## Side-effect operators

- `onSuccess { ... }`
- `onHttpError { ... }`
- `onResponseDecodingError { ... }`
- `onRequestError { ... }`
- `onFailure { ... }`
- `onError { ... }` (alias of `onFailure`, added for migration-friendly naming)

## Suspend side-effect operators

- `onSuccessSuspend { ... }`
- `onHttpErrorSuspend { ... }`
- `onResponseDecodingErrorSuspend { ... }`
- `onRequestErrorSuspend { ... }`
- `onFailureSuspend { ... }`
- `onErrorSuspend { ... }` (alias of `onFailureSuspend`)

## Example

```kotlin
val result = httpClient.getNetworkResult<User, ApiError>(
    urlString = "users/me",
    decodeErrorBody = { raw -> json.decodeFromString<ApiError>(raw) },
)

result
    .onSuccess { success ->
        println("User: ${success.body}")
    }
    .onHttpError { httpError ->
        println("HTTP ${httpError.statusCode}: ${httpError.errorBody}")
    }
    .onResponseDecodingError { decoding ->
        println("Decode failure: ${decoding.cause}")
    }
    .onRequestError { request ->
        println("Request failure: ${request.cause}")
    }
```

## Compatibility notes

- Existing APIs are preserved; new operators are additive.
- `onError` and `onErrorSuspend` are aliases to reduce migration friction for users coming from similar result libraries.
- `CancellationException` is rethrown and never swallowed.
