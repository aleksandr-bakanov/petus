package bav.petus.model

enum class PetType {
    Catus,
    Dogus,
    Frogus,
    Bober,
    Fractal,
    Dragon,
    Alien;

    companion object {
        val names = entries.map { it.name }
    }
}