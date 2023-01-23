package net.pantasystem.milktea.common


sealed class ResultState<out T>(val content: StateContent<T>) {
    class Fixed<out T>(content: StateContent<T>) : ResultState<T>(content)
    class Loading<out T>(content: StateContent<T>) : ResultState<T>(content)
    class Error<out T>(content: StateContent<T>, val throwable: Throwable) : ResultState<T>(content)

    fun<O> convert(converter: (T)->O) : ResultState<O> {
        val content = when(this.content) {
            is StateContent.Exist -> {
                StateContent.Exist(converter.invoke(this.content.rawContent))
            }
            is StateContent.NotExist -> {
                StateContent.NotExist()
            }
        }

        return when(this) {
            is Fixed -> Fixed(content)
            is Loading -> Loading(content)
            is Error -> Error(content, this.throwable)
        }
    }
    suspend fun<O> suspendConvert(converter: suspend (T)->O) : ResultState<O> {
        val content = when(this.content) {
            is StateContent.Exist -> {
                StateContent.Exist(converter.invoke(this.content.rawContent))
            }
            is StateContent.NotExist -> {
                StateContent.NotExist()
            }
        }

        return when(this) {
            is Fixed -> Fixed(content)
            is Loading -> Loading(content)
            is Error -> Error(content, this.throwable)
        }
    }
}

sealed class StateContent<out T> {
    data class Exist<out T>(val rawContent: T) : StateContent<T>()
    class NotExist<out T> : StateContent<T>()
}

sealed class PageableState<T>(val content: StateContent<T>) {
    class Fixed<T>(content: StateContent<T>) : PageableState<T>(content) {
        fun init() : Loading.Init<T> {
            return Loading.Init()
        }

        fun previous() : Loading.Previous<T> {
            return Loading.Previous(this.content)
        }

        fun future() : Loading.Future<T> {
            return Loading.Future(this.content)
        }
    }
    sealed class Loading<T>(content: StateContent<T>) : PageableState<T>(content) {
        class Init<T> : Loading<T>(StateContent.NotExist())
        class Previous<T>(content: StateContent<T>) : Loading<T>(content)
        class Future<T>(content: StateContent<T>) : Loading<T>(content)

        fun error(throwable: Throwable, content: StateContent<T> = this.content) : Error<T> {
            return Error(content, throwable)
        }

        fun fixed(content: StateContent<T>) : Fixed<T> {
            return Fixed(content)
        }

        fun init() : Init<T> {
            return Init()
        }
    }
    class Error<T>(content: StateContent<T>, val throwable: Throwable) : PageableState<T>(content) {
        fun init() : Loading.Init<T> {
            return Loading.Init()
        }

        fun previous() : Loading.Previous<T> {
            return Loading.Previous(this.content)
        }

        fun future() : Loading.Future<T> {
            return Loading.Future(this.content)
        }
    }

    fun<O> convert(converter: (T)->O) : PageableState<O> {
        val content = when(this.content) {
            is StateContent.Exist -> {
                StateContent.Exist(converter.invoke(this.content.rawContent))
            }
            is StateContent.NotExist -> {
                StateContent.NotExist()
            }
        }

        return when(this) {
            is Fixed -> Fixed(content)
            is Loading.Init -> Loading.Init()
            is Loading.Previous -> Loading.Previous(content)
            is Loading.Future -> Loading.Future(content)
            is Error -> Error(content, this.throwable)
        }
    }
    suspend fun<O> suspendConvert(converter: suspend (T)->O) : PageableState<O> {
        val content = when(this.content) {
            is StateContent.Exist -> {
                StateContent.Exist(converter.invoke(this.content.rawContent))
            }
            is StateContent.NotExist -> {
                StateContent.NotExist()
            }
        }

        return when(this) {
            is Fixed -> Fixed(content)
            is Loading.Init -> Loading.Init()
            is Loading.Previous -> Loading.Previous(content)
            is Loading.Future -> Loading.Future(content)
            is Error -> Error(content, this.throwable)
        }
    }

}