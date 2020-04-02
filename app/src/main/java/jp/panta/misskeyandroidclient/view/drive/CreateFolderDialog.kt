package jp.panta.misskeyandroidclient.view.drive

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewModel
import kotlinx.android.synthetic.main.dialog_create_folder.view.*

class CreateFolderDialog : AppCompatDialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_create_folder, null)
        dialog.setContentView(view)
        val folderViewModel = ViewModelProvider(activity!!)[FolderViewModel::class.java]
        view.okButton.setOnClickListener {
            val name = view.editFolderName.text.toString()
            if(name.isNotBlank()){
                folderViewModel.createFolder(name)
                dismiss()

            }
        }
        view.cancelButton.setOnClickListener {
            dismiss()
        }
        return dialog
    }
}