package net.pantasystem.milktea.note.editor

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import java.util.*


@AndroidEntryPoint
class ReservationPostTimePickerDialog : AppCompatDialogFragment(),
    TimePickerDialog.OnTimeSetListener {

    private val mViewModel: NoteEditorViewModel by activityViewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel = mViewModel
        val local = (viewModel.uiState.value.sendToState.schedulePostAt ?: Clock.System.now()).toLocalDateTime(TimeZone.currentSystemDefault())
        return TimePickerDialog(requireActivity(), this, local.hour, local.minute, true)
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        val date = mViewModel.reservationPostingAt.value ?: Date()
        val c = Calendar.getInstance()
        c.time = date
        c.set(Calendar.HOUR_OF_DAY, p1)
        c.set(Calendar.MINUTE, p2)
        mViewModel.setSchedulePostAt(Instant.fromEpochMilliseconds(c.time.time))

    }
}