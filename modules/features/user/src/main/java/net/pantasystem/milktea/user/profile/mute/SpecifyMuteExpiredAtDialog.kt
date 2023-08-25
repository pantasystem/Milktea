package net.pantasystem.milktea.user.profile.mute

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.profile.viewmodel.UserDetailViewModel

@AndroidEntryPoint
class SpecifyMuteExpiredAtDialog : AppCompatDialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "SpecifyMuteExpiredAtDialog"
    }

    val userDetailViewModel by activityViewModels<UserDetailViewModel>()
    val viewModel by activityViewModels<MuteUserViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.mute)
            .setView(ComposeView(requireContext()).apply {
                setContent {
                    MdcTheme {
                        SpecifyMuteExpiredAtDialogContent(
                            state = viewModel.state,
                            onAction = { action ->
                                when(action) {
                                    is SpecifyMuteUserAction.OnChangeState -> {
                                        viewModel.onUpdateState(action.state)
                                    }
                                    SpecifyMuteUserAction.OnDateChangeButtonClicked -> {
                                        UserMuteExpiredDatePickerDialog()
                                            .show(childFragmentManager, UserMuteExpiredDatePickerDialog.FRAGMENT_TAG)
                                    }
                                    SpecifyMuteUserAction.OnTimeChangeButtonClicked -> {
                                        UserMuteExpiredTimePickerDialog()
                                            .show(childFragmentManager, UserMuteExpiredTimePickerDialog.FRAGMENT_TAG)
                                    }
                                }
                            }
                        )
                    }
                }
            })
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val expiredAt = when(val state = viewModel.state) {
                    SpecifyUserMuteUiState.IndefinitePeriod -> {
                        null
                    }
                    is SpecifyUserMuteUiState.Specified -> {
                        state.dateTime
                    }
                }
                userDetailViewModel.mute(expiredAt)
                viewModel.onConfirmed()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                viewModel.onCanceled()
            }
            .create()
    }
}

@AndroidEntryPoint
class UserMuteExpiredDatePickerDialog : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {

    companion object {
        const val FRAGMENT_TAG = "UserMuteExpiredDatePickerDialog"
    }

    val viewModel: MuteUserViewModel by activityViewModels()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val local = viewModel.expiredAt.toLocalDateTime(TimeZone.currentSystemDefault())
        return DatePickerDialog(requireActivity(), this, local.year, local.monthNumber - 1, local.dayOfMonth)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        viewModel.setDate(year, month, dayOfMonth)
    }
}

@AndroidEntryPoint
class UserMuteExpiredTimePickerDialog : AppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener {
    companion object {
        const val FRAGMENT_TAG = "UserMuteExpiredTimePickerDialog"
    }
    val viewModel: MuteUserViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val local = viewModel.expiredAt.toLocalDateTime(TimeZone.currentSystemDefault())
        return TimePickerDialog(requireActivity(), this, local.hour, local.minute, true)
    }
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        viewModel.setTime(hourOfDay, minute)
    }
}