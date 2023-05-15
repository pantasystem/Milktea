package net.pantasystem.milktea.note.reaction

import android.util.SparseArray

object ImageAspectRatioCache {

    private var cache = SparseArray<Float>()

    fun put(url: String?, aspect: Float) {
        url?: return
        synchronized(this) {
            cache[url.hashCode()] = aspect
        }
    }

    fun get(url: String?): Float? {
        url ?: return null
        return cache[url.hashCode()]
    }
}