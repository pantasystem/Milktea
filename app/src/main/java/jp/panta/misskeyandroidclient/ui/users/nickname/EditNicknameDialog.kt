package jp.panta.misskeyandroidclient.ui.users.nickname

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogEditNicknameBinding
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class EditNicknameDialog : AppCompatDialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.dialog_edit_nickname, null)
        val binding = DialogEditNicknameBinding.bind(view)

        val viewModel = ViewModelProvider(requireActivity())[UserDetailViewModel::class.java]
        dialog.setContentView(view)

        binding.sendButton.setOnClickListener {
            val text = binding.input.text?.toString() ?: ""
            viewModel.changeNickname(text)
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return dialog
    }
}