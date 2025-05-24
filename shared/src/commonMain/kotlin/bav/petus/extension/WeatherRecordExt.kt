package bav.petus.extension

import bav.petus.cache.WeatherRecord

fun WeatherRecord.str(): String {
    return buildString {
        append(timestampSecondsSinceEpoch.epochTimeToString())
        append(" | c:")
        append(cloudPercentage)
        append(" | h:")
        append(humidity)
        append(" | t:")
        append(temperature)
        append(" | w:")
        append(windSpeed?.toInt())
    }
}
