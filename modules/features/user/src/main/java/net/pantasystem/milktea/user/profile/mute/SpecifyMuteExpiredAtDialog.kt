package net.pantasystem.milktea.user.profile.mute

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.viewmodel.MuteUserViewModel
import net.pantasystem.milktea.user.viewmodel.UserDetailViewModel

@AndroidEntryPoint
class SpecifyMuteExpiredAtDialog : AppCompatDialogFragment() {

    val userDetailViewModel by activityViewModels<UserDetailViewModel>()
    val viewModel by activityViewModels<MuteUserViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.mute)
            .setView(ComposeView(requireContext()).apply {
                setContent {
                    MdcTheme {
                        SpecifyMuteExpiredAtDialogContent(
                            state = viewModel.state,
                            onAction = { action ->
                                when(action) {
                                    is SpecifyMuteUserAction.OnChangeState -> {
                                        viewModel.onUpdateState(action.state)
                                    }
                                    SpecifyMuteUserAction.OnDateChangeButtonClicked -> {

                                    }
                                    SpecifyMuteUserAction.OnTimeChangeButtonClicked -> {

                                    }
                                }
                            }
                        )
                    }
                }
            })
            .setPositiveButton(android.R.string.ok) { _, _ ->

            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->

            }
            .create()
    }
}
