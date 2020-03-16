package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.users.FollowFollowerUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.SafeUnbox
import java.lang.IllegalArgumentException

class FollowFollowerViewData(
    val followFollowerUser: FollowFollowerUser
){
    val id = followFollowerUser.id

    val user = followFollowerUser.follower ?: followFollowerUser.followee
        ?: throw IllegalArgumentException("不正なパラメーターfollower, followee両方ともがnullです")

    var isFollowing = MutableLiveData<Boolean>(user.isFollowing)
    var isFollowed = MutableLiveData<Boolean>(user.isFollowed)

}