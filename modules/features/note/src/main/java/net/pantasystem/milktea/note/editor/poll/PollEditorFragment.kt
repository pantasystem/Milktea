package net.pantasystem.milktea.note.editor.poll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.datetime.Clock
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.model.notes.PollExpiresAt
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@AndroidEntryPoint
class PollEditorFragment : Fragment() {

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    val viewModel: NoteEditorViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                    val uiState by viewModel.uiState.collectAsState()
                    when (val pollState = uiState.poll) {
                        null -> {}
                        else -> {
                            PollEditorLayout(
                                uiState = pollState,
                                onInput = { id, value ->
                                    viewModel.changePollChoice(id, value)
                                },
                                onAddAnswerButtonClicked = {
                                    viewModel.addPollChoice()
                                },
                                onRemove = {
                                    viewModel.removePollChoice(it)
                                },
                                onExpireAtTypeChanged = {
                                    when(it) {
                                        ExpireAtType.IndefinitePeriod -> {
                                            viewModel.setPollExpiresAt(PollExpiresAt.Infinity)
                                        }
                                        ExpireAtType.SpecificDateAndTime -> {
                                            viewModel.setPollExpiresAt(PollExpiresAt.DateAndTime(
                                                Clock.System.now().plus(1.days)
                                            ))
                                        }
                                    }
                                },
                                onExpireAtChangeDateButtonClicked = viewModel::onExpireAtChangeDateButtonClicked,
                                onExpireAtChangeTimeButtonClicked = viewModel::onExpireAtChangeTimeButtonClicked,
                                onMultipleAnswerTypeChanged = {
                                    viewModel.togglePollMultiple()
                                }
                            )
                        }
                    }

                }
            }
        }
    }

}