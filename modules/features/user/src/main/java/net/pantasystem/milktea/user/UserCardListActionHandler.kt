package net.pantasystem.milktea.user

import android.app.Activity
import net.pantasystem.milktea.common_android.ui.Activities
import net.pantasystem.milktea.common_android.ui.putActivity
import net.pantasystem.milktea.user.followlist.FollowFollowerActivity
import net.pantasystem.milktea.user.compose.UserDetailCardAction
import net.pantasystem.milktea.user.compose.UserDetailCardListAction
import net.pantasystem.milktea.user.profile.UserDetailActivity
import net.pantasystem.milktea.user.viewmodel.ToggleFollowViewModel

class UserCardListActionHandler(
    activity: Activity,
    toggleFollowViewModel: ToggleFollowViewModel,
    private val onRefresh: () -> Unit,
) {

    private val cardActionHandler = UserCardActionHandler(activity, toggleFollowViewModel)
    fun onAction(it: UserDetailCardListAction) {
        when (it) {
            is UserDetailCardListAction.CardAction -> {
                cardActionHandler.onAction(it.cardAction)
            }
            UserDetailCardListAction.Refresh -> {
                onRefresh()
            }
        }
    }

}

class UserCardActionHandler(
    private val activity: Activity,
    private val toggleFollowViewModel: ToggleFollowViewModel,
) {

    fun onAction(it: UserDetailCardAction) {
        when (it) {
            is UserDetailCardAction.FollowersCountClicked -> {
                activity.startActivity(
                    FollowFollowerActivity.newIntent(
                        activity,
                        userId = it.userId,
                        isFollowing = false,
                    )
                )
            }
            is UserDetailCardAction.FollowingsCountClicked -> {
                activity.startActivity(
                    FollowFollowerActivity.newIntent(
                        activity,
                        userId = it.userId,
                        isFollowing = true,
                    )
                )
            }
            is UserDetailCardAction.NotesCountClicked -> {
                val intent = UserDetailActivity.newInstance(
                    activity,
                    userId = it.userId
                )
                intent.putActivity(Activities.ACTIVITY_IN_APP)

                activity.startActivity(intent)
            }
            is UserDetailCardAction.OnCardClicked -> {
                val intent = UserDetailActivity.newInstance(
                    activity,
                    userId = it.userId
                )
                intent.putActivity(Activities.ACTIVITY_IN_APP)

                activity.startActivity(intent)
            }
            is UserDetailCardAction.ToggleFollow -> {
                toggleFollowViewModel.toggleFollow(it.userId)
            }
        }
    }
}