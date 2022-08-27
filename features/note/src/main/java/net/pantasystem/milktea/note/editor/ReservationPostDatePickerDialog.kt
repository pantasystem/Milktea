package net.pantasystem.milktea.note.editor

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.datetime.Instant
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import java.util.*

class ReservationPostDatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {
    private var mViewModel: NoteEditorViewModel? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel  = ViewModelProvider(requireActivity())[NoteEditorViewModel::class.java]
        mViewModel = viewModel
        val date = viewModel.reservationPostingAt.value

        val calendar = Calendar.getInstance()
        calendar.time = date ?: Date()

        return DatePickerDialog(requireActivity(), this, calendar.get(Calendar.YEAR), calendar.get(
            Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val date = mViewModel?.reservationPostingAt?.value ?: Date()

        val c = Calendar.getInstance()
        c.time = date

        c.set(Calendar.YEAR, p1)
        c.set(Calendar.MONTH, p2)
        c.set(Calendar.DAY_OF_MONTH, p3)

        mViewModel?.state?.value?.let { state ->
            mViewModel?.updateState(
                state.copy(
                    reservationPostingAt = Instant.fromEpochMilliseconds(c.time.time)
                )
            )
        }
    }

}

