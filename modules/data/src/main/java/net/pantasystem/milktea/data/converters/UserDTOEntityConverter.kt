package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDTOEntityConverter @Inject constructor() {

    suspend fun convert(account: Account, userDTO: UserDTO, isDetail: Boolean = false): User {
        val instanceInfo = userDTO.instance?.let {
            User.InstanceInfo(
                name = it.name,
                faviconUrl = it.faviconUrl,
                iconUrl = it.iconUrl,
                softwareName = it.softwareName,
                softwareVersion = it.softwareVersion,
                themeColor = it.themeColor
            )
        }
        if (isDetail) {
            return User.Detail(
                id = User.Id(account.accountId, userDTO.id),
                avatarUrl = userDTO.avatarUrl,
                emojis = userDTO.emojiList?.map {
                    it.toModel()
                } ?: emptyList(),
                isBot = userDTO.isBot,
                isCat = userDTO.isCat,
                name = userDTO.name,
                userName = userDTO.userName,
                host = userDTO.host ?: account.getHost(),
                nickname = null,
                isSameHost = userDTO.host == null,
                instance = instanceInfo,
                avatarBlurhash = userDTO.avatarBlurhash,
                info = User.Info(
                    bannerUrl = userDTO.bannerUrl,
                    description = userDTO.description,
                    followersCount = userDTO.followersCount,
                    followingCount = userDTO.followingCount,
                    url = userDTO.url,
                    hostLower = userDTO.hostLower,
                    notesCount = userDTO.notesCount,
                    pinnedNoteIds = userDTO.pinnedNoteIds?.map {
                        Note.Id(account.accountId, it)
                    },
                    isLocked = userDTO.isLocked ?: false,
                    birthday = userDTO.birthday,
                    createdAt = userDTO.createdAt,
                    updatedAt = userDTO.updatedAt,
                    fields = userDTO.fields?.map {
                        User.Field(it.name, it.value)
                    }?: emptyList(),
                    isPublicReactions = userDTO.publicReactions ?: false,
                ),
                related = User.Related(
                    isFollowing = userDTO.isFollowing ?: false,
                    isFollower = userDTO.isFollowed ?: false,
                    isBlocking = userDTO.isBlocking ?: false,
                    isMuting = userDTO.isMuted ?: false,
                    hasPendingFollowRequestFromYou = userDTO.hasPendingFollowRequestFromYou ?: false,
                    hasPendingFollowRequestToYou = userDTO.hasPendingFollowRequestToYou ?: false,
                )
            )
        } else {
            return User.Simple(
                id = User.Id(account.accountId, userDTO.id),
                avatarUrl = userDTO.avatarUrl,
                emojis = userDTO.emojiList?.map { it.toModel() } ?: emptyList(),
                isBot = userDTO.isBot,
                isCat = userDTO.isCat,
                name = userDTO.name,
                userName = userDTO.userName,
                host = userDTO.host ?: account.getHost(),
                nickname = null,
                isSameHost = userDTO.host == null,
                instance = instanceInfo,
                avatarBlurhash = userDTO.avatarBlurhash
            )
        }
    }
}