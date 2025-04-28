package bav.petus.model

enum class PetType {
    Catus,
    Dogus,
    Frogus;

    companion object {
        val names = entries.map { it.name }
    }
}