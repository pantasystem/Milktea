package jp.panta.misskeyandroidclient.ui.main

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common_android.ui.Activities
import net.pantasystem.milktea.common_android.ui.putActivity
import net.pantasystem.milktea.common_android_ui.account.AccountSwitchingDialog
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.followlist.FollowFollowerActivity
import net.pantasystem.milktea.user.profile.UserDetailActivity

class AccountViewModelHandler(
    val binding: ActivityMainBinding,
    val activity: AppCompatActivity,
    val accountViewModel: AccountViewModel,
) {

    fun setup() {
        initAccountViewModelListener()
    }


    private fun initAccountViewModelListener() {
        accountViewModel.switchAccountEvent.onEach {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val dialog = AccountSwitchingDialog()
            dialog.show(activity.supportFragmentManager, AccountSwitchingDialog.FRAGMENT_TAG)
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED).launchIn(activity.lifecycleScope)

        accountViewModel.showFollowersEvent.onEach {
            binding.drawerLayout.closeDrawerWhenOpened()
            val intent = FollowFollowerActivity.newIntent(activity, it, false)
            activity.startActivity(intent)
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED).launchIn(activity.lifecycleScope)

        accountViewModel.showFollowingsEvent.onEach {
            binding.drawerLayout.closeDrawerWhenOpened()
            val intent = FollowFollowerActivity.newIntent(activity, it, true)
            activity.startActivity(intent)
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED).launchIn(activity.lifecycleScope)

        accountViewModel.showProfileEvent.onEach {
            binding.drawerLayout.closeDrawerWhenOpened()
            val intent =
                UserDetailActivity.newInstance(activity, userId = User.Id(it.accountId, it.remoteId))
            intent.putActivity(Activities.ACTIVITY_IN_APP)
            activity.startActivity(intent)
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED).launchIn(activity.lifecycleScope)

    }

    @MainThread
    private fun DrawerLayout.closeDrawerWhenOpened() {
        if (this.isDrawerOpen(GravityCompat.START)) {
            this.closeDrawer(GravityCompat.START)
        }
    }
}