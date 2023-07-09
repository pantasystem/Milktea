package net.pantasystem.milktea.setting

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.account.page.CanOnlyMedia
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.UntilPaginate
import net.pantasystem.milktea.setting.databinding.DialogEditTabNameBinding
import net.pantasystem.milktea.setting.viewmodel.page.PageSettingViewModel

@AndroidEntryPoint
class EditTabSettingDialog : AppCompatDialogFragment(){

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

         when(val pageable = page.pageable()) {
            is CanOnlyMedia<*> -> {
                binding.toggleOnlyMedia.isVisible = true
                binding.toggleOnlyMedia.isChecked = pageable.getOnlyMedia()
            }
            else -> {
                binding.toggleOnlyMedia.isVisible = false
            }
        }

        if (page.pageable() is UntilPaginate) {
            binding.toggleSavePagePosition.isVisible = true
            binding.toggleSavePagePosition.isChecked = page.isSavePagePosition
        } else {
            binding.toggleSavePagePosition.isVisible = false
        }


        binding.okButton.setOnClickListener {
            val name = binding.editTabName.text?.toString()
            if(name?.isNotBlank() == true){
                var target = page.copy(
                    isSavePagePosition = binding.toggleSavePagePosition.isChecked
                )
                when(val pageable = target.pageable()) {
                    is CanOnlyMedia<*> -> {
                        target = target.copy(
                            pageParams = (pageable.setOnlyMedia(binding.toggleOnlyMedia.isChecked) as Pageable).toParams()
                        )
                    }
                    else -> Unit
                }
                val updated = target.copy(title = name)
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