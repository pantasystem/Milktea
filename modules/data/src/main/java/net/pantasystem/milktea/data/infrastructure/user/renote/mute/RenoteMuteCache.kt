package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RenoteMuteCache @Inject constructor() {

    private var map = mapOf<User.Id, RenoteMute>()
    private var notFounds = setOf<User.Id>()
    private var latestStateAccountIds = setOf<Long>()

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

    fun addAll(list: List<RenoteMute>, isAll: Boolean = false) {
        synchronized(this) {
            val mutableNotFounds = notFounds.toMutableSet()
            val cache = map.toMutableMap()
            for (el in list) {
                mutableNotFounds.remove(el.userId)
                cache[el.userId] = el
            }
            map = cache
            notFounds = mutableNotFounds

            if (isAll) {
                val accountIds = list.map {
                    it.userId.accountId
                }
                latestStateAccountIds = accountIds.toSet()
                notFounds = notFounds.filterNot {
                    latestStateAccountIds.contains(it.accountId)
                }.toSet()
            } else {
                val accountIds = list.map {
                    it.userId.accountId
                }
                latestStateAccountIds = latestStateAccountIds.filterNot {
                    accountIds.contains(it)
                }.toSet()
            }
        }
    }

    fun exists(userId: User.Id): Boolean {
        if (notFounds.contains(userId)) {
            return false
        }
        return map[userId] != null
    }

    fun isNotFound(userId: User.Id): Boolean {
        if (latestStateAccountIds.contains(userId.accountId)) {
            return !exists(userId)
        }
        return notFounds.contains(userId)
    }

    fun remove(userId: User.Id) {
        synchronized(this) {
            if (!latestStateAccountIds.contains(userId.accountId)) {
                notFounds = notFounds.toMutableSet().also {
                    it.add(userId)
                }
            }
            map = map.toMutableMap().also {
                it.remove(userId)
            }
        }
    }
}