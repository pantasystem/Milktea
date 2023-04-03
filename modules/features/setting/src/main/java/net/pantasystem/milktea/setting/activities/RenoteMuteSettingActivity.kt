package net.pantasystem.milktea.setting.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.setting.compose.renote.mute.RenoteMuteSettingScreen
import net.pantasystem.milktea.setting.viewmodel.RenoteMuteSettingViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RenoteMuteSettingActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    @Inject
    internal lateinit var userDetailNavigation: UserDetailNavigation

    private val viewModel: RenoteMuteSettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        setContent {
            MdcTheme {
                val uiState by viewModel.uiState.collectAsState()

                RenoteMuteSettingScreen(
                    uiState = uiState,
                    onRemoveRenoteMuteButtonClicked = viewModel::onRemoveRenoteMute,
                    onUserClicked = {
                        startActivity(
                            userDetailNavigation.newIntent(UserDetailNavigationArgs.UserId(it.id))
                        )
                    },
                    onRefresh = {
                        when(val account = uiState.currentAccount) {
                            null -> Unit
                            else -> {
                                viewModel.onRefresh(account)
                            }
                        }
                    },
                    onNavigateUp = {
                        finish()
                    }
                )
            }
        }
    }
}