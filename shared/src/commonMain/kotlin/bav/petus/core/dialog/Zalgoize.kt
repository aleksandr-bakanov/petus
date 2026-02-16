package bav.petus.core.dialog

import kotlin.random.Random

object Zalgoize {

    val chars = mapOf(
        "up" to listOf(
            '\u030d', '\u030e', '\u0304', '\u0305', '\u033f', '\u0311',
            '\u0306', '\u0310', '\u0352', '\u0357', '\u0351', '\u0307',
            '\u0308', '\u030a', '\u0342', '\u0343', '\u0344', '\u034a',
            '\u034b', '\u034c', '\u0303', '\u0302', '\u030c', '\u0350',
            '\u0300', '\u0301', '\u030b', '\u030f', '\u0312', '\u0313',
            '\u0314', '\u033d', '\u0309', '\u0363', '\u0364', '\u0365',
            '\u0366', '\u0367', '\u0368', '\u0369', '\u036a', '\u036b',
            '\u036c', '\u036d', '\u036e', '\u036f', '\u033e', '\u035b',
            '\u0346', '\u031a'
        ),
        "middle" to listOf(
            '\u0315', '\u031b', '\u0340', '\u0341', '\u0358', '\u0321',
            '\u0322', '\u0327', '\u0328', '\u0334', '\u0335', '\u0336',
            '\u034f', '\u035c', '\u035d', '\u035e', '\u035f', '\u0360',
            '\u0362', '\u0338', '\u0337', '\u0361', '\u0489'
        ),
        "down" to listOf(
            '\u0316', '\u0317', '\u0318', '\u0319', '\u031c', '\u031d',
            '\u031e', '\u031f', '\u0320', '\u0324', '\u0325', '\u0326',
            '\u0329', '\u032a', '\u032b', '\u032c', '\u032d', '\u032e',
            '\u032f', '\u0330', '\u0331', '\u0332', '\u0333', '\u0339',
            '\u033a', '\u033b', '\u033c', '\u0345', '\u0347', '\u0348',
            '\u0349', '\u034d', '\u034e', '\u0353', '\u0354', '\u0355',
            '\u0356', '\u0359', '\u035a', '\u0323'
        )
    )

    private val lookupString = chars.values.flatten().toSet()

    data class Range(val min: Int, val max: Int)

    data class ZalgoLevel(
        val up: Range,
        val middle: Range,
        val down: Range
    )

    private fun randInt(min: Int, max: Int): Int = Random.nextInt(min, max + 1)

    private fun randChar(direction: String): Char {
        val list = chars[direction] ?: return ' '
        return list[Random.nextInt(list.size)]
    }

    fun encode(text: String, level: Any, directions: List<String>, ignoreChars: Set<Char> = setOf()): String = buildString {
        val config = when (level) {
            is ZalgoLevel -> level
            1 -> ZalgoLevel(Range(1, 4), Range(0, 2), Range(1, 4))
            2 -> ZalgoLevel(Range(2, 6), Range(1, 3), Range(2, 6))
            3 -> ZalgoLevel(Range(3, 8), Range(2, 4), Range(3, 8))
            4 -> ZalgoLevel(Range(6, 14), Range(4, 6), Range(6, 14))
            5 -> ZalgoLevel(Range(9, 20), Range(6, 8), Range(9, 20))
            "mega" -> ZalgoLevel(Range(100, 140), Range(20, 30), Range(100, 140))
            else -> ZalgoLevel(Range(0, 0), Range(0, 0), Range(0, 0))
        }

        text.forEach { char ->
            // Skip if the character is already a zalgo char or if it should be ignored
            if (char in lookupString) return@forEach

            append(char)

            if (char in ignoreChars) return@forEach

            if ("up" in directions) {
                repeat(randInt(config.up.min, config.up.max)) { append(randChar("up")) }
            }
            if ("middle" in directions) {
                repeat(randInt(config.middle.min, config.middle.max)) { append(randChar("middle")) }
            }
            if ("down" in directions) {
                repeat(randInt(config.down.min, config.down.max)) { append(randChar("down")) }
            }
        }
    }
}