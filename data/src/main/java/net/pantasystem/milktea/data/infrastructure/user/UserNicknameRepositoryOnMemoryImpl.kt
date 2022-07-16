package net.pantasystem.milktea.data.infrastructure.user

import net.pantasystem.milktea.model.user.nickname.UserNickname
import net.pantasystem.milktea.model.user.nickname.UserNicknameNotFoundException
import net.pantasystem.milktea.model.user.nickname.UserNicknameRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Singleton

@Singleton
class UserNicknameRepositoryOnMemoryImpl :
    UserNicknameRepository {

    val lock = Mutex()
    val map = mutableMapOf<UserNickname.Id, UserNickname>()


    override suspend fun findOne(id: UserNickname.Id): UserNickname {
        return map[id]?: throw UserNicknameNotFoundException()
    }

    override suspend fun save(nickname: UserNickname) {
        lock.withLock {
            map[nickname.id] = nickname
        }
    }

    override suspend fun delete(id: UserNickname.Id) {
        lock.withLock {
            map.remove(id)
        }
    }
}