package bav.petus.model

import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val id: Long = 0,

    val name: String = "",
    val type: PetType = PetType.Frogus,

    var isDead: Boolean = false,
    val creationTick: Long = 0L,
    var ageState: AgeState = AgeState.Egg,
    var illnessPossibility: Float = 0f,

    var activeSleepTick: Long = 0L,
    var activeSleepState: SleepState = SleepState.Active,

    var satiety: Float = 100F,
    var psych: Float = 100F,
    var health: Float = 100F,

    var illness: Boolean = false,
    var isPooped: Boolean = false,

    var deathOfOldAgePossibility: Float = 0f,
    var timeOfDeath: String = "",
) {
    val sleep: Boolean
        get() = activeSleepState == SleepState.Sleep
}