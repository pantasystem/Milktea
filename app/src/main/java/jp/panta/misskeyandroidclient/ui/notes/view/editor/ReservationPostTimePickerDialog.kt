package jp.panta.misskeyandroidclient.ui.notes.view.editor

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.editor.NoteEditorViewModel
import kotlinx.datetime.Instant
import java.util.*


class ReservationPostTimePickerDialog : AppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener{

    private var mViewModel: NoteEditorViewModel? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel = ViewModelProvider(requireActivity())[NoteEditorViewModel::class.java]
        mViewModel = viewModel

        val date = viewModel.reservationPostingAt.value?: Date()
        val c = Calendar.getInstance()
        c.time = date
        return TimePickerDialog(requireActivity(), this, c[Calendar.HOUR], c[Calendar.MINUTE], true)

    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        val date = mViewModel?.reservationPostingAt?.value ?: Date()
        val c = Calendar.getInstance()
        c.time = date
        c[Calendar.HOUR] = p1
        c[Calendar.MINUTE] = p2
        mViewModel?.let { viewModel ->
            viewModel.updateState(
                viewModel.state.value.copy(
                    reservationPostingAt = Instant.fromEpochMilliseconds(c.time.time)
                )
            )
        }

    }
}