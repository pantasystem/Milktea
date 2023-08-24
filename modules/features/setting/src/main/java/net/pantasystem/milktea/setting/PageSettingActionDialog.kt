package net.pantasystem.milktea.setting

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.setting.databinding.DialogPageSettingActionBinding
import net.pantasystem.milktea.setting.viewmodel.page.PageSettingViewModel

@Suppress("DEPRECATION")
@AndroidEntryPoint
class PageSettingActionDialog : BottomSheetDialogFragment(){

    companion object {
        const val FRAGMENT_TAG = "PageSettingActionDialog"
        private const val PAGE = "PageSettingActionDialog.page"
        fun newInstance(page: Page): PageSettingActionDialog {
            return PageSettingActionDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(PAGE, page)
                }
            }
        }
    }

    private val viewModel: PageSettingViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_page_setting_action, null)
        dialog.setContentView(view)
        val binding = DataBindingUtil.bind<DialogPageSettingActionBinding>(view)


        val targetPage = requireArguments().getSerializable(PAGE) as? Page
        binding?.deletePage?.setOnClickListener {
            if(targetPage != null){
                viewModel.removePage(targetPage)
            }
            dismiss()
        }
        binding?.editPage?.setOnClickListener {
            if (targetPage != null) {
                viewModel.onEditButtonClicked(targetPage)
            }
            dismiss()
        }
        return dialog
    }
}