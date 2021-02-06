package jp.panta.misskeyandroidclient.model.users

import jp.panta.misskeyandroidclient.model.AddResult
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun observable(): Flow<Event>

    sealed class Event{
        data class Added(val user: User): Event()
        data class Removed(val userId: User.Id): Event()
    }

    suspend fun get(userId: User.Id): User?

    suspend fun add(user: User): AddResult

    suspend fun addAll(users: List<User>): List<AddResult>

    suspend fun remove(user: User): Boolean
}