package net.pantasystem.milktea.userlist

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import net.pantasystem.milktea.userlist.databinding.DialogUserListEditorBinding

class UserListEditorDialog : AppCompatDialogFragment(){

    companion object{
        const val FRAGMENT_TAG = "UserListEditorDialog"

        private const val EXTRA_MODE = "jp.panta.misskeyandroidclient.viewmodel.list.EXTRA_MODE"
        private const val EXTRA_LIST_ID = "jp.panta.misskeyandroidclient.viewmodel.list.EXTRA_LIST_ID"
        private const val EXTRA_LIST_NAME = "jp.panta.misskeyandroidclient.viewmodel.list.EXTRA_LIST_NAME"

        fun newInstance(): UserListEditorDialog {
            return UserListEditorDialog().apply{
                arguments = Bundle().apply{
                    putInt(EXTRA_MODE, Mode.CREATE.ordinal)
                }
            }
        }

        fun newInstance(listId: String, nowName: String): UserListEditorDialog {
            return UserListEditorDialog().apply{
                arguments = Bundle().apply{
                    putInt(EXTRA_MODE, Mode.UPDATE.ordinal)
                    putString(EXTRA_LIST_ID, listId)
                    putString(EXTRA_LIST_NAME, nowName)
                }
            }
        }
    }

    private enum class Mode{
        CREATE, UPDATE
    }

    interface OnSubmittedListener{
        fun onSubmit(name: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_user_list_editor, null)
        dialog.setContentView(view)
        val binding = DialogUserListEditorBinding.bind(view)

        val modeOrdinal = arguments?.getInt(EXTRA_MODE)?: 0

        val mode = Mode.values()[modeOrdinal]

        binding.titleView.text = when(mode){
            Mode.CREATE -> getString(R.string.create_user_list)
            Mode.UPDATE -> getString(R.string.update_user_list)
        }

        if(mode == Mode.UPDATE){
            binding.editListName.setText(
                arguments?.getString(EXTRA_LIST_NAME)?: ""
            )
        }

        binding.okButton.isEnabled = !binding.editListName.text.isNullOrBlank()

        binding.editListName.addTextChangedListener(object : TextWatcher{

            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.okButton.isEnabled = !s.isNullOrBlank()

            }
        })

        //val viewModel = ViewModelProvider(requireActivity(), UserListOperateViewModel.Factory(account, miCore))[UserListOperateViewModel::class.java]
        binding.okButton.setOnClickListener{
            val name = binding.editListName.text.toString()
            val context = requireContext()
            if(context is OnSubmittedListener){
                context.onSubmit(name)
            }else{
                Log.w("UserListEditor", "コールバックを発見することができませんでした。")
            }

            dismiss()
        }
        binding.cancelButton.setOnClickListener{
            dismiss()
        }
        return dialog
    }
}