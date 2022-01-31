package jp.panta.misskeyandroidclient.model.list

import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.datetime.Clock
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
        val userListState = UserListState(mapOf(
            userList.id to userList
        ))
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
        for(page in pages) {
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
        for(page in pages.reversed()) {
            state = state.prependAll(accountId, page)
        }
        println("allLists:${allLists.map{it.id}}")
        assertEquals(allLists.map { it.id }, state.pagedIdsAccountMap[accountId])

    }
}