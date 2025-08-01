package bav.petus.model

enum class FractalType {
    Gosper,
    Koch,
    Sponge;

    companion object {
        val names = entries.map { it.name }
    }
}