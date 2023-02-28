package net.pantasystem.milktea.api

object CurrentClassLoader {

    private var _classLoader: ClassLoader? = null
    operator fun invoke(): ClassLoader? {
        return (_classLoader ?: javaClass.classLoader).also {
            _classLoader = it
        }
    }
}