package net.pantasystem.milktea.note.editor.account

import android.app.Dialog
import android.os.Bundle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_android_ui.account.AccountSwitchingDialogLayout
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NoteEditorSwitchAccountDialog : BottomSheetDialogFragment() {
    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    lateinit var accountSettingNavigation: AccountSettingNavigation

    val viewModel: NoteEditorViewModel by activityViewModels()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            val view = ComposeView(requireContext()).apply {
                setContent {
                    MdcTheme {
                        val uiState by viewModel.accountUiState.collectAsState()
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
                                viewModel.setAccountIdAndSwitchCurrentAccount(it.account.accountId)
                                dismiss()
                            },
                            onAddAccountButtonClicked = {
                                requireActivity().startActivity(authorizationNavigation.newIntent(
                                    AuthorizationArgs.New))
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