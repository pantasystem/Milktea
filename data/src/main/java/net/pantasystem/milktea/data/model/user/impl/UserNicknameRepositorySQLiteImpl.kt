package net.pantasystem.milktea.data.model.user.impl

import net.pantasystem.milktea.model.user.nickname.UserNickname
import net.pantasystem.milktea.model.user.nickname.UserNicknameNotFoundException
import net.pantasystem.milktea.model.user.nickname.UserNicknameRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject


class UserNicknameRepositorySQLiteImpl @Inject constructor(
    private val userNicknameDAO: UserNicknameDAO,
    private val userNicknameRepositoryOnMemoryImpl: UserNicknameRepositoryOnMemoryImpl,
) : net.pantasystem.milktea.model.user.nickname.UserNicknameRepository {

    /**
     * ニックネームは存在しない可能性が高い＆
     * 存在しないものを毎回探索しに行くのはパフォーマンス的によろしくないので
     * 存在しないものを記録するようにしている。
     */
    private val notExistsIds = mutableSetOf<net.pantasystem.milktea.model.user.nickname.UserNickname.Id>()
    private val lock = Mutex()

    override suspend fun findOne(id: net.pantasystem.milktea.model.user.nickname.UserNickname.Id): net.pantasystem.milktea.model.user.nickname.UserNickname {
        if (notExistsIds.contains(id)) {
            throw net.pantasystem.milktea.model.user.nickname.UserNicknameNotFoundException()
        }
        val inMem = runCatching {
            userNicknameRepositoryOnMemoryImpl.findOne(id)
        }.getOrNull()
        if (inMem != null) {
            return inMem
        }
        val result = userNicknameDAO.findByUserNameAndHost(id.userName, id.host)
        if (result != null) {
            return result.toUserNickname()
        }
        lock.withLock {
            notExistsIds.add(id)
        }
        throw net.pantasystem.milktea.model.user.nickname.UserNicknameNotFoundException()
    }

    override suspend fun save(nickname: net.pantasystem.milktea.model.user.nickname.UserNickname) {
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

    override suspend fun delete(id: net.pantasystem.milktea.model.user.nickname.UserNickname.Id) {
        lock.withLock {
            notExistsIds.add(id)
        }
        userNicknameDAO.delete(id.userName, id.host)
        userNicknameRepositoryOnMemoryImpl.delete(id)
    }
}