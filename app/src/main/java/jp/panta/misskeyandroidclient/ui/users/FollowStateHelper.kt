package jp.panta.misskeyandroidclient.ui.users

import android.widget.Button
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.data.model.users.FollowState

object FollowStateHelper {

    @JvmStatic
    @BindingAdapter("isFollowing")
    fun Button.setFollowState(isFollowing: Boolean){
        this.text = if(isFollowing){
            context.getString(R.string.following)
        }else{
            context.getString(R.string.follow)
        }
    }

    @JvmStatic
    @BindingAdapter("followState")
    fun Button.setFollowState(state: FollowState?) {
        state?: return
        this.text = when(state) {
            FollowState.FOLLOWING -> {
                context.getString(R.string.unfollow)
            }
            FollowState.UNFOLLOWING -> {
                context.getString(R.string.follow)
            }
            FollowState.UNFOLLOWING_LOCKED -> {
                context.getString(R.string.request_follow_from_u)
            }
            FollowState.PENDING_FOLLOW_REQUEST -> {
                context.getString(R.string.follow_approval_pending)
            }
        }
    }
}