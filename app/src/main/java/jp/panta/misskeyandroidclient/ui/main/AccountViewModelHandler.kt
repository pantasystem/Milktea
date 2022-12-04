package jp.panta.misskeyandroidclient.ui.main

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import net.pantasystem.milktea.user.activity.FollowFollowerActivity
import net.pantasystem.milktea.user.activity.UserDetailActivity
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.common_android.ui.Activities
import net.pantasystem.milktea.common_android.ui.putActivity
import net.pantasystem.milktea.common_android_ui.account.AccountSwitchingDialog
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User

class AccountViewModelHandler(
    val binding: ActivityMainBinding,
    val activity: AppCompatActivity,
    val accountViewModel: AccountViewModel,
) {

    fun setup() {
        initAccountViewModelListener()
    }

    private val switchAccountButtonObserver = Observer<Int> {
        activity.runOnUiThread {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val dialog = AccountSwitchingDialog()
            dialog.show(activity.supportFragmentManager, "mainActivity")
        }
    }


    private val showFollowingsObserver = Observer<User.Id> {
        binding.drawerLayout.closeDrawerWhenOpened()
        val intent = FollowFollowerActivity.newIntent(activity, it, true)
        activity.startActivity(intent)
    }

    private val showFollowersObserver = Observer<User.Id> {
        binding.drawerLayout.closeDrawerWhenOpened()
        val intent = FollowFollowerActivity.newIntent(activity, it, false)
        activity.startActivity(intent)
    }

    @ExperimentalCoroutinesApi
    private val showProfileObserver = Observer<Account> {
        binding.drawerLayout.closeDrawerWhenOpened()
        val intent =
            UserDetailActivity.newInstance(activity, userId = User.Id(it.accountId, it.remoteId))
        intent.putActivity(Activities.ACTIVITY_IN_APP)
        activity.startActivity(intent)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initAccountViewModelListener() {
        accountViewModel.switchAccount.removeObserver(switchAccountButtonObserver)
        accountViewModel.switchAccount.observe(activity, switchAccountButtonObserver)
        accountViewModel.showFollowings.observe(activity, showFollowingsObserver)
        accountViewModel.showFollowers.observe(activity, showFollowersObserver)
        accountViewModel.showProfile.observe(activity, showProfileObserver)
    }

    @MainThread
    private fun DrawerLayout.closeDrawerWhenOpened() {
        if (this.isDrawerOpen(GravityCompat.START)) {
            this.closeDrawer(GravityCompat.START)
        }
    }
}