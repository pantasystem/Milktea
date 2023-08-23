package net.pantasystem.milktea.note.editor.poll

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.model.note.PollExpiresAt
import net.pantasystem.milktea.model.note.expiresAt
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import java.util.Calendar
import java.util.Date

class PollTimePickerDialog : AppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener {

    companion object {
        const val FRAGMENT_TAG = "PollTimePickerDialog"
    }

    private val mViewModel: NoteEditorViewModel by activityViewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel = mViewModel
        val date = viewModel.poll.value?.expiresAt?.expiresAt() ?: Clock.System.now()
        val local = date.toLocalDateTime(TimeZone.currentSystemDefault())
        return TimePickerDialog(requireActivity(), this, local.hour, local.minute, true)
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        val date = mViewModel.poll.value?.expiresAt?.asDate() ?: Date()
        val c = Calendar.getInstance()
        c.time = date
        c.set(Calendar.HOUR_OF_DAY, p1)
        c.set(Calendar.MINUTE, p2)
        mViewModel.setPollExpiresAt(
            PollExpiresAt.DateAndTime(
                Instant.fromEpochMilliseconds(c.time.time)
            )
        )

    }
}