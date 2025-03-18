package bav.petus.model

enum class SleepState {
    Active, Sleep;

    fun not(): SleepState {
        return when (this) {
            Active -> Sleep
            Sleep -> Active
        }
    }
}