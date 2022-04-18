package net.pantasystem.milktea.data.model.user.impl

import net.pantasystem.milktea.model.user.nickname.UserNickname
import net.pantasystem.milktea.model.user.nickname.UserNicknameNotFoundException
import net.pantasystem.milktea.model.user.nickname.UserNicknameRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Singleton

@Singleton
class UserNicknameRepositoryOnMemoryImpl :
    net.pantasystem.milktea.model.user.nickname.UserNicknameRepository {

    val lock = Mutex()
    val map = mutableMapOf<net.pantasystem.milktea.model.user.nickname.UserNickname.Id, net.pantasystem.milktea.model.user.nickname.UserNickname>()


    override suspend fun findOne(id: net.pantasystem.milktea.model.user.nickname.UserNickname.Id): net.pantasystem.milktea.model.user.nickname.UserNickname {
        return map[id]?: throw net.pantasystem.milktea.model.user.nickname.UserNicknameNotFoundException()
    }

    override suspend fun save(nickname: net.pantasystem.milktea.model.user.nickname.UserNickname) {
        lock.withLock {
            map[nickname.id] = nickname
        }
    }

    override suspend fun delete(id: net.pantasystem.milktea.model.user.nickname.UserNickname.Id) {
        lock.withLock {
            map.remove(id)
        }
    }
}