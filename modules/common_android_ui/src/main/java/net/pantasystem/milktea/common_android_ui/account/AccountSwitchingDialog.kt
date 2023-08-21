package net.pantasystem.milktea.common_android_ui.account

import android.app.Dialog
import android.os.Bundle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.AccountSettingNavigation
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

@AndroidEntryPoint
class AccountSwitchingDialog : BottomSheetDialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "AccountSwitchingDialog"
    }

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    lateinit var accountSettingNavigation: AccountSettingNavigation

    @Inject
    lateinit var configRepository: LocalConfigRepository

    val viewModel: AccountViewModel by activityViewModels()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            val view = ComposeView(requireContext()).apply {
                setContent {
                    MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                        val uiState by viewModel.uiState.collectAsState()
                        AccountSwitchingDialogLayout(
                            uiState = uiState,
                            onSettingButtonClicked = {
                                startActivity(accountSettingNavigation.newIntent(Unit))
                                dismiss()
                            },
                            onAvatarIconClicked = { accountInfo ->
                                startActivity(
                                    userDetailNavigation.newIntent(UserDetailNavigationArgs.UserName(accountInfo.user?.let {
                                        "@${it.userName}@${it.host}"
                                    } ?: "@${accountInfo.account.userName}@${accountInfo.account.getHost()}"))
                                )
                                dismiss()
                            },
                            onAccountClicked = {
                                viewModel.setSwitchTargetConnectionInstance(it.account)
                                dismiss()
                            },
                            onAddAccountButtonClicked = {
                                requireActivity().startActivity(authorizationNavigation.newIntent(AuthorizationArgs.New))
                                dismiss()
                            }
                        )
                    }
                }
            }
            setContentView(view)
        }
    }



}

