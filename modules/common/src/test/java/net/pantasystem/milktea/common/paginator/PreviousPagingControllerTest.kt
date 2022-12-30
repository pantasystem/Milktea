package net.pantasystem.milktea.common.paginator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class PreviousPagingControllerTest {

    @Test
    fun loadPrevious_throwsError() {
        val stateEvents = mutableListOf<PageableState<List<Int>>>()
        val store = object : PaginationState<Int>, IdGetter<String>, PreviousLoader<String> {
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
                return null
            }

            override suspend fun getUntilId(): String? {
                return (localState.value.content as? StateContent.Exist)?.rawContent?.lastOrNull()
                    ?.toString()
            }

            override suspend fun loadPrevious(): Result<List<String>> {
                throw Exception()
            }
        }
        val previousPagingController = PreviousPagingController<String, Int>(
            entityConverter = object : EntityConverter<String, Int> {
                override suspend fun convertAll(list: List<String>): List<Int> {
                    return list.map { it.toInt() }
                }
            },
            locker = object : StateLocker {
                override val mutex: Mutex = Mutex()
            },
            previousLoader = store,
            state = store,
            dispatcher = Dispatchers.Default
        )

        runBlocking {
            previousPagingController.loadPrevious()
        }
        assertEquals(2, stateEvents.size)
        assertEquals(PageableState.Loading.Previous::class.java,stateEvents[0].javaClass)
        assertEquals(PageableState.Error::class.java,stateEvents[1].javaClass)
    }

    @Test
    fun loadPrevious() {
        val stateEvents = mutableListOf<PageableState<List<Int>>>()
        val store = object : PaginationState<Int>, IdGetter<String>, PreviousLoader<String> {
            private val localState =
                MutableStateFlow<PageableState<List<Int>>>(PageableState.Loading.Init<List<Int>>())
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
                return null
            }

            override suspend fun getUntilId(): String? {
                return (localState.value.content as? StateContent.Exist)?.rawContent?.lastOrNull()
                    ?.toString()
            }

            override suspend fun loadPrevious(): Result<List<String>> {
                val next = (getUntilId()?.toInt()?: 0) + 1
                return runCancellableCatching {
                    (next until (next + 20)).map { it.toString() }
                }
            }
        }
        val previousPagingController = PreviousPagingController<String, Int>(
            entityConverter = object : EntityConverter<String, Int> {
                override suspend fun convertAll(list: List<String>): List<Int> {
                    return list.map { it.toInt() }
                }
            },
            locker = object : StateLocker {
                override val mutex: Mutex = Mutex()
            },
            previousLoader = store,
            state = store
        )

        runBlocking {
            previousPagingController.loadPrevious()
            previousPagingController.loadPrevious()
            previousPagingController.loadPrevious()
            assertEquals(60, (store.getState().content as StateContent.Exist).rawContent.size)
        }
        assertEquals(6, stateEvents.size)
        assertEquals(PageableState.Loading.Previous::class.java,stateEvents[0].javaClass)

        assertEquals(PageableState.Fixed::class.java,stateEvents[1].javaClass)

        assertEquals(PageableState.Loading.Previous::class.java,stateEvents[2].javaClass)
        assertEquals(PageableState.Fixed::class.java,stateEvents[3].javaClass)

        assertEquals(PageableState.Loading.Previous::class.java,stateEvents[4].javaClass)
        assertEquals(PageableState.Fixed::class.java,stateEvents[5].javaClass)

        assertEquals((1..60).toList(), (store.getState().content as StateContent.Exist).rawContent)
    }
}