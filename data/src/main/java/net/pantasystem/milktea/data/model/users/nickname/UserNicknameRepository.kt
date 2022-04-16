package net.pantasystem.milktea.data.model.users.nickname


interface UserNicknameRepository {

    suspend fun save(nickname: UserNickname)

    suspend fun findOne(id: UserNickname.Id): UserNickname

    suspend fun delete(id: UserNickname.Id)

}