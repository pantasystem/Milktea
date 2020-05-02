package jp.panta.misskeyandroidclient.view.settings.page

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageSettingViewModel
import kotlinx.android.synthetic.main.dialog_edit_tab_name.view.*
import kotlinx.android.synthetic.main.item_detail_note.view.*

class EditTabNameDialog : AppCompatDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_edit_tab_name, null)
        dialog.setContentView(view)
        val pageSettingViewModel = ViewModelProvider(requireActivity())[PageSettingViewModel::class.java]

        val page = pageSettingViewModel.pageOnUpdateEvent.event
        if(page == null){
            dismiss()
            return dialog
        }
        view.editTabName.setText(page.title)

        view.okButton.setOnClickListener {
            val name = view.editTabName.text?.toString()
            if(name?.isNotBlank() == true){
                page.title = name
                pageSettingViewModel.updatePage(page)
            }
            dismiss()
        }
        view.cancelButton.setOnClickListener{
            dismiss()
        }
        return dialog
    }
}