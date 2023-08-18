package net.pantasystem.milktea.note.editor.poll

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.model.note.PollExpiresAt
import net.pantasystem.milktea.model.note.expiresAt
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class PollDatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener{

    private val mViewModel: NoteEditorViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel = mViewModel
        val date = viewModel.poll.value?.expiresAt?.expiresAt()?: Clock.System.now()
        val local = date.toLocalDateTime(TimeZone.currentSystemDefault())

        return DatePickerDialog(requireActivity(), this, local.year, local.monthNumber - 1, local.dayOfMonth)

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val date = mViewModel.poll.value?.expiresAt?.expiresAt()?: Clock.System.now()

        val c = Calendar.getInstance()
        c.time = Date(date.toEpochMilliseconds())

        c.set(Calendar.YEAR, p1)
        c.set(Calendar.MONTH, p2)
        c.set(Calendar.DAY_OF_MONTH, p3)

        mViewModel.setPollExpiresAt(
            PollExpiresAt.DateAndTime(Instant.fromEpochMilliseconds(c.time.time))
        )
    }
}