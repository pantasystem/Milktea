package net.pantasystem.milktea.note.editor

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import java.util.*

class ReservationPostDatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {
    private var mViewModel: NoteEditorViewModel? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel  = ViewModelProvider(requireActivity())[NoteEditorViewModel::class.java]
        mViewModel = viewModel
        val date = viewModel.uiState.value.sendToState.schedulePostAt ?: Clock.System.now()
        val local = date.toLocalDateTime(TimeZone.currentSystemDefault())

        return DatePickerDialog(requireActivity(), this, local.year, local.monthNumber - 1, local.dayOfMonth)

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val date = mViewModel?.reservationPostingAt?.value ?: Date()

        val c = Calendar.getInstance()
        c.time = date

        c.set(Calendar.YEAR, p1)
        c.set(Calendar.MONTH, p2)
        c.set(Calendar.DAY_OF_MONTH, p3)

        mViewModel?.setSchedulePostAt(Instant.fromEpochMilliseconds(c.time.time))
    }

}

