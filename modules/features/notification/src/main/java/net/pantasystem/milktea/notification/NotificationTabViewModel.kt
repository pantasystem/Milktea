package net.pantasystem.milktea.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import javax.inject.Inject

@HiltViewModel
class NotificationTabViewModel @Inject constructor(
    val accountStore: AccountStore,
) : ViewModel() {

    val tabs = accountStore.observeCurrentAccount.filterNotNull().map {
        when(it.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                listOf(
                    NotificationTabType.TitleWithPageable(
                        StringSource(net.pantasystem.milktea.common_android_ui.R.string.notification),
                        Pageable.Notification(),
                    ),
                    NotificationTabType.TitleWithPageable(
                        StringSource(net.pantasystem.milktea.common_android_ui.R.string.mention),
                        Pageable.Mention(following = null),
                    ),
                    NotificationTabType.FollowRequests(
                        StringSource(R.string.notifications_follow_requests)
                    )
                )
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA  -> {
                listOf(
                    NotificationTabType.TitleWithPageable(
                        StringSource(net.pantasystem.milktea.common_android_ui.R.string.notification),
                        Pageable.Notification(),
                    ),
                    NotificationTabType.TitleWithPageable(
                        StringSource(net.pantasystem.milktea.common_android_ui.R.string.mention),
                        Pageable.Mastodon.Mention,
                    ),
                    NotificationTabType.FollowRequests(
                        StringSource(R.string.notifications_follow_requests)
                    )
                )
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
}

sealed interface NotificationTabType {
    val title: StringSource
    data class FollowRequests(override val title: StringSource) : NotificationTabType
    data class TitleWithPageable(override val title: StringSource, val pageable: Pageable) : NotificationTabType
}