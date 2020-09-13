package jp.panta.misskeyandroidclient.viewmodel.list

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.list.CreateList
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.android.synthetic.main.dialog_user_list_editor.view.*

class UserListEditorDialog : AppCompatDialogFragment(){

    companion object{
        private const val EXTRA_MODE = "jp.panta.misskeyandroidclient.viewmodel.list.EXTRA_MODE"
        private const val EXTRA_LIST_ID = "jp.panta.misskeyandroidclient.viewmodel.list.EXTRA_LIST_ID"
        private const val EXTRA_LIST_NAME = "jp.panta.misskeyandroidclient.viewmodel.list.EXTRA_LIST_NAME"

        fun newInstance(): UserListEditorDialog{
            return UserListEditorDialog().apply{
                arguments = Bundle().apply{
                    putInt(EXTRA_MODE, Mode.CREATE.ordinal)
                }
            }
        }

        fun newInstance(listId: String, nowName: String): UserListEditorDialog{
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_user_list_editor, null)
        dialog.setContentView(view)

        val modeOrdinal = arguments?.getInt(EXTRA_MODE)?: 0
        val listId = arguments?.getString(EXTRA_LIST_ID)

        val mode = Mode.values()[modeOrdinal]

        view.titleView.text = when(mode){
            Mode.CREATE -> getString(R.string.create_user_list)
            Mode.UPDATE -> getString(R.string.update_user_list)
        }

        if(mode == Mode.UPDATE){
            view.editListName.setText(
                arguments?.getString(EXTRA_LIST_NAME)?: ""
            )
        }

        val miCore = view.context.applicationContext as MiCore
        val account = miCore.getCurrentAccount().value!!

        val viewModel = ViewModelProvider(requireActivity(), UserListOperateViewModel.Factory(account, miCore))[UserListOperateViewModel::class.java]
        view.okButton.setOnClickListener{
            val name = view.editListName.text.toString()
            when(mode){
                Mode.CREATE ->{
                    viewModel.create(CreateList(
                        i = account.getI(miCore.getEncryption())!!,
                        name = name
                    ))
                }
                Mode.UPDATE ->{
                    viewModel.rename(
                        listId = listId!!,
                        name = name
                    )
                }
            }

            dismiss()
        }
        view.cancelButton.setOnClickListener{
            dismiss()
        }
        return dialog
    }
}