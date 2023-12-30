import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HttpClientDeferredExtensionsTest {
    private val mockEngine =
        MockEngine { request ->
            val responseContent =
                when (request.method) {
                    HttpMethod.Get -> "get success"
                    HttpMethod.Post -> "post success"
                    HttpMethod.Put -> "put success"
                    HttpMethod.Delete -> "delete success"
                    HttpMethod.Patch -> "patch success"
                    HttpMethod.Head -> "head success"
                    HttpMethod.Options -> "options success"
                    else -> "" // Handle other methods if needed
                }
            respondError(HttpStatusCode.Conflict, content = responseContent)
        }

    private val httpClient = HttpClient(mockEngine)

    @Test
    fun `test getDeferredResult extension function`() {
        runBlocking {
            val deferredResult = httpClient.getResultAsync<String>("sample_get_url")
            val result = deferredResult.await()
            assertTrue(result.isSuccess)

            var onSuccessCalled = true
            result.onSuccess {
                onSuccessCalled = false
            }
            assertEquals(expected = true, actual = onSuccessCalled)
            result.onFailure {
                // assertNotNull(it)
            }

//            val responseBody = result.getOrThrow()
//            assertNotNull(responseBody)
            // assertEquals("get success", responseBody)
        }
    }

    @Test
    fun `test postDeferredResult extension function`() {
        runBlocking {
            val deferredResult = httpClient.postResultAsync<String>("sample_post_url")
            val result = deferredResult.await()
            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("post success", responseBody)
        }
    }

    @Test
    fun `test putDeferredResult extension function`() {
        runBlocking {
            val deferredResult = httpClient.putResultAsync<String>("sample_put_url")
            val result = deferredResult.await()
            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("put success", responseBody)
        }
    }

    @Test
    fun `test deleteDeferredResult extension function`() {
        runBlocking {
            val deferredResult = httpClient.deleteResultAsync<String>("sample_delete_url")
            val result = deferredResult.await()
            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("delete success", responseBody)
        }
    }

    @Test
    fun `test patchDeferredResult extension function`() {
        runBlocking {
            val deferredResult = httpClient.patchResultAsync<String>("sample_patch_url")
            val result = deferredResult.await()
            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("patch success", responseBody)
        }
    }

    @Test
    fun `test headDeferredResult extension function`() {
        runBlocking {
            val deferredResult = httpClient.headResultAsync<String>("sample_head_url")
            val result = deferredResult.await()
            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("head success", responseBody)
        }
    }

    @Test
    fun `test optionsDeferredResult extension function`() {
        runBlocking {
            val deferredResult = httpClient.optionsResultAsync<String>("sample_options_url")
            val result = deferredResult.await()
            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("options success", responseBody)
        }
    }
}
