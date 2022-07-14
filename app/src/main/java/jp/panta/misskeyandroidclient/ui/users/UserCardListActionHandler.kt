package jp.panta.misskeyandroidclient.ui.users

import android.app.Activity
import jp.panta.misskeyandroidclient.Activities
import jp.panta.misskeyandroidclient.FollowFollowerActivity
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.putActivity
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ToggleFollowViewModel

class UserCardListActionHandler(
    private val activity: Activity,
    private val toggleFollowViewModel: ToggleFollowViewModel,
    private val onRefresh: () -> Unit,
) {

    fun onAction(it: UserDetailCardListAction) {
        when (it) {
            is UserDetailCardListAction.CardAction -> {
                when (it.cardAction) {
                    is UserDetailCardAction.FollowersCountClicked -> {
                        activity.startActivity(
                            FollowFollowerActivity.newIntent(
                                activity,
                                userId = it.cardAction.userId,
                                isFollowing = false,
                            )
                        )
                    }
                    is UserDetailCardAction.FollowingsCountClicked -> {
                        activity.startActivity(
                            FollowFollowerActivity.newIntent(
                                activity,
                                userId = it.cardAction.userId,
                                isFollowing = true,
                            )
                        )
                    }
                    is UserDetailCardAction.NotesCountClicked -> {
                        val intent = UserDetailActivity.newInstance(
                            activity,
                            userId = it.cardAction.userId
                        )
                        intent.putActivity(Activities.ACTIVITY_IN_APP)

                        activity.startActivity(intent)
                    }
                    is UserDetailCardAction.OnCardClicked -> {
                        val intent = UserDetailActivity.newInstance(
                            activity,
                            userId = it.cardAction.userId
                        )
                        intent.putActivity(Activities.ACTIVITY_IN_APP)

                        activity.startActivity(intent)
                    }
                    is UserDetailCardAction.ToggleFollow -> {
                        toggleFollowViewModel.toggleFollow(it.cardAction.userId)
                    }
                }
            }

            UserDetailCardListAction.Refresh -> {
                onRefresh()
            }
        }
    }

}