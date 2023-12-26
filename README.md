<h1 align="center">KtorBoost</h1></br>



<p align="center">
	<img src="https://github.com/AndroidPoet/KtorBoost/assets/13647384/bc97617b-73e3-4298-a2d6-3bd62db97887" width="15%"/>

</p> <br>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/androidpoet/KtorBoost/actions/workflows/build.yml"><img alt="Build Status" 
  src="https://github.com/androidpoet/KtorBoost/actions/workflows/build.yml/badge.svg"/></a>
 <a href="https://github.com/androidpoet"><img alt="Profile" src="https://user-images.githubusercontent.com/13647384/162662962-82e3c1eb-baf8-4e21-ad26-d4c4e3c31e44.svg"/></a>
</p><br>




<p align="center">
🚀 Simplifying Ktor for Easier Development.
</p><br>

Ktor Boost streamlines HTTP requests in Ktor by offering functions that neatly package results in the [Kotlin's Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/) class. It makes
handling successes and errors clearer, simplifying error control in Ktor apps


<p align="center">
<img src="https://github.com/AndroidPoet/KtorBoost/assets/13647384/7f99beb3-10a4-4795-a8d0-d70403a2555a"
"/>
</p>

## Download

[![Maven Central](https://img.shields.io/maven-central/v/io.github.androidpoet/ktor-boost.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.androidpoet%22%20AND%20a:%22ktor-boost%22)

### Gradle

Add the dependency below to your **module**'s `build.gradle` file:

```gradle
sourceSets {
    val commonMain by getting {
        dependencies {
            implementation("io.github.androidpoet:ktor-boost:$version")
    
        }
    }
}
```

## Usage

## Before

Initially, when fetching data from APIs, the code directly gets the response's content, leading to
repetitive use of the 'body()' method:

```kotlin
// api service claas function 
suspend fun getUserNames(): List<String> {
    return httpClient.get("trendingMovies").body()
}



```

Handling errors involves enclosing each API call within separate try-catch blocks, causing code
duplication:

```kotlin
// viewModel

viewModelScope.launch {
    try {
        val data = apiService.getUserNames()
        // handle data
    } catch (e: Throwable) {
        // handle error 
    }
}


```

## After

With improvements, the API calls are now focused solely on returning values:

```kotlin
// api service claas function 

suspend fun getUserNamesResult(): Result<List<String>> {
    return httpClient.getResult<List<String>>("trendingMovies")
}

//or

suspend fun getUserNamesResult() = httpClient.getResult<List<String>>("trendingMovies")

```

This refined approach in error and response handling simplifies the code:

```kotlin
// viewModel

viewModelScope.launch {
    val result = apiService.getUserNames()
    result.onSuccess { data ->
        // handle data
    }.onFailure { error ->
        // handle error 
    }
}

```

## Different variations and use cases

The solution offers multiple variations in response handling, enabling adaptability based on team
preferences and project needs:

If you prefer executing the response function as a suspend function, useful for nested suspend
function usage on success or error:

```kotlin
viewModelScope.launch {
    result.onSuccessSuspend { data ->
        // handle data
    }.onFailureSuspend { error ->
        // handle error
    }
}

```

### For cases solely interested in success response, using getOrNull() function and handling errors manually:

```kotlin
viewModelScope.launch {
    if (result.isSuccess) {
        val data = result.getOrNull()
        // handle data
    } else {
        // handle error
    }
}

```

### Folding the response, a concise approach for clean and readable code:

```kotlin
viewModelScope.launch {
    result.fold(onSuccess = { data ->
        // handle data
    }, onFailure = { error ->
        // handle error
    })
}

```

### Fold also supports suspend version:

```kotlin
viewModelScope.launch {
    result.foldSuspend(
        onSuccess = { data ->
            // handle data
        }, onFailure = { error ->
            // handle error
        })

}

```

These diverse approaches in response handling empower you to select the most suitable method,
whether for executing suspend functions,
specifically handling success responses, or streamlining code for clearer readability.

## Find this repository useful? :heart:

Support it by joining __[stargazers](https://github.com/androidpoet/KtorBoost/stargazers)__ for this
repository. :star: <br>
Also, __[follow me](https://github.com/androidpoet)__ on GitHub for my next creations! 🤩


# License
```xml
Copyright 2023 AndroidPoet (Ranbir Singh)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
