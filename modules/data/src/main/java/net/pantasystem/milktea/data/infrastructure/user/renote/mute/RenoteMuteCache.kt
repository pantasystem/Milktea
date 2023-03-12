package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import javax.inject.Inject

class RenoteMuteCache @Inject constructor() {

    private var map = mapOf<User.Id, RenoteMute>()
    private var notFounds = setOf<User.Id>()

    fun clearBy(accountId: Long) {
        synchronized(this) {
            val newMap = map.filterNot {
                it.key.accountId == accountId
            }
            notFounds = notFounds + map.map {
                it.key
            }.filter {
                it.accountId == accountId
            }
            map = newMap
        }
    }

    fun add(renoteMute: RenoteMute) {
        synchronized(this) {
            notFounds = notFounds.toMutableSet().also {
                it.remove(renoteMute.userId)
            }
            map = map.toMutableMap().also {
                it[renoteMute.userId] = renoteMute
            }
        }
    }

    fun addAll(list: List<RenoteMute>) {
        synchronized(this) {
            val mutableNotFounds = notFounds.toMutableSet()
            val cache = map.toMutableMap()
            for (el in list) {
                mutableNotFounds.remove(el.userId)
                cache[el.userId] = el
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

    fun isNotFound(userId: User.Id): Boolean {
        return notFounds.contains(userId)
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