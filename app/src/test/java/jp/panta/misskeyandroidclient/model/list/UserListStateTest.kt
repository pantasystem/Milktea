package jp.panta.misskeyandroidclient.model.list

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.app_store.userlist.UserListState
import org.junit.Assert.*

import org.junit.Test

class UserListStateTest {

    @Test
    fun deleted() {
        val userList = UserList(
            UserList.Id(
                0, "listId"
            ),
            name = "",
            createdAt = Clock.System.now(),
            userIds = emptyList()
        )
        val userListState = UserListState(
            mapOf(
                userList.id to userList
            )
        )
        val deleted = userListState.deleted(userList.id)
        assertEquals(0, deleted.userLists.size)
    }

    @Test
    fun created() {
        val newUserList = UserList(
            UserList.Id(
                0, "listId"
            ),
            name = "",
            createdAt = Clock.System.now(),
            userIds = emptyList()
        )

        val userListState = UserListState(emptyMap())
        val updatedState = userListState.created(newUserList)
        assertEquals(1, updatedState.userLists.size)
        assertEquals(newUserList, updatedState.userLists[0])
    }

    @Test
    fun updated() {
        val userList = UserList(
            UserList.Id(
                0, "listId"
            ),
            name = "",
            createdAt = Clock.System.now(),
            userIds = emptyList()
        )
        val userListState = UserListState(
            mapOf(
                userList.id to userList
            )
        )

        val updated = userList.copy(
            name = "updated name"
        )
        val updatedState = userListState.updated(
            updated
        )
        assertEquals(1, updatedState.userLists.size)
        assertEquals(updated, updatedState.userLists[0])
    }

    @Test
    fun appendAll() {
        val accountId = 0L
        val userList = UserList(
            UserList.Id(
                accountId, "listId"
            ),
            name = "",
            createdAt = Clock.System.now(),
            userIds = emptyList()
        )

        val allLists = (0 until 20).map {
            userList.copy(
                id = userList.id.copy(
                    userListId = "listId$it"
                )
            )
        }
        val pages = allLists.chunked(5)

        var state = UserListState(emptyMap())
        for (page in pages) {
            state = state.appendAll(accountId, page)
        }
        assertEquals(allLists.map { it.id }, state.pagedIdsAccountMap[accountId])


    }

    @Test
    fun prependAll() {
        val accountId = 0L
        val userList = UserList(
            UserList.Id(
                accountId, "listId"
            ),
            name = "",
            createdAt = Clock.System.now(),
            userIds = emptyList()
        )

        val allLists = (0 until 20).map {
            userList.copy(
                id = userList.id.copy(
                    userListId = "listId$it"
                )
            )
        }
        val pages = allLists.chunked(5)

        var state = UserListState(emptyMap())
        for (page in pages.reversed()) {
            state = state.prependAll(accountId, page)
        }
        println("allLists:${allLists.map { it.id }}")
        assertEquals(allLists.map { it.id }, state.pagedIdsAccountMap[accountId])

    }

    @Test
    fun replaceAll() {
        val accountId = 0L
        val userList = UserList(
            UserList.Id(
                accountId, "listId"
            ),
            name = "",
            createdAt = Clock.System.now(),
            userIds = emptyList()
        )

        val allLists = (0 until 20).map {
            userList.copy(
                id = userList.id.copy(
                    userListId = "listId$it"
                )
            )
        }

        val state = UserListState(emptyMap())
        state.appendAll(accountId, allLists)

        val replacedLists = allLists.map {
            it.copy(id = it.id.copy(userListId = it.id.userListId + "+replaced"))
        }

        val replacedState = state.replaceAll(accountId, replacedLists)
        assertEquals(replacedLists.map { it.id }, replacedState.pagedIdsAccountMap[accountId])
    }

    @Test
    fun getUserLists() {
        val accountId = 0L
        val userList = UserList(
            UserList.Id(
                accountId, "listId"
            ),
            name = "",
            createdAt = Clock.System.now(),
            userIds = emptyList()
        )

        val allLists = (0 until 20).map {
            userList.copy(
                id = userList.id.copy(
                    userListId = "listId$it"
                )
            )
        }
        var state = UserListState(emptyMap())
        state = state.replaceAll(accountId, allLists)
            .replaceAll(accountId + 1, allLists.map {
                it.copy(id = it.id.copy(accountId = accountId + 1))
            })
        assertEquals(allLists, state.getUserLists(accountId))
    }
}