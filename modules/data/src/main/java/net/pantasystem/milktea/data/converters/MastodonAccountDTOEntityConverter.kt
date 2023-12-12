package net.pantasystem.milktea.data.converters

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.instance.ticker.InstanceTickerRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MastodonAccountDTOEntityConverter @Inject constructor(
    private val instanceTickerRepository: InstanceTickerRepository,
    private val coroutineScope: CoroutineScope,
) {

    suspend fun convert(account: Account, dto: MastodonAccountDTO, related: User.Related? = null): User {
        val host = dto.acct.split("@").getOrNull(1) ?: account.getHost()
        val isSameHost = dto.acct.split("@").getOrNull(1) == null
                || dto.acct.split("@").getOrNull(1) == account.getHost()

        coroutineScope.launch {
            if (!isSameHost) {
                instanceTickerRepository.find(host)
            }
        }

        return User.Detail(
            User.Id(account.accountId, dto.id),
            userName = dto.username,
            name = dto.displayName,
            avatarUrl = dto.avatar,
            emojis = dto.emojis.map {
                CustomEmoji(
                    name = it.shortcode,
                    uri = it.url,
                    url = it.url,
                    category = it.category,
                )
            },
            host = host,
            isBot = dto.bot,
            isCat = false,
            nickname = null,
            isSameHost = isSameHost,
            instance = if (isSameHost) null else instanceTickerRepository.find(host).mapCancellableCatching {
                User.InstanceInfo(
                    name = it.name,
                    faviconUrl = it.faviconUrl,
                    iconUrl = it.iconUrl,
                    softwareName = it.softwareName,
                    softwareVersion = it.softwareVersion,
                    themeColor = it.themeColor,
                )
            }.getOrNull(),
            avatarBlurhash = null,
            info = User.Info(
                followersCount = dto.followersCount.toInt(),
                followingCount = dto.followingCount.toInt(),
                notesCount = dto.statusesCount.toInt(),
                hostLower = null,
                pinnedNoteIds = null,
                bannerUrl = dto.header,
                url = dto.url,
                isLocked = dto.locked,
                birthday = null,
                fields = emptyList(),
                createdAt = dto.createdAt,
                updatedAt = null,
                isPublicReactions = false,
                description = dto.note,
                ffVisibility = null,
            ),
            related = related,
            badgeRoles = emptyList(),
        )
    }
}