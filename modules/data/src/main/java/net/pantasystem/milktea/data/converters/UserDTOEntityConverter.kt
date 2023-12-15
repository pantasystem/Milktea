package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatioDataSource
import net.pantasystem.milktea.model.emoji.CustomEmojiParser
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.EmojiResolvedType
import net.pantasystem.milktea.model.image.ImageCacheRepository
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDTOEntityConverter @Inject constructor(
    private val customEmojiRepository: CustomEmojiRepository,
    private val customEmojiAspectRatioDataSource: CustomEmojiAspectRatioDataSource,
    private val imageCacheRepository: ImageCacheRepository,
) {

    suspend fun convert(account: Account, userDTO: UserDTO, isDetail: Boolean = false, instanceEmojis: Map<String, CustomEmoji>? = null): User {
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

        val urls = userDTO.emojiList?.mapNotNull {
            it.url ?: it.uri
        } ?: emptyList()
        val aspects = customEmojiAspectRatioDataSource.findIn(urls).getOrElse {
            emptyList()
        }.associateBy {
            it.uri
        }
        val fileCaches = imageCacheRepository.findBySourceUrls(urls).getOrElse { emptyList() }.associateBy {
            it.sourceUrl
        }

        var emojis = userDTO.emojiList?.map {
            it.toModel(
                aspects[it.url ?: it.uri]?.aspectRatio,
                cachePath = fileCaches[it.url ?: it.uri]?.cachePath
            )
        } ?: emptyList()

        val parsedResult = CustomEmojiParser.parse(
            userDTO.host ?: account.getHost(),
            emojis,
            userDTO.name ?: userDTO.userName,
            instanceEmojis ?: customEmojiRepository.getAndConvertToMap(account.getHost()),
        )

        val resolvedEmojis = parsedResult.emojis.mapNotNull {
            (it.result as? EmojiResolvedType.Resolved)?.emoji
        }
        val finallyResolvedEmojis = if (instanceEmojis == null) {
            val tags = parsedResult.emojis.mapNotNull {
                it.result as? EmojiResolvedType.UnResolved
            }.map {
                it.tag
            }
            customEmojiRepository.findByNames(userDTO.host ?: account.getHost(), tags).getOrElse {
                emptyList()
            } + resolvedEmojis
        } else {
            resolvedEmojis
        }

        emojis = (emojis + finallyResolvedEmojis).distinctBy {
            it.name to it.host to it.url to it.uri
        }

        val badgeRoles = userDTO.badgeRoles?.mapIndexed { index, role ->
            User.BadgeRole(
                name = role.name,
                iconUri = role.iconUrl,
                displayOrder = role.displayOrder ?: ((userDTO.badgeRoles?.size ?: 0) - index)
            )
        }?.sortedByDescending {
            it.displayOrder
        } ?: emptyList()

        if (isDetail) {
            return User.Detail(
                id = User.Id(account.accountId, userDTO.id),
                avatarUrl = userDTO.avatarUrl,
                emojis = emojis,
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
                    } ?: emptyList(),
                    isPublicReactions = userDTO.publicReactions ?: false,
                    ffVisibility = when (userDTO.followFollowerVisibility) {
                        UserDTO.FollowFollowerVisibility.Public -> User.FollowerFollowerVisibility.Public
                        UserDTO.FollowFollowerVisibility.Followers -> User.FollowerFollowerVisibility.Followers
                        UserDTO.FollowFollowerVisibility.Private -> User.FollowerFollowerVisibility.Private
                        null -> null
                    },
                ),
                related = User.Related(
                    isFollowing = userDTO.isFollowing ?: false,
                    isFollower = userDTO.isFollowed ?: false,
                    isBlocking = userDTO.isBlocking ?: false,
                    isMuting = userDTO.isMuted ?: false,
                    hasPendingFollowRequestFromYou = userDTO.hasPendingFollowRequestFromYou
                        ?: false,
                    hasPendingFollowRequestToYou = userDTO.hasPendingFollowRequestToYou ?: false,
                    isNotify = userDTO.notifyState?.let {
                        userDTO.notifyState == "normal"
                    }
                ),
                badgeRoles = badgeRoles
            )
        } else {
            return User.Simple(
                id = User.Id(account.accountId, userDTO.id),
                avatarUrl = userDTO.avatarUrl,
                emojis = emojis,
                isBot = userDTO.isBot,
                isCat = userDTO.isCat,
                name = userDTO.name,
                userName = userDTO.userName,
                host = userDTO.host ?: account.getHost(),
                nickname = null,
                isSameHost = userDTO.host == null,
                instance = instanceInfo,
                avatarBlurhash = userDTO.avatarBlurhash,
                badgeRoles = badgeRoles
            )
        }
    }
}