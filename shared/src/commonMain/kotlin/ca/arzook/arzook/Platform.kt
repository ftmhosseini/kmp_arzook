package ca.arzook.arzook

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform