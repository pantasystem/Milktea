@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.user.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.UserCardActionHandler
import net.pantasystem.milktea.user.compose.screen.FollowFollowerRoute
import net.pantasystem.milktea.user.viewmodel.FollowFollowerViewModel
import net.pantasystem.milktea.user.viewmodel.ToggleFollowViewModel
import net.pantasystem.milktea.user.viewmodel.UserDetailViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FollowFollowerActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_VIEW_CURRENT =
            "net.pantasystem.milktea.user.activity.FollowFollowerActivity.EXTRA_VIEW_CURRENT"
        private const val FOLLOWING_VIEW_MODE = 0
        private const val FOLLOWER_VIEW_MODE = 1

        fun newIntent(context: Context, userId: User.Id, isFollowing: Boolean): Intent {
            return Intent(context, FollowFollowerActivity::class.java).apply {
                putExtra(FollowFollowerViewModel.EXTRA_USER_ID, userId)
                putExtra(
                    EXTRA_VIEW_CURRENT,
                    if (isFollowing) FOLLOWING_VIEW_MODE else FOLLOWER_VIEW_MODE
                )
            }
        }
    }

    @Inject
    lateinit var assistedFactory: UserDetailViewModel.ViewModelAssistedFactory

    @Inject
    lateinit var applyTheme: ApplyTheme

    private val toggleFollowFollowerViewModel: ToggleFollowViewModel by viewModels()


    private val followFollowerViewModel by viewModels<FollowFollowerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        setContent {
            MdcTheme {
                FollowFollowerRoute(
                    followFollowerViewModel = followFollowerViewModel,
                    toggleFollowViewModel = toggleFollowFollowerViewModel,
                    onCardAction = {
                        UserCardActionHandler(this, toggleFollowFollowerViewModel).onAction(it)
                    },
                    onNavigateUp = {
                        finish()
                    },
                    initialTabIndex = intent.getIntExtra(EXTRA_VIEW_CURRENT, FOLLOWER_VIEW_MODE)
                )
            }
        }
    }
}
