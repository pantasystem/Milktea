package net.pantasystem.milktea.model.user.renote.mute

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.user.User

interface RenoteMuteRepository {

    /**
     * アカウント単位で内部の状態とリモートの状態の同期を行う
     * APIがリノートのミュートに対応していない場合はスキップされる
     */
    suspend fun syncBy(accountId: Long): Result<Unit>

    /**
     * ユーザ単位で内部の状態とリモートの状態の同期を行う
     * APIがリノートのミュートに対応していない場合はスキップされる
     */
    suspend fun syncBy(userId: User.Id): Result<Unit>

    suspend fun findBy(accountId: Long): Result<List<RenoteMute>>

    suspend fun findOne(userId: User.Id): Result<RenoteMute>

    suspend fun delete(userId: User.Id): Result<Unit>

    suspend fun create(userId: User.Id): Result<RenoteMute>

    suspend fun exists(userId: User.Id): Result<Boolean>

    fun observeBy(accountId: Long): Flow<List<RenoteMute>>

}