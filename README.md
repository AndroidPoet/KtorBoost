<h1 align="center">KtorBoost</h1>

<p align="center">
  <img src="https://github.com/AndroidPoet/KtorBoost/assets/13647384/bc97617b-73e3-4298-a2d6-3bd62db97887" width="15%"/>
</p>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/androidpoet/KtorBoost/actions/workflows/build.yml"><img alt="Build Status" src="https://github.com/androidpoet/KtorBoost/actions/workflows/build.yml/badge.svg"/></a>
  <a href="https://androidweekly.net/issues/issue-509"><img alt="Android Weekly" src="https://androidweekly.net/issues/issue-603/badge"/></a>
  <a href="https://github.com/androidpoet"><img alt="Profile" src="https://user-images.githubusercontent.com/13647384/162662962-82e3c1eb-baf8-4e21-ad26-d4c4e3c31e44.svg"/></a>
</p>

<p align="center">
  Small Kotlin Multiplatform helpers that make Ktor client calls easier to return, inspect, and handle.
</p>

<p align="center">
  <img src="https://github.com/AndroidPoet/KtorBoost/assets/13647384/7f99beb3-10a4-4795-a8d0-d70403a2555a" style="max-width:100%;height:auto;">
</p>

## Install

[![Maven Central](https://img.shields.io/maven-central/v/io.github.androidpoet/ktor-boost.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.androidpoet%22%20AND%20a:%22ktor-boost%22)

```kotlin
sourceSets {
    val commonMain by getting {
        dependencies {
            implementation("io.github.androidpoet:ktor-boost:$version")
        }
    }
}
```

## Features

- Simple `Result<T>` wrappers for Ktor HTTP calls.
- Typed `NetworkResult<T, E>` for status codes, headers, raw error bodies, and decoded API errors.
- Bearer token helpers with automatic refresh and request replay on `401`.
- Retry and timeout helpers for transient network failures.
- KMP-safe byte downloads with progress callbacks.
- Empty response support with `Unit`.
- Async helpers returning `Deferred<Result<T>>`.
- Request builder shortcuts for bearer auth, query params, JSON bodies, and form bodies.
- Suspend-friendly `Result` helpers.

## Quick Start

Use `getResult`, `postResult`, `putResult`, `deleteResult`, `patchResult`, `headResult`, or
`optionsResult` when you want a simple Kotlin `Result<T>`.

```kotlin
val result = httpClient.getResult<List<Movie>>("trendingMovies")

result
    .onSuccess { movies ->
        // render movies
    }
    .onFailure { error ->
        // show error
    }
```

If you want non-2xx HTTP responses to become `Result.failure(...)`, configure Ktor with
`expectSuccess = true`:

```kotlin
val httpClient = HttpClient {
    expectSuccess = true
}
```

## API Choices

| Use case | API |
| --- | --- |
| Simple success/failure handling | `getResult<T>()` |
| Need status code, headers, or error body | `getNetworkResult<T, E>()` |
| Authenticated request with token refresh | `getAuthenticatedNetworkResult<T, E>()` |
| Retry transient failures | `getResultWithRetry<T>()` |
| Download bytes with progress | `downloadBytes()` |
| Empty response body, such as `204 No Content` | `deleteResult<Unit>()` |
| Need a `Deferred<Result<T>>` | `getResultAsync<T>()` |
| Add common headers, query params, or body | `bearerToken`, `queryParams`, `jsonBody`, `formBody` |
| Need suspend callbacks on `Result` | `onSuccessSuspend`, `onFailureSuspend`, `foldSuspend` |

## Typed HTTP Errors

Use `NetworkResult` when your app needs response metadata or typed API errors.

```kotlin
val result = httpClient.getNetworkResult<User, ApiError>(
    urlString = "users/me",
    decodeErrorBody = { rawBody ->
        json.decodeFromString<ApiError>(rawBody)
    },
)

when (result) {
    is NetworkResult.Success -> {
        val user = result.body
        val statusCode = result.statusCode
        val headers = result.headers
    }

    is NetworkResult.HttpError -> {
        val statusCode = result.statusCode
        val rawErrorBody = result.rawBody
        val apiError = result.errorBody
    }

    is NetworkResult.ResponseDecodingError -> {
        val cause = result.cause
    }

    is NetworkResult.RequestError -> {
        val cause = result.cause
    }
}
```

`NetworkResult` works with or without Ktor's `expectSuccess` setting.

You can also use convenience helpers:

```kotlin
val user = result.getOrNull()
val apiError = result.errorOrNull()
val statusCode = result.statusCodeOrNull()

val displayName =
    result
        .map { user -> user.name }
        .getOrNull()
```

## Auth Token Refresh

Use `BearerTokenProvider` when authenticated APIs need automatic token refresh. Your app owns
token storage; KtorBoost asks for the current token, refreshes when needed, and replays the
request after a `401`.

```kotlin
class AppTokenProvider(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
) : BearerTokenProvider {
    override suspend fun currentToken(): String? {
        return tokenStore.accessToken
    }

    override suspend fun refreshToken(): String? {
        val token = authApi.refreshAccessToken(tokenStore.refreshToken)
        tokenStore.accessToken = token
        return token
    }

    override suspend fun clearToken() {
        tokenStore.clear()
    }
}
```

Then call authenticated helpers:

```kotlin
val result = httpClient.getAuthenticatedNetworkResult<User, ApiError>(
    urlString = "users/me",
    tokenProvider = appTokenProvider,
    decodeErrorBody = { rawBody ->
        json.decodeFromString<ApiError>(rawBody)
    },
)
```

For simple `Result<T>`:

```kotlin
val result = httpClient.getAuthenticatedResult<User>(
    urlString = "users/me",
    tokenProvider = appTokenProvider,
)
```

Behavior:

- If `currentToken()` returns a token, KtorBoost sends `Authorization: Bearer <token>`.
- If `currentToken()` returns `null`, KtorBoost calls `refreshToken()` before the first request.
- If the server returns `401`, KtorBoost calls `refreshToken()` and replays the request once.
- If the replay still returns `401`, KtorBoost calls `clearToken()` and returns the error.
- KtorBoost does not cache tokens internally; your `BearerTokenProvider` remains the source of truth.

## Retry And Timeout

Use retry helpers for transient failures such as `408`, `429`, `500`, `502`, `503`, and `504`.

```kotlin
val result = httpClient.getResultWithRetry<User>(
    urlString = "users/me",
    retryPolicy = RetryPolicy(maxRetries = 3),
    timeout = 5.seconds,
)
```

For typed errors:

```kotlin
val result = httpClient.getNetworkResultWithRetry<User, ApiError>(
    urlString = "users/me",
    retryPolicy = RetryPolicy(maxRetries = 3),
    timeout = 5.seconds,
    decodeErrorBody = { rawBody ->
        json.decodeFromString<ApiError>(rawBody)
    },
)
```

## Downloads

Use `downloadBytes` for KMP-safe downloads with progress. The core API returns bytes and
metadata; apps can decide where to store the bytes on each platform.

```kotlin
val result = httpClient.downloadBytes(
    urlString = "files/report.pdf",
    onProgress = { progress ->
        val fraction = progress.fraction
        val bytesRead = progress.bytesRead
        val totalBytes = progress.totalBytes
    },
)

when (result) {
    is DownloadResult.Success -> {
        val bytes = result.content.bytes
        val contentType = result.content.contentType
        val contentLength = result.content.contentLength
    }

    is DownloadResult.HttpError -> {
        val statusCode = result.statusCode
        val rawErrorBody = result.rawBody
    }

    is DownloadResult.RequestError -> {
        val cause = result.cause
    }
}
```

`DownloadResult.Success` includes:

- `bytes`: downloaded `ByteArray`.
- `statusCode`: HTTP status code.
- `headers`: response headers.
- `contentLength`: value from `Content-Length`, when available.
- `contentType`: parsed response content type, when available.

`DownloadProgress` includes:

- `bytesRead`: bytes received so far.
- `totalBytes`: total size when the server sends `Content-Length`.
- `fraction`: progress from `0.0` to `1.0` when total size is known.

## Request Builder Shortcuts

Use small request builder helpers to keep call sites readable.

```kotlin
val result = httpClient.postResult<User>("users") {
    bearerToken(token)
    queryParams(mapOf("source" to "android"))
    jsonBody(CreateUserRequest(name = "Ranbir"))
}
```

## Empty Responses

For endpoints that return no body, request `Unit`.

```kotlin
val result = httpClient.deleteResult<Unit>("users/123")
```

## Async

Async helpers return `Deferred<Result<T>>`.

```kotlin
val deferredResult = httpClient.getResultAsync<List<Movie>>("trendingMovies")
val result = deferredResult.await()
```

## Suspend Result Helpers

KtorBoost also includes suspend-friendly `Result` helpers:

```kotlin
result
    .onSuccessSuspend { movies ->
        repository.save(movies)
    }
    .onFailureSuspend { error ->
        logger.log(error)
    }
```

```kotlin
val message =
    result.foldSuspend(
        onSuccess = { movies -> "Loaded ${movies.size} movies" },
        onFailure = { error -> error.message ?: "Something went wrong" },
    )
```

## Compatibility

Existing simple helpers are still available:

- `getResult`
- `postResult`
- `putResult`
- `deleteResult`
- `patchResult`
- `headResult`
- `optionsResult`
- `getResultAsync`
- `postResultAsync`
- `putResultAsync`
- `deleteResultAsync`
- `patchResultAsync`
- `headResultAsync`
- `optionsResultAsync`

Recommended release version: `1.1.0`.

This release adds `NetworkResult`, auth refresh helpers, retry helpers, request builder shortcuts,
downloads, and fixes coroutine behavior:

- `runCatchingSuspend` now rethrows `CancellationException`.
- Async helpers now return a real pending `Deferred<Result<T>>`.

## License

```text
Copyright 2023 AndroidPoet (Ranbir Singh)

Licensed under the Apache License, Version 2.0.
See LICENSE.txt for details.
```
