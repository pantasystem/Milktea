package net.pantasystem.milktea.common_android_ui

import android.view.View
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.model.user.User

object UserTransitionHelper {

    @JvmStatic
    @BindingAdapter("transitionDestinationUser")
    fun View.showUserDetail(user: User?){
        user?: return
        this.setOnClickListener { view ->
            val context = view.context

            // TODO: 修正する
//            val intent = UserDetailActivity.newInstance(context, userId = user.id)
//            intent.putActivity(Activities.ACTIVITY_IN_APP)
//
//
//            if(context is Activity){
//                val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(context, view, "user")
//                context.startActivity(intent, compat.toBundle())
//            }else{
//                context.startActivity(intent)
//            }
        }
    }
}