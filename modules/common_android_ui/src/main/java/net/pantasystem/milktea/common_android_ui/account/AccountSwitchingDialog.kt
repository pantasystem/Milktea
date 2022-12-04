package net.pantasystem.milktea.common_android_ui.account

import android.app.Dialog
import android.os.Bundle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import javax.inject.Inject

@AndroidEntryPoint
class AccountSwitchingDialog : BottomSheetDialogFragment() {

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    val viewModel: AccountViewModel by activityViewModels()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            val view = ComposeView(requireContext()).apply {
                setContent {
                    MdcTheme {
                        val uiState by viewModel.uiState.collectAsState()
                        AccountSwitchingDialogLayout(
                            uiState = uiState,
                            onSettingButtonClicked = {
                                // TODO: アカウント設定画面を作成してそこに遷移するようにする
                                dismiss()
                            },
                            onAvatarIconClicked = {
                                viewModel.showProfile(it.account)
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

