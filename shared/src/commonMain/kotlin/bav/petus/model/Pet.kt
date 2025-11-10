package bav.petus.model

import bav.petus.core.inventory.InventoryItem
import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val id: Long = 0,

    val name: String = "",
    val type: PetType = PetType.Frogus,
    val fractalType: FractalType = FractalType.Gosper,
    val dragonType: DragonType = DragonType.Red,

    val place: Place = Place.Zoo,
    val bodyState: BodyState = BodyState.Alive,
    val burialType: BurialType = BurialType.Buried,

    // Timestamp in seconds since epoch
    val creationTime: Long = 0L,
    val ageState: AgeState = AgeState.Egg,
    val illnessPossibility: Float = 0f,

    val lastActiveSleepSwitchTimestamp: Long = 0L,
    val activeSleepState: SleepState = SleepState.Sleep,

    val satiety: Float = 100F,
    val psych: Float = 100F,
    val health: Float = 100F,

    val illness: Boolean = false,
    val isPooped: Boolean = false,

    val deathOfOldAgePossibility: Float = 0f,
    val timeOfDeath: Long = 0L,

    val inventory: List<InventoryItem> = listOf(),

    // If now is before this timestamp - play with pet is not allowed
    val timestampPlayAllowed: Long = 0L,
) {
    val sleep: Boolean
        get() = activeSleepState == SleepState.Sleep
}