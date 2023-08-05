package net.pantasystem.milktea.user.profile.viewmodel

import androidx.annotation.StringRes
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.R
import javax.inject.Inject


sealed class UserDetailTabType(
    @StringRes val title: Int,
) {

    data class UserTimeline(val userId: User.Id) : UserDetailTabType(R.string.post)
    data class UserTimelineWithReplies(val userId: User.Id) :
        UserDetailTabType(R.string.notes_and_replies)

    data class UserTimelineOnlyPosts(val userId: User.Id) : UserDetailTabType(R.string.post_only)

    data class PinNote(val userId: User.Id) : UserDetailTabType(R.string.pin)
    data class Gallery(val userId: User.Id, val accountId: Long) :
        UserDetailTabType(R.string.gallery)

    data class Reactions(val userId: User.Id) : UserDetailTabType(R.string.reaction)
    data class Media(val userId: User.Id) : UserDetailTabType(R.string.media)

    data class MastodonUserTimeline(val userId: User.Id) : UserDetailTabType(R.string.post)
    data class MastodonUserTimelineWithReplies(val userId: User.Id) :
        UserDetailTabType(R.string.notes_and_replies)

    data class MastodonUserTimelineOnlyPosts(val userId: User.Id) :
        UserDetailTabType(R.string.post_only)

    data class MastodonMedia(val userId: User.Id) : UserDetailTabType(R.string.media)
}


class UserDetailTabTypeFactory @Inject constructor(
    private val featureEnables: FeatureEnables,
) {


    suspend fun createTabsForInstanceType(account: Account, user: User.Detail): List<UserDetailTabType> {
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> createMisskeyTabs(account, user)
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> createMastodonPleromaTabs(user)
        }
    }

    private suspend fun createMisskeyTabs(account: Account, user: User.Detail): List<UserDetailTabType> {
        return listOfNotNull(
            UserDetailTabType.UserTimeline(user.id),
            UserDetailTabType.UserTimelineOnlyPosts(user.id),
            UserDetailTabType.UserTimelineWithReplies(user.id),
            UserDetailTabType.PinNote(user.id),
            UserDetailTabType.Media(user.id),
            createGalleryTabIfEnabled(account, user),
            createReactionTabIfPublic(account, user)
        )
    }

    private fun createMastodonPleromaTabs(user: User): List<UserDetailTabType> {
        return listOf(
            UserDetailTabType.MastodonUserTimeline(user.id),
            UserDetailTabType.MastodonUserTimelineOnlyPosts(user.id),
            UserDetailTabType.MastodonUserTimelineWithReplies(user.id),
            UserDetailTabType.MastodonMedia(user.id)
        )
    }

    private suspend fun createGalleryTabIfEnabled(account: Account, user: User.Detail): UserDetailTabType? {
        return if (isGalleryEnabled(account)) {
            UserDetailTabType.Gallery(user.id, accountId = account.accountId)
        } else null
    }

    private suspend fun createReactionTabIfPublic(account: Account, user: User.Detail): UserDetailTabType? {
        return if (isReactionPublic(account, user)) {
            UserDetailTabType.Reactions(user.id)
        } else null
    }

    private suspend fun isGalleryEnabled(account: Account): Boolean {
        return featureEnables.isEnable(account.normalizedInstanceUri, FeatureType.Gallery)
    }

    private suspend fun isReactionPublic(account: Account, user: User.Detail): Boolean {
        return featureEnables.isEnable(account.normalizedInstanceUri, FeatureType.UserReactionHistory) && (user.info.isPublicReactions || user.id == User.Id(account.accountId, account.remoteId))
    }
}

