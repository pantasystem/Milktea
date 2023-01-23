package net.pantasystem.milktea.setting.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.common_navigation.AccountSettingNavigation
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.setting.compose.account.AccountSettingScreen
import javax.inject.Inject

@AndroidEntryPoint
class AccountSettingActivity : AppCompatActivity() {

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    val viewModel: AccountViewModel by viewModels()

    @Inject
    lateinit var applyTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            MdcTheme {
                AccountSettingScreen(
                    uiState = uiState,
                    onAccountClicked = {
                        viewModel.setSwitchTargetConnectionInstance(it.account)
                    },
                    onAddAccountButtonClicked = {
                        startActivity(authorizationNavigation.newIntent(AuthorizationArgs.New))
                    },
                    onNavigateUp = { finish() },
                    onShowUser = {
                        startActivity(userDetailNavigation.newIntent(it))
                    },
                    onSignOutButtonClicked = {
                        viewModel.signOut(it.account)
                    }
                )
            }
        }

    }
}


class AccountSettingActivityNavigationImpl @Inject constructor(
    val activity: Activity
) : AccountSettingNavigation {
    override fun newIntent(args: Unit): Intent {
        return Intent(activity, AccountSettingActivity::class.java)
    }
}