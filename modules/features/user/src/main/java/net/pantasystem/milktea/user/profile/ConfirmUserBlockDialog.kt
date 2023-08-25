package net.pantasystem.milktea.user.profile

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.profile.viewmodel.UserDetailViewModel

@AndroidEntryPoint
class ConfirmUserBlockDialog : AppCompatDialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "ConfirmUserBlockDialog"
    }

    val viewModel: UserDetailViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.confirm_user_block_title)
            setMessage(getString(R.string.confirm_user_block_description, viewModel.userState.value?.displayUserName))
            setPositiveButton(android.R.string.ok) { _ ,_ ->
                viewModel.block()
                dismiss()
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                dismiss()
            }
        }.create()
    }
}