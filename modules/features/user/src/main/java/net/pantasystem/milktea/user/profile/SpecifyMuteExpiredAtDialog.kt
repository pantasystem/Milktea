package net.pantasystem.milktea.user.profile

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.viewmodel.UserDetailViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@AndroidEntryPoint
class SpecifyMuteExpiredAtDialog : AppCompatDialogFragment() {

    val viewModel by activityViewModels<UserDetailViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.mute)
            .setView(ComposeView(requireContext()).apply {
                setContent {
                    MdcTheme {
                        SpecifyMuteExpiredAtDialogContent()
                    }
                }
            })
            .setPositiveButton(android.R.string.ok) { _, _ ->

            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->

            }
            .create()
    }
}

@Composable
fun SpecifyMuteExpiredAtDialogContent() {
    var state: SpecifyUserMuteUiState by remember {
        mutableStateOf(SpecifyUserMuteUiState.IndefinitePeriod)
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("無期限")
            Switch(
                checked = state is SpecifyUserMuteUiState.IndefinitePeriod,
                onCheckedChange = {
                    state = if (it) {
                        SpecifyUserMuteUiState.IndefinitePeriod
                    } else {
                        SpecifyUserMuteUiState.Specified(Clock.System.now() + 15.hours)
                    }
                })


        }

        when (val s = state) {
            is SpecifyUserMuteUiState.Specified -> {
                val localDateTime = s.localDateTime

                TextButton(
                    onClick = {
                        state = s.applyDuration(15.minutes)
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text("15分後")
                }
                TextButton(
                    onClick = {
                        state = s.applyDuration(30.minutes)
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text("30分後")
                }
                TextButton(
                    onClick = {
                        state = s.applyDuration(1.hours)
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text("1時間後")
                }
                TextButton(
                    onClick = {
                        state = s.applyDuration(7.days)
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text("1週間後")
                }
                TextButton(
                    onClick = {
                        state = s.applyDuration(30.days)
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text("1ヶ月後")
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = {

                        },
                        Modifier.weight(1f),
                    ) {
                        Text("${localDateTime.year}/${localDateTime.monthNumber}/${localDateTime.dayOfMonth}")
                    }
                    Spacer(Modifier.width(4.dp))
                    Button(
                        onClick = { /*TODO*/ },
                        Modifier.weight(1f),
                    ) {
                        Text("${localDateTime.hour}:${localDateTime.minute}")
                    }
                }
            }
            is SpecifyUserMuteUiState.IndefinitePeriod -> {}
        }

    }
}

sealed interface SpecifyUserMuteUiState {
    object IndefinitePeriod : SpecifyUserMuteUiState
    data class Specified(val dateTime: Instant) : SpecifyUserMuteUiState {
        fun applyDuration(duration: Duration): Specified {
            return copy(dateTime = Clock.System.now() + duration)
        }

        val localDateTime by lazy {
            dateTime.toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}