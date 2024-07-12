package bav.petus.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("cloud_pct") val cloudPercentage: Int?,
    @SerialName("feels_like") val feelsLike: Int?,
    @SerialName("humidity") val humidity: Int?,
    @SerialName("max_temp") val maxTemp: Int?,
    @SerialName("min_temp") val minTemp: Int?,
    @SerialName("sunrise") val sunrise: Long?,
    @SerialName("sunset") val sunset: Long?,
    @SerialName("temp") val temperature: Int?,
    @SerialName("wind_degrees") val windDegrees: Int?,
    @SerialName("wind_speed") val windSpeed: Double?,
)
