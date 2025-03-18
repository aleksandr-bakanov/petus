package bav.petus.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("cloud_pct") val cloudPercentage: Int?,
    // Temp in celsius
    @SerialName("feels_like") val feelsLike: Int?,
    // Percentage
    @SerialName("humidity") val humidity: Int?,
    // Temp in celsius
    @SerialName("max_temp") val maxTemp: Int?,
    // Temp in celsius
    @SerialName("min_temp") val minTemp: Int?,
    // Seconds since UNIX epoch
    @SerialName("sunrise") val sunrise: Long?,
    // Seconds since UNIX epoch
    @SerialName("sunset") val sunset: Long?,
    // Temp in celsius
    @SerialName("temp") val temperature: Int?,
    @SerialName("wind_degrees") val windDegrees: Int?,
    // Meters in second
    @SerialName("wind_speed") val windSpeed: Double?,
)
