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
| Empty response body, such as `204 No Content` | `deleteResult<Unit>()` |
| Need a `Deferred<Result<T>>` | `getResultAsync<T>()` |
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

This release adds `NetworkResult` and fixes coroutine behavior:

- `runCatchingSuspend` now rethrows `CancellationException`.
- Async helpers now return a real pending `Deferred<Result<T>>`.

## License

```text
Copyright 2023 AndroidPoet (Ranbir Singh)

Licensed under the Apache License, Version 2.0.
See LICENSE.txt for details.
```
