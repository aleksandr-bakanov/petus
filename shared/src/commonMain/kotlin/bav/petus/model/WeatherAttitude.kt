package bav.petus.model

/**
 * Describes relation of particular pet to particular weather condition.
 * Values are between -1.0 (negative feelings) and +1.0 (positive feelings)
 */
data class WeatherAttitude(
    val toCloudPercentage: Double,
    val toTemperature: Double,
    val toHumidity: Double,
    val toWindSpeed: Double,
)

/**
 * Represents range of relation to some weather condition.
 *
 * Middle point describes most joyful condition.
 * Start and End points describes points beyond which lies most unpleasant condition.
 *
 * Example:
 *   Assuming relation of some pet to temperature described by
 *   ThreePointRange(start = 10.0, middle = 22.0, end = 26.0)
 *   That means:
 *     - Anything less than 10 degree considered as most unpleasant - multiplier -1
 *     - Exactly 22 degree - most joyful - multiplier +1
 *     - Anything more than 26 degree - most unpleasant - multiplier -1
 *     - There are two points: [start + (middle - start) / 2 = 16] and
 *       [middle + (end - middle) / 2 = 24] that represent neutral relation to temperature -
 *       multiplier 0
 *     - Between start, middle and end points relation is somewhere between -1 and +1
 *
 *     0         5         10        15        20 22     25 26     30    <- temperature
 *     |---------|---------|---------|---------|---------|---------|
 *     -1        -1       (-1)  -.5    0    +.5  (+1)  0  (-1)     -1    <- multiplier
 */
data class ThreePointRange(
    val start: Double,
    val middle: Double,
    val end: Double
)
