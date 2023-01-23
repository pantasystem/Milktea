package net.pantasystem.milktea.model.user.nickname


interface UserNicknameRepository {

    suspend fun save(nickname: UserNickname)

    suspend fun findOne(id: UserNickname.Id): UserNickname

    suspend fun delete(id: UserNickname.Id)

}