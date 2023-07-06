package net.pantasystem.milktea.drive

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import net.pantasystem.milktea.drive.databinding.DialogCreateFolderBinding
import net.pantasystem.milktea.drive.viewmodel.DriveViewModel

class CreateFolderDialog : AppCompatDialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_create_folder, null)
        val binding = DialogCreateFolderBinding.bind(view)
        dialog.setContentView(view)
        val directoryViewModel = ViewModelProvider(requireActivity())[DriveViewModel::class.java]
        binding.okButton.setOnClickListener {
            val name = binding.editFolderName.text.toString()
            if(name.isNotBlank()){
                directoryViewModel.createDirectory(name)
                dismiss()

            }
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return dialog
    }
}