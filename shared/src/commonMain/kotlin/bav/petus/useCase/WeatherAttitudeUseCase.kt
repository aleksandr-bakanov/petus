package bav.petus.useCase

import bav.petus.cache.WeatherRecord
import bav.petus.model.DragonType
import bav.petus.model.Pet
import bav.petus.model.PetType
import bav.petus.model.ThreePointRange
import bav.petus.model.WeatherAttitude

class WeatherAttitudeUseCase {

    fun convertWeatherRecordToAttitude(pet: Pet, record: WeatherRecord?): WeatherAttitude {
        return if (record == null) NEUTRAL_ATTITUDE
        else WeatherAttitude(
            toCloudPercentage = getAttitudeToCloudPercentage(pet, record.cloudPercentage),
            toTemperature = getAttitudeToTemperature(pet, record.temperature),
            toHumidity = getAttitudeToHumidity(pet, record.humidity),
            toWindSpeed = getAttitudeToWindSpeed(pet, record.windSpeed)
        )
    }

    private fun getAttitudeToCloudPercentage(pet: Pet, value: Int?): Double {
        return when (pet.type) {
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
            // Bober love sunny days
            PetType.Bober -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = -1000.0, middle = 30.0, end = 80.0
                ),
            )
            // Fractal doesn't care about weather
            PetType.Fractal -> NEUTRAL_ATTITUDE.toCloudPercentage
            // Dragons
            PetType.Dragon -> {
                val range = when (pet.dragonType) {
                    DragonType.Red -> ThreePointRange(start = -1000.0, middle = 0.0, end = 30.0)
                    DragonType.Blue -> ThreePointRange(start = 0.0, middle = 50.0, end = 100.0)
                    DragonType.Void -> ThreePointRange(start = 70.0, middle = 100.0, end = 1000.0)
                }
                calculateAttitude(
                    value = value?.toDouble(),
                    range = range,
                )
            }
            // Alien love cloudy days
            PetType.Alien -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 20.0, middle = 70.0, end = 1000.0
                ),
            )
        }
    }

    private fun getAttitudeToTemperature(pet: Pet, value: Int?): Double {
        return when (pet.type) {
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
            // Bober loves warm
            PetType.Bober -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 15.0, middle = 23.0, end = 27.0
                ),
            )
            // Fractal doesn't care about weather
            PetType.Fractal -> NEUTRAL_ATTITUDE.toTemperature
            // Dragons
            PetType.Dragon -> {
                val range = when (pet.dragonType) {
                    DragonType.Red -> ThreePointRange(start = 20.0, middle = 30.0, end = 80.0)
                    DragonType.Blue -> ThreePointRange(start = -10.0, middle = 5.0, end = 10.0)
                    DragonType.Void -> ThreePointRange(start = -1000.0, middle = -50.0, end = -20.0)
                }
                calculateAttitude(
                    value = value?.toDouble(),
                    range = range,
                )
            }
            // Alien love cold
            PetType.Alien -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 3.0, middle = 10.0, end = 15.0
                ),
            )
        }
    }

    private fun getAttitudeToHumidity(pet: Pet, value: Int?): Double {
        return when (pet.type) {
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
            // Bober likes wet weather
            PetType.Bober -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 30.0, middle = 50.0, end = 1000.0
                ),
            )
            // Fractal doesn't care about weather
            PetType.Fractal -> NEUTRAL_ATTITUDE.toHumidity
            // Dragons
            PetType.Dragon -> {
                val range = when (pet.dragonType) {
                    DragonType.Red -> ThreePointRange(start = -1000.0, middle = 50.0, end = 90.0)
                    DragonType.Blue -> ThreePointRange(start = 30.0, middle = 50.0, end = 1000.0)
                    DragonType.Void -> ThreePointRange(start = 0.0, middle = 50.0, end = 100.0)
                }
                calculateAttitude(
                    value = value?.toDouble(),
                    range = range,
                )
            }
            // Alien like wet weather
            PetType.Alien -> calculateAttitude(
                value = value?.toDouble(),
                range = ThreePointRange(
                    start = 30.0, middle = 50.0, end = 1000.0
                ),
            )
        }
    }

    private fun getAttitudeToWindSpeed(pet: Pet, value: Double?): Double {
        return when (pet.type) {
            // Cats like small wind
            PetType.Catus -> calculateAttitude(
                value = value,
                range = ThreePointRange(
                    start = -1000.0, middle = 0.5, end = 1.5
                ),
            )
            // Dogs like up to medium wind
            PetType.Dogus -> calculateAttitude(
                value = value,
                range = ThreePointRange(
                    start = -1000.0, middle = 1.0, end = 2.5
                ),
            )
            // Frogs don't like wind
            PetType.Frogus -> calculateAttitude(
                value = value,
                range = ThreePointRange(
                    start = -1000.0, middle = 0.0, end = 0.3
                ),
            )
            // Bober does not like wind
            PetType.Bober -> calculateAttitude(
                value = value,
                range = ThreePointRange(
                    start = -1000.0, middle = 0.0, end = 0.3
                ),
            )
            // Fractal doesn't care about weather
            PetType.Fractal -> NEUTRAL_ATTITUDE.toWindSpeed
            // Dragons
            PetType.Dragon -> {
                val range = when (pet.dragonType) {
                    DragonType.Red -> ThreePointRange(start = -1000.0, middle = 1.0, end = 2.5)
                    DragonType.Blue -> ThreePointRange(start = -1000.0, middle = 0.5, end = 1.5)
                    DragonType.Void -> ThreePointRange(start = -1000.0, middle = 0.0, end = 0.3)
                }
                calculateAttitude(
                    value = value,
                    range = range,
                )
            }
            // Alien like up to medium wind
            PetType.Alien -> calculateAttitude(
                value = value,
                range = ThreePointRange(
                    start = -1000.0, middle = 1.0, end = 2.5
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