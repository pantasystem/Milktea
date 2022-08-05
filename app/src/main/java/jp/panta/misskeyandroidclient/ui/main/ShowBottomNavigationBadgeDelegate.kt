package jp.panta.misskeyandroidclient.ui.main

import com.google.android.material.bottomnavigation.BottomNavigationView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainUiState


internal class ShowBottomNavigationBadgeDelegate(
    private val bottomNavigationView: BottomNavigationView
) {
    operator fun invoke(state: MainUiState) {
        if (state.unreadNotificationCount <= 0) {
            bottomNavigationView.getBadge(R.id.navigation_notification)
                ?.clearNumber()
        }
        if (state.unreadMessagesCount <= 0) {
            bottomNavigationView.getBadge(R.id.navigation_message_list)?.clearNumber()
        }
        bottomNavigationView.getOrCreateBadge(R.id.navigation_notification)
            .apply {
                isVisible = state.unreadNotificationCount > 0
                number = state.unreadNotificationCount
            }
        bottomNavigationView.getOrCreateBadge(R.id.navigation_message_list).apply {
            isVisible = state.unreadMessagesCount > 0
            number = state.unreadMessagesCount
        }
    }
}