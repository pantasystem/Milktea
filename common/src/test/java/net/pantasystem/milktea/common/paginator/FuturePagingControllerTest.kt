package net.pantasystem.milktea.common.paginator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.State
import net.pantasystem.milktea.common.StateContent
import org.junit.Assert.*

import org.junit.Test

class FuturePagingControllerTest {

    @Test
    fun loadFuture_whenThrowsError() {
        val stateEvents = mutableListOf<PageableState<List<Int>>>()
        val store = object : PaginationState<Int>, IdGetter<String>, FutureLoader<String> {
            private val localState =
                MutableStateFlow<PageableState<List<Int>>>(PageableState.Loading.Init())
            override val state: Flow<PageableState<List<Int>>>
                get() = localState

            override fun getState(): PageableState<List<Int>> {
                return localState.value
            }

            override fun setState(state: PageableState<List<Int>>) {
                stateEvents.add(state)
                localState.value = state
            }

            override suspend fun getSinceId(): String? {
                return (localState.value.content as? StateContent.Exist)?.rawContent?.firstOrNull()
                    ?.toString()
            }

            override suspend fun getUntilId(): String? {
                return null
            }

            override suspend fun loadFuture(): Result<List<String>> {
                throw Exception()
            }
        }
        val futurePagingController = FuturePagingController<String, Int>(
            entityConverter = object : EntityConverter<String, Int> {
                override suspend fun convertAll(list: List<String>): List<Int> {
                    return list.map { it.toInt() }
                }
            },
            locker = object : StateLocker {
                override val mutex: Mutex = Mutex()
            },
            futureLoader = store,
            state = store
        )

        runBlocking {
            futurePagingController.loadFuture()
        }
        assertEquals(PageableState.Error::class.java,stateEvents[1].javaClass)

    }

    @Test
    fun loadFuture() {
        val stateEvents = mutableListOf<PageableState<List<Int>>>()
        val store = object : PaginationState<Int>, IdGetter<String>, FutureLoader<String> {
            private val localState =
                MutableStateFlow<PageableState<List<Int>>>(PageableState.Loading.Init())
            override val state: Flow<PageableState<List<Int>>>
                get() = localState

            override fun getState(): PageableState<List<Int>> {
                return localState.value
            }

            override fun setState(state: PageableState<List<Int>>) {
                stateEvents.add(state)
                localState.value = state
            }

            override suspend fun getSinceId(): String? {
                return (localState.value.content as? StateContent.Exist)?.rawContent?.firstOrNull()
                    ?.toString()
            }

            override suspend fun getUntilId(): String? {
                return null
            }

            override suspend fun loadFuture(): Result<List<String>> {
                val nextId = (getSinceId()?.toInt() ?: 61) - 1
                return runCatching {
                    (0.coerceAtLeast(nextId - 20) .. nextId).toList().asReversed().map {
                        it.toString()
                    }
                }
            }
        }
        val futurePagingController = FuturePagingController<String, Int>(
            entityConverter = object : EntityConverter<String, Int> {
                override suspend fun convertAll(list: List<String>): List<Int> {
                    return list.map { it.toInt() }
                }
            },
            locker = object : StateLocker {
                override val mutex: Mutex = Mutex()
            },
            futureLoader = store,
            state = store
        )

        runBlocking {
            futurePagingController.loadFuture()
            futurePagingController.loadFuture()
            futurePagingController.loadFuture()
        }

        assertEquals(6, stateEvents.size)
        assertEquals((0..60).toList(), (store.getState().content as StateContent.Exist).rawContent)
    }
}