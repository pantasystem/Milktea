package net.pantasystem.milktea.common_android_ui

import android.app.Activity
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.internal.managers.FragmentComponentManager
import net.pantasystem.milktea.common_android.ui.Activities
import net.pantasystem.milktea.common_android.ui.haptic.HapticFeedbackController
import net.pantasystem.milktea.common_android.ui.putActivity
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.user.User

object UserTransitionHelper {

    @JvmStatic
    @BindingAdapter("transitionDestinationUser")
    fun View.showUserDetail(user: User?){
        user?: return
        this.setOnClickListener { view ->
            HapticFeedbackController.performClickHapticFeedback(view)
            val context = view.context


            val activity = FragmentComponentManager.findActivity(context) as Activity
            val intent = EntryPointAccessors.fromActivity(activity, NavigationEntryPointForBinding::class.java)
                .userDetailNavigation()
                .newIntent(UserDetailNavigationArgs.UserId(user.id))

            intent.putActivity(Activities.ACTIVITY_IN_APP)
            if(context is Activity){
                val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(context, view, "user")
                context.startActivity(intent, compat.toBundle())
            }else{
                context.startActivity(intent)
            }
        }
    }
}