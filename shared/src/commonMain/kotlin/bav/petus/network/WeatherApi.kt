package bav.petus.network

import bav.petus.entity.WeatherDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class WeatherApi {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    suspend fun getWeather(latitude: Double, longitude: Double): Result<WeatherDto> {
        val response = httpClient.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.api-ninjas.com"
                path("v1/weather")
                parameters.append("lat", latitude.toString())
                parameters.append("lon", longitude.toString())
            }
            header("X-Api-Key", "YOUR-API-KEY-HERE")
        }

        return when (response.status) {
            HttpStatusCode.OK -> Result.success(response.body())
            else -> Result.failure(Throwable("${response.status.value} ${response.status.description}"))
        }
    }
}