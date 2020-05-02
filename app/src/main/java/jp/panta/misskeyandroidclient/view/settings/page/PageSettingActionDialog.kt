package jp.panta.misskeyandroidclient.view.settings.page

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogPageSettingActionBinding
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageSettingViewModel

class PageSettingActionDialog : BottomSheetDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_page_setting_action, null)
        dialog.setContentView(view)
        val binding = DataBindingUtil.bind<DialogPageSettingActionBinding>(view)

        val viewModel = ViewModelProvider(requireActivity())[PageSettingViewModel::class.java]
        val targetPage = viewModel.pageOnActionEvent.event
        binding?.deletePage?.setOnClickListener {
            if(targetPage != null){
                viewModel.removePage(targetPage)
            }
            dismiss()
        }
        binding?.editPage?.setOnClickListener {
            viewModel.pageOnUpdateEvent.event = targetPage
            dismiss()
        }
        return dialog
    }
}