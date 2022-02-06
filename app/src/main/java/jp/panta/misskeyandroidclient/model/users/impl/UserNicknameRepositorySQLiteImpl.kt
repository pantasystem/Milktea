package jp.panta.misskeyandroidclient.model.users.impl

import jp.panta.misskeyandroidclient.model.users.nickname.UserNickname
import jp.panta.misskeyandroidclient.model.users.nickname.UserNicknameNotFoundException
import jp.panta.misskeyandroidclient.model.users.nickname.UserNicknameRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject


class UserNicknameRepositorySQLiteImpl @Inject constructor(
    private val userNicknameDAO: UserNicknameDAO,
    private val userNicknameRepositoryOnMemoryImpl: UserNicknameRepositoryOnMemoryImpl,
) : UserNicknameRepository {

    /**
     * ニックネームは存在しない可能性が高い＆
     * 存在しないものを毎回探索しに行くのはパフォーマンス的によろしくないので
     * 存在しないものを記録するようにしている。
     */
    private val notExistsIds = mutableSetOf<UserNickname.Id>()
    private val lock = Mutex()

    override suspend fun findOne(id: UserNickname.Id): UserNickname {
        if (notExistsIds.contains(id)) {
            throw UserNicknameNotFoundException()
        }
        val inMem = runCatching {
            userNicknameRepositoryOnMemoryImpl.findOne(id)
        }.getOrNull()
        if (inMem != null) {
            return inMem
        }
        val result = userNicknameDAO.findByUserNameAndHost(id.userName, id.host)
        lock.withLock {
            notExistsIds.add(id)
        }
        result?: throw UserNicknameNotFoundException()
        return result.toUserNickname()
    }

    override suspend fun save(nickname: UserNickname) {
        val found = userNicknameDAO.findByUserNameAndHost(nickname.id.userName, nickname.id.host)
        val dto = UserNicknameDTO(
            userName = nickname.id.userName,
            host = nickname.id.host,
            nickname = nickname.name,
            id = found?.id ?: 0L,
        )
        lock.withLock {
            notExistsIds.remove(nickname.id)
        }
        userNicknameRepositoryOnMemoryImpl.save(nickname)
        if (found == null) {
            userNicknameDAO.create(dto)
        } else {
            userNicknameDAO.update(dto)
        }
    }
}