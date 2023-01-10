package net.pantasystem.milktea.model.instance

object HostWithVersion {

    private val hostWithVersion = mutableMapOf<String, Version>()

    fun put(host: String, version: Version) {
        synchronized(this) {
            hostWithVersion[host] = version
        }
    }

    fun get(host: String): Version? {
        return synchronized(this) {
            hostWithVersion[host]
        }
    }

    fun isOverV13(host: String?, default: Boolean = false): Boolean {
        if (host == null) {
            return false
        }
        return get(host)?.let {
            it >= Version("13.0.0")
        } ?: default
    }

}