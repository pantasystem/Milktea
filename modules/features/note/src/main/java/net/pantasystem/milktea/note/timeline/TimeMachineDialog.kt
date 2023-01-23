package net.pantasystem.milktea.note.timeline

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogTimeMachineBinding
import net.pantasystem.milktea.note.timeline.viewmodel.TimeMachineDialogViewModel
import net.pantasystem.milktea.note.timeline.viewmodel.TimeMachineEventViewModel

@AndroidEntryPoint
class TimeMachineDialog : AppCompatDialogFragment() {

    val viewModel by activityViewModels<TimeMachineDialogViewModel>()
    private val timeMachineEventViewModel by activityViewModels<TimeMachineEventViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(this.context, R.layout.dialog_time_machine, null)
        val binding = DialogTimeMachineBinding.bind(view)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.pickTime.setOnClickListener {
            TimeMachineTimePickerDialog().show(childFragmentManager, "timeMachineTimePickerDialog")
        }

        binding.pickDate.setOnClickListener {
            TimeMachineDatePickerDialog().show(childFragmentManager, "timeMachineDatePickerDialog")
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.time_machine)
            .setMessage(R.string.dialog_timemachine_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                timeMachineEventViewModel.setDateTime(viewModel.currentDateTime.value)
            }.setNegativeButton(android.R.string.cancel) { _, _ ->

            }.setView(view)
            .create()
    }
}

@AndroidEntryPoint
class TimeMachineDatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {

    val viewModel: TimeMachineDialogViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val local = viewModel.currentDateTime.value.toLocalDateTime(TimeZone.currentSystemDefault())
        return DatePickerDialog(requireActivity(), this, local.year, local.monthNumber - 1, local.dayOfMonth)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        viewModel.setDate(year, month, dayOfMonth)
    }
}

class TimeMachineTimePickerDialog : AppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener {
    val viewModel: TimeMachineDialogViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val local = viewModel.currentDateTime.value.toLocalDateTime(TimeZone.currentSystemDefault())
        return TimePickerDialog(requireActivity(), this, local.hour, local.minute, true)
    }
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        viewModel.setTime(hourOfDay, minute)
    }
}