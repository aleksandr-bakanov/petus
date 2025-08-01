package bav.petus.model

enum class PetType {
    Catus,
    Dogus,
    Frogus,
    Bober,
    Fractal;

    companion object {
        val names = entries.map { it.name }
    }
}