import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HttpClientExtensionsTest {
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
            respondOk(responseContent)
        }

    private val httpClient = HttpClient(mockEngine)

    @Test
    fun `test getResult extension function`() {
        runBlocking {
            val result = httpClient.getResult<String>("sample_get_url")

            println(result.toString())

            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("get success", responseBody)
        }
    }

    @Test
    fun `test postResult extension function`() {
        runBlocking {
            val result = httpClient.postResult<String>("sample_post_url")
            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("post success", responseBody)
        }
    }

    @Test
    fun `test putResult extension function`() {
        runBlocking {
            val result = httpClient.putResult<String>("sample_put_url")

            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("put success", responseBody)
        }
    }

    @Test
    fun `test deleteResult extension function`() {
        runBlocking {
            val result = httpClient.deleteResult<String>("sample_delete_url")

            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("delete success", responseBody)
        }
    }

    @Test
    fun `test patchResult extension function`() {
        runBlocking {
            val result = httpClient.patchResult<String>("sample_patch_url")

            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("patch success", responseBody)
        }
    }

    @Test
    fun `test headResult extension function`() {
        runBlocking {
            val result = httpClient.headResult<String>("sample_head_url")

            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("head success", responseBody)
        }
    }

    @Test
    fun `test optionsResult extension function`() {
        runBlocking {
            val result = httpClient.optionsResult<String>("sample_options_url")

            assertTrue(result.isSuccess)
            val responseBody = result.getOrNull()
            assertNotNull(responseBody)
            assertEquals("options success", responseBody)
        }
    }
}
