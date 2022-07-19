package jp.panta.misskeyandroidclient.ui.users.viewmodel.search

import org.junit.Assert
import org.junit.Test

class SearchUserTest {

    @Test
    fun isUserName() {
        val searchUser = SearchUser("harunon", null)
        Assert.assertTrue(searchUser.isUserName)
        Assert.assertTrue(SearchUser("nocturne_db", null).isUserName)
    }

    @Test
    fun isUserNameGiveNotUserNameCases() {
        val searchUser = SearchUser("はるのん", null)
        Assert.assertFalse(searchUser.isUserName)
    }
}