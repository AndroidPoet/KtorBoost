import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class KtorBoostRequestBuilderTest {
    @Test
    fun test_requestBuilderShortcuts_addHeadersQueryParamsAndJsonContentType() {
        val httpClient =
            HttpClient(
                MockEngine { request ->
                    assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
                    assertEquals("android", request.url.parameters["platform"])
                    assertEquals("application/json", request.body.contentType?.withoutParameters().toString())

                    respond(
                        content = "success",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                    )
                },
            )

        val result =
            runBlocking {
                httpClient.postResult<String>("sample_post_url") {
                    bearerToken("token")
                    queryParams(mapOf("platform" to "android", "ignored" to null))
                    jsonBody("""{"name":"KtorBoost"}""")
                }
            }

        assertEquals(Result.success("success"), result)
    }
}
