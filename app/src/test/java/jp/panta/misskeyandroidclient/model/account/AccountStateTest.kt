package jp.panta.misskeyandroidclient.model.account

import org.junit.Assert.*

import org.junit.Test

class AccountStateTest {

    @Test
    fun getCurrentAccount() {
        val accountState = AccountState(
            isLoading = false,
            accounts = (1..4).map {
                Account(
                    "id:$it",
                    "host",
                    "name",
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1L
        )
        assertEquals(1L, accountState.currentAccount?.accountId)

        val changedAccountState = accountState.copy(currentAccountId = 2)
        assertEquals(2L, changedAccountState.currentAccount?.accountId)

    }

    @Test
    fun getAccountWhenInvalidCurrentAccountId() {
        val accountState = AccountState(
            isLoading = false,
            accounts = (1..4).map {
                Account(
                    "id:$it",
                    "host",
                    "name",
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1000L
        )
        assertEquals(accountState.accounts.first(), accountState.currentAccount)
    }

    @Test
    fun isUnauthorized() {
        var accountState = AccountState()
        assertFalse(accountState.isUnauthorized)
        accountState = accountState.copy(isLoading = false)
        assertTrue(accountState.isUnauthorized)
    }

    @Test
    fun isUnauthorizedWhenHasAccount() {
        var accountState = AccountState(
            isLoading = false,
            accounts = (1..4).map {
                Account(
                    "id:$it",
                    "host",
                    "name",
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1L
        )
        assertFalse(accountState.isUnauthorized)
        accountState = accountState.copy(isLoading = false)
        assertFalse(accountState.isUnauthorized)
    }

    @Test
    fun hasAccount() {
        val accountState = AccountState(
            isLoading = false,
            accounts = (1..4).map {
                Account(
                    "id:$it",
                    "host",
                    "name",
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1L
        )
        assertTrue(accountState.hasAccount(accountState.accounts.first()))
        assertFalse(accountState.hasAccount(Account(
            "id:100",
            "host",
            "name",
            ""
        ).copy(accountId = 1000L)))
    }

    @Test
    fun add() {
        val accountState = AccountState(
            isLoading = false,
            accounts = (1..4).map {
                Account(
                    "id:$it",
                    "host",
                    "name",
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1L
        )
        val addedState = accountState.add(
            Account(
                "id:10",
                "host",
                "name",
                ""
            ).copy(accountId = 100L)
        )
        assertEquals(100L, addedState.accounts.last().accountId)
    }

    @Test
    fun addWhenUnauthorized() {
        val state = AccountState(isLoading = false)
        assertTrue(state.isUnauthorized)
        val added = state.add(
            Account(
                "id:10",
                "host",
                "name",
                ""
            ).copy(accountId = 100L)
        )
        assertFalse(added.isUnauthorized)
        assertEquals(100L, added.accounts.first().accountId)
        assertEquals(100L, added.currentAccountId)
    }
    @Test
    fun addWhenAdded() {
        val accountState = AccountState(
            isLoading = false,
            accounts = (1..4).map {
                Account(
                    "id:$it",
                    "host",
                    "name",
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1L
        )
        val updated = accountState.add(accountState.accounts.first())
        assertEquals(accountState.accounts.size, updated.accounts.size)
        assertEquals(accountState.accounts, updated.accounts)
    }

    @Test
    fun delete() {

    }
}