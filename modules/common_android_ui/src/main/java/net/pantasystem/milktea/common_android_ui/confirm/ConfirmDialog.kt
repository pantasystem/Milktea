package net.pantasystem.milktea.common_android_ui.confirm

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.model.confirm.ConfirmEvent
import net.pantasystem.milktea.model.confirm.ResultType

class ConfirmDialog : AppCompatDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val confirmViewModel = ViewModelProvider(requireActivity())[ConfirmViewModel::class.java]
        val event = confirmViewModel.confirmEvent.event
        if(event != null){
            val builder = MaterialAlertDialogBuilder(requireContext())
            if(event.title != null){
                builder.setTitle(event.title)
            }
            if(event.message != null){
                builder.setMessage(event.message)
            }
            builder.setPositiveButton(event.positiveButtonText?: getString(android.R.string.ok)) { _, _ ->
                confirmViewModel.confirmedEvent.event = ConfirmEvent(
                    confirmId = event.confirmId,
                    resultType = ResultType.POSITIVE,
                    args = event.args,
                    eventType = event.eventType
                )
                dismiss()
            }

            builder.setNegativeButton(event.negativeButtonText?: getString(android.R.string.cancel)) { _, _ ->
                confirmViewModel.confirmedEvent.event = ConfirmEvent(
                    confirmId = event.confirmId,
                    resultType = ResultType.NEGATIVE,
                    args = event.args,
                    eventType = event.eventType
                )
                dismiss()
            }
            return builder.create()
        }else{
            dismiss()
        }

        return super.onCreateDialog(savedInstanceState)
    }
}