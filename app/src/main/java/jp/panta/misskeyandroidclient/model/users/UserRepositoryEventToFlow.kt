package jp.panta.misskeyandroidclient.model.users

import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.reflect.KClass

/**
 * UserRepositoryのイベントをFlowに変換する
 */
class UserRepositoryEventToFlow(
    val userRepository: UserRepository
) : UserRepository.Listener{

    private val userIdWithListener = mutableMapOf<User.Id, MutableSet<(e: UserRepository.Event)->Unit>>()

    init {
        userRepository.addEventListener(this)
    }

    @ExperimentalCoroutinesApi
    fun from(userId: User.Id): Flow<UserRepository.Event> {
        return channelFlow {
            val callback: (UserRepository.Event)-> Unit = { ev ->
                offer(ev)
            }

            listen(userId, callback)
            awaitClose {
                unListen(userId, callback)
            }
        }
    }

    private fun listen(userId: User.Id, callback: (e: UserRepository.Event)->Unit) {
        synchronized(userIdWithListener) {
            val listeners = userIdWithListener[userId]
                ?: mutableSetOf()

            listeners.add(callback)
            userIdWithListener[userId] = listeners
        }
    }

    private fun unListen(userId: User.Id, callback: (e: UserRepository.Event)->Unit) {
        synchronized(userIdWithListener) {
            val listeners = userIdWithListener[userId]
            listeners?.remove(callback)
            listeners?.let{
                userIdWithListener[userId] = listeners
            }
        }
    }

    override fun on(e: UserRepository.Event) {
        synchronized(userIdWithListener) {
            userIdWithListener[e.userId]?.forEach {
                it.invoke(e)
            }
        }
    }

}