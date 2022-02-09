package jp.panta.misskeyandroidclient.model.users.impl

import jp.panta.misskeyandroidclient.model.users.nickname.UserNickname
import jp.panta.misskeyandroidclient.model.users.nickname.UserNicknameNotFoundException
import jp.panta.misskeyandroidclient.model.users.nickname.UserNicknameRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Singleton

@Singleton
class UserNicknameRepositoryOnMemoryImpl : UserNicknameRepository{

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