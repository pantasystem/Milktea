package jp.panta.misskeyandroidclient.view.notes.editor

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import java.util.*

class PollDatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener{

    private var mViewModel: NoteEditorViewModel? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewModel  = ViewModelProvider(requireActivity())[NoteEditorViewModel::class.java]
        mViewModel = viewModel
        val date = viewModel.poll.value?.expiresAt?.value?: Date()

        val calendar = Calendar.getInstance()
        calendar.time = date

        return DatePickerDialog(requireActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        val date = mViewModel?.poll?.value?.expiresAt?.value?: Date()

        val c = Calendar.getInstance()
        c.time = date

        c.set(Calendar.YEAR, p1)
        c.set(Calendar.MONTH, p2)
        c.set(Calendar.DAY_OF_MONTH, p3)

        mViewModel?.poll?.value?.expiresAt?.value = c.time
    }
}