package net.pantasystem.milktea.messaging.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.messaging.CreateMessage
import net.pantasystem.milktea.model.messaging.MessageRepository
import net.pantasystem.milktea.model.messaging.MessagingId
import javax.inject.Inject

@HiltViewModel
class MessageEditorViewModel @Inject constructor(

    private val filePropertyDataSource: FilePropertyDataSource,
    private val messageRepository: MessageRepository,
    loggerFactory: Logger.Factory

) : ViewModel() {


    private val logger = loggerFactory.create("MessageActionViewModel")

    private val _uiState = MutableStateFlow(MessageEditorUiState("", null))
    val uiState: StateFlow<MessageEditorUiState> = _uiState

    private val mErrors = MutableStateFlow<Throwable?>(null)

    private val messagingId = MutableStateFlow<MessagingId?>(null)

    fun setFilePropertyFromId(filePropertyId: FileProperty.Id) {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(file = filePropertyDataSource.find(filePropertyId).getOrNull())
            }
        }
    }

    fun send() {

        val tmpText = uiState.value.text
        val tmpFile = uiState.value.file
        val msgId = messagingId.value

        require(msgId != null)
        viewModelScope.launch {
            val createMessage = CreateMessage.Factory.create(msgId, tmpText, tmpFile?.id?.fileId)
            runCancellableCatching { messageRepository.create(createMessage) }.onFailure {
                logger.error("メッセージ作成中にエラー発生", e = it)
                mErrors.value = it
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(file = null, text = "")
                }
            }
        }
    }

    fun setMessagingId(messagingId: MessagingId) {
        this.messagingId.update {
            messagingId
        }
    }

    fun setText(text: String) {
        _uiState.update { state ->
            state.copy(text = text)
        }
    }

}

data class MessageEditorUiState(
    val text: String,
    val file: FileProperty?,
)