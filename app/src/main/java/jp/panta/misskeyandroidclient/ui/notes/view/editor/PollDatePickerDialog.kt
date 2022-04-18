package jp.panta.misskeyandroidclient.ui.notes.view.editor

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.notes.PollExpiresAt
import net.pantasystem.milktea.model.notes.expiresAt
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.editor.NoteEditorViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*

@AndroidEntryPoint
class PollDatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener{

    private val mViewModel: NoteEditorViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel = mViewModel
        val date = viewModel.poll.value?.expiresAt?.expiresAt()?: Clock.System.now()


        val calendar = Calendar.getInstance()
        calendar.time = Date(date.toEpochMilliseconds())

        return DatePickerDialog(requireActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val date = mViewModel.poll.value?.expiresAt?.expiresAt()?: Clock.System.now()

        val c = Calendar.getInstance()
        c.time = Date(date.toEpochMilliseconds())

        c.set(Calendar.YEAR, p1)
        c.set(Calendar.MONTH, p2)
        c.set(Calendar.DAY_OF_MONTH, p3)

        mViewModel.state.value.let { state ->
            mViewModel.updateState(
                state.copy(
                    poll = state.poll?.copy(
                        expiresAt = net.pantasystem.milktea.model.notes.PollExpiresAt.DateAndTime(Instant.fromEpochMilliseconds(c.time.time))
                    )
                )
            )
        }
    }
}