package bav.petus.useCase

import bav.petus.cache.WeatherRecord
import bav.petus.model.PetType
import bav.petus.model.ThreePointRange
import bav.petus.model.WeatherAttitude

class WeatherAttitudeUseCase {

    fun convertWeatherRecordToAttitude(petType: PetType, record: WeatherRecord?): WeatherAttitude {
        return if (record == null) NEUTRAL_ATTITUDE
        else WeatherAttitude(
            toCloudPercentage = getAttitudeToCloudPercentage(petType, record.cloudPercentage),
            toTemperature = getAttitudeToTemperature(petType, record.temperature),
            toHumidity = getAttitudeToHumidity(petType, record.humidity),
            toWindSpeed = getAttitudeToWindSpeed(petType, record.windSpeed)
        )
    }

    private fun getAttitudeToCloudPercentage(petType: PetType, value: Int?): Double {
        return when (petType) {
            // Cats love sunny days
            PetType.Catus -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = -1000.0, middle = 30.0, end = 80.0
                ),
            )
            // Dogs love sunny days
            PetType.Dogus -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = -1000.0, middle = 30.0, end = 80.0
                ),
            )
            // Frogs love cloudy days
            PetType.Frogus -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 20.0, middle = 70.0, end = 1000.0
                ),
            )
        }
    }

    private fun getAttitudeToTemperature(petType: PetType, value: Int?): Double {
        return when (petType) {
            // Cats love warm
            PetType.Catus -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 10.0, middle = 22.0, end = 26.0
                ),
            )
            // Dogs love warm
            PetType.Dogus -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 15.0, middle = 23.0, end = 27.0
                ),
            )
            // Frogs love cold
            PetType.Frogus -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 3.0, middle = 10.0, end = 15.0
                ),
            )
        }
    }

    private fun getAttitudeToHumidity(petType: PetType, value: Int?): Double {
        return when (petType) {
            // Cats like dry weather
            PetType.Catus -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = -1000.0, middle = 50.0, end = 90.0
                ),
            )
            // Dogs like dry weather
            PetType.Dogus -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = -1000.0, middle = 50.0, end = 90.0
                ),
            )
            // Frogs like wet weather
            PetType.Frogus -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 30.0, middle = 50.0, end = 1000.0
                ),
            )
        }
    }

    private fun getAttitudeToWindSpeed(petType: PetType, value: Double?): Double {
        return when (petType) {
            // Cats like small wind
            PetType.Catus -> calculateAttitude(
                value = value,
                range = ThreePointRange(
                    start = -1000.0, middle = 5.0, end = 15.0
                ),
            )
            // Dogs like up to medium wind
            PetType.Dogus -> calculateAttitude(
                value = value,
                range = ThreePointRange(
                    start = -1000.0, middle = 10.0, end = 25.0
                ),
            )
            // Frogs don't like wind
            PetType.Frogus -> calculateAttitude(
                value = value,
                range = ThreePointRange(
                    start = -1000.0, middle = 0.0, end = 3.0
                ),
            )
        }
    }

    private fun calculateAttitude(value: Double?, range: ThreePointRange): Double {
        if (value == null) return 0.0
        val result = when {
            value <= range.start || value >= range.end -> -1.0
            value > range.start && value <= range.middle -> {
                val r = range.middle - range.start
                val x = value - range.start
                -1.0 + 2.0 * (x / r)
            }
            value > range.middle && value < range.end -> {
                val r = range.end - range.middle
                val x = value - range.middle
                +1.0 - 2.0 * (x / r)
            }
            else -> 0.0
        }
        return result.coerceIn(-1.0, +1.0)
    }

    companion object {
        private val NEUTRAL_ATTITUDE = WeatherAttitude(
            toCloudPercentage = 0.0,
            toTemperature = 0.0,
            toHumidity = 0.0,
            toWindSpeed = 0.0,
        )
    }
}