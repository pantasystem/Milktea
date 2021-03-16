package jp.panta.misskeyandroidclient.model.users

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

/**
 * UserRepositoryのイベントをFlowに変換する
 */
class UserRepositoryEventToFlow(
    val userDataSource: UserDataSource
) : UserDataSource.Listener{

    private val userIdWithListener = mutableMapOf<User.Id, MutableSet<(e: UserDataSource.Event)->Unit>>()

    init {
        userDataSource.addEventListener(this)
    }

    @ExperimentalCoroutinesApi
    fun from(userId: User.Id): Flow<UserDataSource.Event> {
        return channelFlow {
            val callback: (UserDataSource.Event)-> Unit = { ev ->
                offer(ev)
            }

            listen(userId, callback)
            awaitClose {
                unListen(userId, callback)
            }
        }
    }

    private fun listen(userId: User.Id, callback: (e: UserDataSource.Event)->Unit) {
        synchronized(userIdWithListener) {
            val listeners = userIdWithListener[userId]
                ?: mutableSetOf()

            listeners.add(callback)
            userIdWithListener[userId] = listeners
        }
    }

    private fun unListen(userId: User.Id, callback: (e: UserDataSource.Event)->Unit) {
        synchronized(userIdWithListener) {
            val listeners = userIdWithListener[userId]
            listeners?.remove(callback)
            listeners?.let{
                userIdWithListener[userId] = listeners
            }
        }
    }

    override fun on(e: UserDataSource.Event) {
        synchronized(userIdWithListener) {
            userIdWithListener[e.userId]?.forEach {
                it.invoke(e)
            }
        }
    }

}