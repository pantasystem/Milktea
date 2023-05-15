package net.pantasystem.milktea.note.reaction

object ImageAspectCache {

    private var cache = mutableMapOf<String, Float>()

    fun put(url: String?, aspect: Float) {
        url?: return
        synchronized(this) {
            cache[url] = aspect
        }
    }

    fun get(url: String?): Float? {
        url ?: return null
        return cache[url]
    }
}