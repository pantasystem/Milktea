package net.pantasystem.milktea.messaging.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.messaging.CreateMessage
import net.pantasystem.milktea.model.messaging.MessageRepository
import net.pantasystem.milktea.model.messaging.MessagingId
import javax.inject.Inject

@HiltViewModel
class MessageActionViewModel @Inject constructor(

    private val filePropertyDataSource: FilePropertyDataSource,
    private val messageRepository: MessageRepository,
    loggerFactory: Logger.Factory

) : ViewModel() {


    private val logger = loggerFactory.create("MessageActionViewModel")

    val text = MutableLiveData<String>()
    val file = MutableLiveData<FileProperty?>()

    private val mErrors = MutableStateFlow<Throwable?>(null)

    private val messagingId = MutableStateFlow<MessagingId?>(null)

    fun setFilePropertyFromId(filePropertyId: FileProperty.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            file.postValue(filePropertyDataSource.find(filePropertyId))
        }
    }

    fun send() {

        val tmpText = text.value
        val tmpFile = file.value
        val msgId = messagingId.value

        require(msgId != null)
        viewModelScope.launch(Dispatchers.IO) {
            val createMessage = CreateMessage.Factory.create(msgId, tmpText, tmpFile?.id?.fileId)
            runCatching { messageRepository.create(createMessage) }.onFailure {
                logger.error("メッセージ作成中にエラー発生", e = it)
                mErrors.value = it
            }.onSuccess {
                file.postValue(null)
                text.postValue("")
            }
        }
    }

    fun setMessagingId(messagingId: MessagingId) {
        this.messagingId.update {
            messagingId
        }
    }

}