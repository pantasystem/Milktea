package jp.panta.misskeyandroidclient.model.account

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountState
import org.junit.Assert.*

import org.junit.Test

class AccountStateTest {

    @Test
    fun getCurrentAccount() {
        val accountState = net.pantasystem.milktea.model.account.AccountState(
            isLoading = false,
            accounts = (1..4).map {
                net.pantasystem.milktea.model.account.Account(
                    "id:$it",
                    "host",
                    "name",
                    net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
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
        val accountState = net.pantasystem.milktea.model.account.AccountState(
            isLoading = false,
            accounts = (1..4).map {
                net.pantasystem.milktea.model.account.Account(
                    "id:$it",
                    "host",
                    "name",
                    net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1000L
        )
        assertEquals(accountState.accounts.first(), accountState.currentAccount)
    }

    @Test
    fun isUnauthorized() {
        var accountState = net.pantasystem.milktea.model.account.AccountState()
        assertFalse(accountState.isUnauthorized)
        accountState = accountState.copy(isLoading = false)
        assertTrue(accountState.isUnauthorized)
    }

    @Test
    fun isUnauthorizedWhenHasAccount() {
        var accountState = net.pantasystem.milktea.model.account.AccountState(
            isLoading = false,
            accounts = (1..4).map {
                net.pantasystem.milktea.model.account.Account(
                    "id:$it",
                    "host",
                    "name",
                    net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
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
        val accountState = net.pantasystem.milktea.model.account.AccountState(
            isLoading = false,
            accounts = (1..4).map {
                net.pantasystem.milktea.model.account.Account(
                    "id:$it",
                    "host",
                    "name",
                    net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1L
        )
        assertTrue(accountState.hasAccount(accountState.accounts.first()))
        assertFalse(
            accountState.hasAccount(
                net.pantasystem.milktea.model.account.Account(
                    "id:100",
                    "host",
                    "name",
                    net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
                    ""
                ).copy(accountId = 1000L)
            )
        )
    }

    @Test
    fun add() {
        val accountState = net.pantasystem.milktea.model.account.AccountState(
            isLoading = false,
            accounts = (1..4).map {
                net.pantasystem.milktea.model.account.Account(
                    "id:$it",
                    "host",
                    "name",
                    net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1L
        )
        val addedState = accountState.add(
            net.pantasystem.milktea.model.account.Account(
                "id:10",
                "host",
                "name",
                net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
                ""
            ).copy(accountId = 100L)
        )
        assertEquals(100L, addedState.accounts.last().accountId)
    }

    @Test
    fun addWhenUnauthorized() {
        val state = net.pantasystem.milktea.model.account.AccountState(isLoading = false)
        assertTrue(state.isUnauthorized)
        val added = state.add(
            net.pantasystem.milktea.model.account.Account(
                "id:10",
                "host",
                "name",
                net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
                ""
            ).copy(accountId = 100L)
        )
        assertFalse(added.isUnauthorized)
        assertEquals(100L, added.accounts.first().accountId)
        assertEquals(100L, added.currentAccountId)
    }

    @Test
    fun addWhenAdded() {
        val accountState = net.pantasystem.milktea.model.account.AccountState(
            isLoading = false,
            accounts = (1..4).map {
                net.pantasystem.milktea.model.account.Account(
                    "id:$it",
                    "host",
                    "name",
                    net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
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
        val accountState = net.pantasystem.milktea.model.account.AccountState(
            isLoading = false,
            accounts = (1..4).map {
                net.pantasystem.milktea.model.account.Account(
                    "id:$it",
                    "host",
                    "name",
                    net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY,
                    ""
                ).copy(accountId = it.toLong())
            },
            currentAccountId = 1L
        )
        val deleted = accountState.delete(accountState.accounts.first().accountId)
        assertArrayEquals(
            (2L..4L).toList().toLongArray(),
            deleted.accounts.map { it.accountId }.toLongArray()
        )
        assertNotEquals(deleted.currentAccountId, 1L)

    }
}