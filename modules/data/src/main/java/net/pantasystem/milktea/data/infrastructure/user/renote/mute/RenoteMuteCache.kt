package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import javax.inject.Inject

class RenoteMuteCache @Inject constructor() {

    private var map = mapOf<User.Id, RenoteMute>()
    private var notFounds = setOf<User.Id>()

    fun clearBy(accountId: Long) {
        synchronized(this) {
            map = map.filterNot {
                it.key.accountId == accountId
            }
            notFounds = notFounds.filterNot {
                it.accountId == accountId
            }.toSet()
        }
    }

    fun put(userId: User.Id, renoteMute: RenoteMute) {
        synchronized(this) {
            notFounds = notFounds.toMutableSet().also {
                it.remove(userId)
            }
            map = map.toMutableMap().also {
                it[userId] = renoteMute
            }
        }
    }

    fun putAll(list: List<Pair<User.Id, RenoteMute>>) {
        synchronized(this) {
            val mutableNotFounds = notFounds.toMutableSet()
            val cache = map.toMutableMap()
            for (el in list) {
                mutableNotFounds.remove(el.first)
                cache[el.first] = el.second
            }
            map = cache
            notFounds = mutableNotFounds
        }
    }

    fun exists(userId: User.Id): Boolean {
        if (notFounds.contains(userId)) {
            return false
        }
        return map[userId] != null
    }

    fun remove(userId: User.Id) {
        synchronized(this) {
            notFounds = notFounds.toMutableSet().also {
                it.add(userId)
            }
            map = map.toMutableMap().also {
                it.remove(userId)
            }
        }
    }
}