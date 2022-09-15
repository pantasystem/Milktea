package net.pantasystem.milktea.setting

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.setting.databinding.DialogPageSettingActionBinding
import net.pantasystem.milktea.setting.viewmodel.page.PageSettingViewModel

@AndroidEntryPoint
class PageSettingActionDialog : BottomSheetDialogFragment(){

    private val viewModel: PageSettingViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_page_setting_action, null)
        dialog.setContentView(view)
        val binding = DataBindingUtil.bind<DialogPageSettingActionBinding>(view)


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