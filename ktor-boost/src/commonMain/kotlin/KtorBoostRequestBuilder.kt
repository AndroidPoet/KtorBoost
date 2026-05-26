import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType

public fun HttpRequestBuilder.bearerToken(token: String) {
    bearerAuth(token)
}

public fun HttpRequestBuilder.jsonBody(body: Any) {
    contentType(ContentType.Application.Json)
    setBody(body)
}

public fun HttpRequestBuilder.formBody(parameters: Parameters) {
    setBody(FormDataContent(parameters))
}

public fun HttpRequestBuilder.queryParams(parameters: Map<String, Any?>) {
    parameters.forEach { (key, value) ->
        if (value != null) {
            parameter(key, value)
        }
    }
}
