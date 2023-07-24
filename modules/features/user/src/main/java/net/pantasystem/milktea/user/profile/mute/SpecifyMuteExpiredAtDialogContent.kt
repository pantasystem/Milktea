package net.pantasystem.milktea.user.profile.mute

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import net.pantasystem.milktea.user.R
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
                .padding(horizontal = 16.dp)
                .clickable {
                    onAction(
                        SpecifyMuteUserAction.OnChangeState(
                            if (state is SpecifyUserMuteUiState.Specified) {
                                SpecifyUserMuteUiState.IndefinitePeriod
                            } else {
                                SpecifyUserMuteUiState.Specified(Clock.System.now() + 15.minutes)
                            }
                        )
                    )
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.user_mute_specify_time_indefinite_period))
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

        when (state) {
            is SpecifyUserMuteUiState.Specified -> {
                val localDateTime = state.localDateTime

                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                state.applyDuration(15.minutes)
                            )
                        )
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.user_mute_specify_time_15_minutes_later))
                }
                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                state.applyDuration(30.minutes)
                            )
                        )
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.user_mute_specify_time_30_minutes_later))
                }
                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                state.applyDuration(1.hours)
                            )
                        )
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.user_mute_specify_time_1_hours_later))
                }
                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                state.applyDuration(7.days)
                            )
                        )
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.user_mute_specify_time_1_week_later))
                }
                TextButton(
                    onClick = {
                        onAction(
                            SpecifyMuteUserAction.OnChangeState(
                                state.applyDuration(30.days)
                            )
                        )
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.user_mute_specify_time_1_month_later))
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = {
                            onAction(SpecifyMuteUserAction.OnDateChangeButtonClicked)
                        },
                        Modifier.weight(1f),
                    ) {
                        Text("${localDateTime.year}/${localDateTime.monthNumber}/${localDateTime.dayOfMonth}")
                    }
                    Spacer(Modifier.width(4.dp))
                    Button(
                        onClick = {
                            onAction(SpecifyMuteUserAction.OnTimeChangeButtonClicked)
                        },
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