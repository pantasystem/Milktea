package jp.panta.misskeyandroidclient.ui.notes.view.editor

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import net.pantasystem.milktea.model.notes.PollExpiresAt
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.editor.NoteEditorViewModel
import kotlinx.datetime.Instant
import java.util.*

class PollTimePickerDialog : AppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener{

    private val mViewModel: NoteEditorViewModel by activityViewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val viewModel = mViewModel
        val date = viewModel.poll.value?.expiresAt?.asDate()?: Date()
        val c = Calendar.getInstance()
        c.time = date
        return TimePickerDialog(requireActivity(), this, c[Calendar.HOUR], c[Calendar.MINUTE], true)

    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        val date = mViewModel.poll.value?.expiresAt?.asDate() ?: Date()
        val c = Calendar.getInstance()
        c.time = date
        c[Calendar.HOUR] = p1
        c[Calendar.MINUTE] = p2
        mViewModel.updateState(
            mViewModel.state.value.copy(
                poll = mViewModel.state.value.poll?.copy(
                    expiresAt = net.pantasystem.milktea.model.notes.PollExpiresAt.DateAndTime(
                        Instant.fromEpochMilliseconds(c.time.time)
                    )
                )
            )
        )

    }
}