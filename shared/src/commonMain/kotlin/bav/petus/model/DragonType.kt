package bav.petus.model

enum class DragonType {
    Red,
    Blue,
    Void;

    companion object {
        val names = entries.map { it.name }
    }
}