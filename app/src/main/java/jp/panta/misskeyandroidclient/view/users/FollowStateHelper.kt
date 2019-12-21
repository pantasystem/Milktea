package jp.panta.misskeyandroidclient.view.users

import android.widget.Button
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R

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
}