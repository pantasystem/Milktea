package jp.panta.misskeyandroidclient.ui.settings.page

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogEditTabNameBinding
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.page.PageSettingViewModel

@AndroidEntryPoint
class EditTabNameDialog : AppCompatDialogFragment(){

    private val pageSettingViewModel: PageSettingViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_edit_tab_name, null)
        val binding = DataBindingUtil.bind<DialogEditTabNameBinding>(view)
        requireNotNull(binding)
        dialog.setContentView(view)

        val page = pageSettingViewModel.pageOnUpdateEvent.event
        if(page == null){
            dismiss()
            return dialog
        }
        binding.editTabName.setText(page.title)

        binding.okButton.setOnClickListener {
            val name = binding.editTabName.text?.toString()
            if(name?.isNotBlank() == true){
                val updated = page.copy(title = name)
                pageSettingViewModel.updatePage(updated)
            }
            dismiss()
        }
        binding.cancelButton.setOnClickListener{
            dismiss()
        }
        return dialog
    }
}