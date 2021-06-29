package jp.panta.misskeyandroidclient.util


sealed class State<out T>(val content: StateContent<T>) {
    class Fixed<out T>(content: StateContent<T>) : State<T>(content)
    class Loading<out T>(content: StateContent<T>) : State<T>(content)
    class Error<out T>(content: StateContent<T>, val throwable: Throwable) : State<T>(content)
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
    sealed class Loading<T>(val content: StateContent<T>) {
        class Init<T>() : Loading<T>(StateContent.NotExist())
        class Previous<T>(content: StateContent<T>) : Loading<T>(content)
        class Future<T>(content: StateContent<T>) : Loading<T>(content)

        fun error(throwable: Throwable, content: StateContent<T> = this.content) : Error<T>{
            return Error(content, throwable)
        }

        fun fixed(content: StateContent<T>) : Fixed<T>{
            return Fixed(content)
        }

        fun init() : Init<T>{
            return Init()
        }
    }
    class Error<T>(content: StateContent<T>, val throwable: Throwable) : State<T>(content) {
        fun init() : PageableState.Loading.Init<T> {
            return PageableState.Loading.Init()
        }

        fun previous() : PageableState.Loading.Previous<T> {
            return PageableState.Loading.Previous(this.content)
        }

        fun future() : PageableState.Loading.Future<T> {
            return PageableState.Loading.Future(this.content)
        }
    }

}