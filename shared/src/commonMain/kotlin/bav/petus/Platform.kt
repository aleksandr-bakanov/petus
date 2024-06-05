package bav.petus

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform