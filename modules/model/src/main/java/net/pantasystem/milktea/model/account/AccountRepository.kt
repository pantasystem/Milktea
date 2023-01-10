package net.pantasystem.milktea.model.account

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

interface AccountRepository{

    sealed class Event {
        data class Created(val account: Account) : Event()
        data class Updated(val account: Account) : Event()
        data class Deleted(val accountId: Long) : Event()
    }

    fun interface Listener {
        fun on(e: Event)
    }

    fun addEventListener(listener: Listener)

    fun removeEventListener(listener: Listener)

    suspend fun get(accountId: Long): Result<Account>

    suspend fun add(account: Account, isUpdatePages: Boolean = false): Result<Account>

    suspend fun delete(account: Account)

    suspend fun findAll(): Result<List<Account>>

    suspend fun setCurrentAccount(account: Account): Result<Account>

    suspend fun getCurrentAccount(): Result<Account>
}

@ExperimentalCoroutinesApi
fun AccountRepository.listenEvent(): Flow<AccountRepository.Event> {
    return channelFlow {

        val listener: (e: AccountRepository.Event)-> Unit = {
            trySend(it)
        }
        addEventListener(listener)

        awaitClose {
            removeEventListener(listener)
        }
    }
}


@ExperimentalCoroutinesApi
fun AccountRepository.watchCurrentAccount() : Flow<Account> {
    return this.listenEvent().map {
        this.getCurrentAccount().getOrThrow()
    }.onStart {
        emit(getCurrentAccount().getOrThrow())
    }
}

@ExperimentalCoroutinesApi
fun AccountRepository.watchAccount(accountId: Long) : Flow<Account> {
    return this.listenEvent().map {
        this.get(accountId).getOrThrow()
    }.onStart {
        emit(get(accountId).getOrThrow())
    }
}