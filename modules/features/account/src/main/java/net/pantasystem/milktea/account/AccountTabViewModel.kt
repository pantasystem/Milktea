package net.pantasystem.milktea.account

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

@HiltViewModel
class AccountTabViewModel @Inject constructor(
    accountStore: AccountStore,
    private val featureEnables: FeatureEnables,
) : ViewModel() {

    val tabs = accountStore.observeCurrentAccount.filterNotNull().map { account ->
        val isEnableGallery =
            featureEnables.isEnable(account.normalizedInstanceDomain, FeatureType.Gallery)
        val userId = User.Id(account.accountId, account.remoteId)
        val isEnableMessaging =
            featureEnables.isEnable(account.normalizedInstanceDomain, FeatureType.Messaging, false)
        when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                listOfNotNull(
                    AccountTabTypes.Account,
                    if (isEnableMessaging) AccountTabTypes.Message else null,
                    AccountTabTypes.UserTimeline(userId),
                    AccountTabTypes.UserTimelineWithReplies(userId),
                    AccountTabTypes.PinNote(userId),
                    AccountTabTypes.Media(userId),
                    if (isEnableGallery) AccountTabTypes.Gallery(
                        userId, accountId = account.accountId
                    ) else null,
                    AccountTabTypes.Reactions(userId),
                )
            }
            Account.InstanceType.MASTODON -> {
                listOf(
                    AccountTabTypes.MastodonUserTimeline(userId),
                    AccountTabTypes.MastodonUserTimelineWithReplies(userId),
                    AccountTabTypes.MastodonMedia(userId)
                )
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        listOf(AccountTabTypes.Account)
    )

}

sealed class AccountTabTypes(
    @StringRes val title: Int
) {

    object Account : AccountTabTypes(R.string.account)
    object Message : AccountTabTypes(R.string.message)

    data class UserTimeline(val userId: User.Id) : AccountTabTypes(R.string.post)
    data class UserTimelineWithReplies(val userId: User.Id) :
        AccountTabTypes(R.string.notes_and_replies)

    data class PinNote(val userId: User.Id) : AccountTabTypes(R.string.pin)
    data class Gallery(val userId: User.Id, val accountId: Long) :
        AccountTabTypes(R.string.gallery)

    data class Reactions(val userId: User.Id) : AccountTabTypes(R.string.reaction)
    data class Media(val userId: User.Id) : AccountTabTypes(R.string.media)

    data class MastodonUserTimeline(val userId: User.Id) : AccountTabTypes(R.string.post)
    data class MastodonUserTimelineWithReplies(val userId: User.Id) :
        AccountTabTypes(R.string.notes_and_replies)

    data class MastodonMedia(val userId: User.Id) : AccountTabTypes(R.string.media)
}