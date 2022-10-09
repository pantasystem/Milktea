package net.pantasystem.milktea.user.profile.mute

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import net.pantasystem.milktea.user.viewmodel.SpecifyUserMuteUiState
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes


@Composable
fun SpecifyMuteExpiredAtDialogContent(
    state: SpecifyUserMuteUiState,
    onAction: (SpecifyMuteUserAction) -> Unit,
) {

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
                    onAction(
                        SpecifyMuteUserAction.OnChangeState(
                            if (it) {
                                SpecifyUserMuteUiState.IndefinitePeriod
                            } else {
                                SpecifyUserMuteUiState.Specified(Clock.System.now() + 15.hours)
                            }
                        )
                    )
                })


        }

        when (val s = state) {
            is SpecifyUserMuteUiState.Specified -> {
                val localDateTime = s.localDateTime

                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                s.applyDuration(15.minutes)
                            )
                        )
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text("15分後")
                }
                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                s.applyDuration(30.minutes)
                            )
                        )
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text("30分後")
                }
                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                s.applyDuration(1.hours)
                            )
                        )
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text("1時間後")
                }
                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                s.applyDuration(7.days)
                            )
                        )
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text("1週間後")
                }
                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                s.applyDuration(30.days)
                            )
                        )
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

sealed interface SpecifyMuteUserAction {
    data class OnChangeState(val state: SpecifyUserMuteUiState) : SpecifyMuteUserAction
    object OnDateChangeButtonClicked : SpecifyMuteUserAction
    object OnTimeChangeButtonClicked : SpecifyMuteUserAction
}