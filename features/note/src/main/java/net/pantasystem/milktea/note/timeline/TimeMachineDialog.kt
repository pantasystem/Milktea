package net.pantasystem.milktea.note.timeline

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogTimeMachineBinding

@AndroidEntryPoint
class TimeMachineDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {

            val view = View.inflate(this.context, R.layout.dialog_time_machine, null)
            val binding = DialogTimeMachineBinding.bind(view)
            setContentView(view)

            setTitle(R.string.time_machine)

        }
    }
}