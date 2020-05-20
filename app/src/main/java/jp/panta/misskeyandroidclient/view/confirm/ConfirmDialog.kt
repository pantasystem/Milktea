package jp.panta.misskeyandroidclient.view.confirm

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.confirm.ConfirmEvent
import jp.panta.misskeyandroidclient.model.confirm.ResultType
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel

class ConfirmDialog : AppCompatDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val confirmViewModel = ViewModelProvider(requireActivity())[ConfirmViewModel::class.java]
        val event = confirmViewModel.confirmEvent.event
        if(event != null){
            val builder = AlertDialog.Builder(requireContext())
                .setTitle(event.title)
            if(event.message != null){
                builder.setMessage(event.message)
            }
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                confirmViewModel.confirmedEvent.event = ConfirmEvent(
                    confirmId = event.confirmId,
                    resultType = ResultType.POSITIVE,
                    args = event.args,
                    eventType = event.eventType
                )
                dismiss()
            }

            builder.setNegativeButton(android.R.string.cancel) { _, _ ->
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