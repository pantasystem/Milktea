package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.api.misskey.clip.ClipDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.clip.Clip
import net.pantasystem.milktea.model.clip.ClipId
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipDTOEntityConverter @Inject constructor(
    val userDTOEntityConverter: UserDTOEntityConverter,
    val userDataSource: UserDataSource,
) {
    suspend fun convert(account: Account, clipDTO: ClipDTO): Clip {
        userDataSource.add(userDTOEntityConverter.convert(account, clipDTO.user))
        return Clip(
            id = ClipId(account.accountId, clipDTO.id),
            name = clipDTO.name,
            description = clipDTO.description,
            createdAt = clipDTO.createdAt,
            isPublic = clipDTO.isPublic,
            userId = User.Id(account.accountId, clipDTO.userId)
        )
    }
}