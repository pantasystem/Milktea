package net.pantasystem.milktea.model.user.nickname


import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateNicknameUseCase @Inject constructor(
    val userDataSource: UserDataSource,
    val userNicknameRepository: UserNicknameRepository,
) : UseCase {

    suspend operator fun invoke(user: User, nickname: String): User {
        val id = UserNickname.Id(
            userName = user.userName,
            host = user.host
        )
        userNicknameRepository.save(
            UserNickname(
                id = id,
                name = nickname
            )
        )
        val existsUserName = userNicknameRepository.findOne(id)
        val updatedUser = when(user) {
            is User.Detail -> {
                user.copy(nickname = existsUserName)
            }
            is User.Simple -> {
                user.copy(nickname = existsUserName)
            }
        }
        userDataSource.add(
            updatedUser
        )
        return updatedUser
    }
}