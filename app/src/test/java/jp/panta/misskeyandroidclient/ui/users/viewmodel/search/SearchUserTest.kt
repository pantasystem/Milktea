package jp.panta.misskeyandroidclient.ui.users.viewmodel.search

import net.pantasystem.milktea.user.search.SearchUser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class SearchUserTest {

    @Test
    fun isUserName() {
        val searchUser = SearchUser("harunon", null)
        Assertions.assertTrue(searchUser.isUserName)
        Assertions.assertTrue(SearchUser("nocturne_db", null).isUserName)
    }

    @Test
    fun isUserNameGiveNotUserNameCases() {
        val searchUser = SearchUser("はるのん", null)
        Assertions.assertFalse(searchUser.isUserName)
    }
}