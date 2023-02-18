package net.pantasystem.milktea.note.editor.poll

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import net.pantasystem.milktea.common_compose.Spinner
import net.pantasystem.milktea.common_compose.SwitchTile
import net.pantasystem.milktea.common_resource.R
import net.pantasystem.milktea.model.notes.PollChoiceState
import net.pantasystem.milktea.model.notes.PollEditingState
import net.pantasystem.milktea.model.notes.PollExpiresAt
import java.util.*

@Composable
fun PollEditorLayout(
    modifier: Modifier = Modifier,
    uiState: PollEditingState,
    onInput: (id: UUID, value: String) -> Unit,
    onAddAnswerButtonClicked: () -> Unit,
    onRemove: (id: UUID) -> Unit,
    onExpireAtTypeChanged: (type: ExpireAtType) -> Unit,
    onExpireAtChangeDateButtonClicked: () -> Unit,
    onExpireAtChangeTimeButtonClicked: () -> Unit,
    onMultipleAnswerTypeChanged: (Boolean) -> Unit,
) {
    Column(modifier) {
        Column(Modifier.fillMaxWidth()) {
            for (i in 0 until uiState.choices.size) {
                val element = uiState.choices[i]
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = element.text,
                    onValueChange = {
                        onInput(element.id, it)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            onRemove(element.id)
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                )
                if (i < (uiState.choices.size - 1)) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        TextButton(onClick = onAddAnswerButtonClicked) {
            Text(stringResource(id = R.string.add_choice))
        }
        SwitchTile(checked = uiState.multiple, onChanged = onMultipleAnswerTypeChanged) {
            Text(text = stringResource(id = R.string.multiple_answer))
        }
        Spinner(
            items = listOf(ExpireAtType.IndefinitePeriod, ExpireAtType.SpecificDateAndTime),
            selectedItem = when (uiState.expiresAt) {
                is PollExpiresAt.DateAndTime -> ExpireAtType.SpecificDateAndTime
                PollExpiresAt.Infinity -> ExpireAtType.IndefinitePeriod
            },
            onItemSelected = onExpireAtTypeChanged,
            selectedItemFactory = { modifier, item ->
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        when (item) {
                            ExpireAtType.IndefinitePeriod -> stringResource(id = R.string.indefinite_period)
                            ExpireAtType.SpecificDateAndTime -> stringResource(id = R.string.date_and_time_specification)
                        },
                        fontSize = 16.sp
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

            },
        ) { item, _ ->
            when (item) {
                ExpireAtType.IndefinitePeriod -> ExpireAtTypeLayout(
                    Modifier.fillMaxWidth(),
                    stringResource(id = R.string.indefinite_period)
                )
                ExpireAtType.SpecificDateAndTime -> ExpireAtTypeLayout(
                    Modifier.fillMaxWidth(),
                    stringResource(id = R.string.date_and_time_specification)
                )
            }
        }
        when (val expireAt = uiState.expiresAt) {
            is PollExpiresAt.DateAndTime -> {
                Row(Modifier.fillMaxWidth()) {
                    Button(onClick = onExpireAtChangeDateButtonClicked) {
                        Text("${expireAt.year}/${expireAt.month}/${expireAt.dayOfMonth}")
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Button(onClick = onExpireAtChangeTimeButtonClicked) {
                        Text("${expireAt.hour}:${expireAt.minutes}")
                    }
                }
            }
            PollExpiresAt.Infinity -> Unit
        }
    }
}

@Composable
fun ExpireAtTypeLayout(modifier: Modifier, text: String) {
    Row(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}

@Preview
@Composable
fun Preview_PollEditorLayout() {
    Surface {
        PollEditorLayout(
            uiState = PollEditingState(
                choices = listOf(
                    PollChoiceState(
                        "test1",
                        UUID.randomUUID(),
                    ),
                    PollChoiceState(
                        "test2",
                        UUID.randomUUID(),
                    ),
                    PollChoiceState(
                        "test3",
                        UUID.randomUUID(),
                    )
                ),
                multiple = false,
                expiresAt = PollExpiresAt.DateAndTime(Clock.System.now())
            ),
            onInput = { _, _ -> },
            onRemove = {},
            onAddAnswerButtonClicked = {},
            onExpireAtChangeDateButtonClicked = {},
            onExpireAtChangeTimeButtonClicked = {},
            onExpireAtTypeChanged = {},
            onMultipleAnswerTypeChanged = {}
        )
    }
}


enum class ExpireAtType {
    IndefinitePeriod, SpecificDateAndTime,
}