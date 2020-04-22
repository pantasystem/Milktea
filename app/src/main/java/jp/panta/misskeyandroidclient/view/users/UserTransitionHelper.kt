package jp.panta.misskeyandroidclient.view.users

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.model.users.User

object UserTransitionHelper {

    @JvmStatic
    @BindingAdapter("transitionDestinationUser")
    fun View.showUserDetail(user: User?){
        user?: return
        this.setOnClickListener { view ->
            val context = view.context
            val intent = Intent(context, UserDetailActivity::class.java)
            intent.putExtra(UserDetailActivity.EXTRA_USER_ID, user.id)
            if(context is Activity){
                val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(context, view, "user")
                context.startActivity(intent, compat.toBundle())
            }else{
                context.startActivity(intent)
            }
        }
    }
}