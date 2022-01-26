package jp.panta.misskeyandroidclient.ui.messaging.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.mfm.MFMParser
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageRelation
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import java.lang.IllegalArgumentException


abstract class MessageViewData (val message: MessageRelation, account: Account){
    val messagingId = message.message.messagingId(account)
    val id = message.message.id
    abstract val name: String
    abstract val avatarIcon: String
    val text = message.message.text
    val textNode = MFMParser.parse(message.message.text, message.message.emojis)
    val file = if(message.message.file == null) null else message.message.file?.toFile()?.let{
        FileViewData(it)
    }
    //val isRead = message.message.isRead
    private val mIsReadLiveData = MutableLiveData(message.message.isRead)
    private var mIsRead: Boolean = message.message.isRead
        set(value) {
            mIsReadLiveData.postValue(value)
            field = value
        }

    val isRead: LiveData<Boolean> = mIsReadLiveData

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageViewData

        if (message != other.message) return false
        if (messagingId != other.messagingId) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (avatarIcon != other.avatarIcon) return false
        if (text != other.text) return false
        if (textNode != other.textNode) return false
        if (file != other.file) return false
        if (isRead != other.isRead) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + messagingId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + avatarIcon.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (textNode?.hashCode() ?: 0)
        result = 31 * result + (file?.hashCode() ?: 0)
        result = 31 * result + isRead.hashCode()
        return result
    }

    fun update(message: Message) {
        require(this.message.message.id == message.id)
        mIsRead = message.isRead
    }


}

class SelfMessageViewData(message: MessageRelation, account: Account) : MessageViewData(message, account){
    override val avatarIcon: String = message.user.avatarUrl?: throw IllegalArgumentException("not self message")

    override val name: String = message.user.name?: message.user.userName

}

class OtherUserMessageViewData(message: MessageRelation, account: Account) : MessageViewData(message, account){
    override val avatarIcon: String = message.user.avatarUrl?: throw IllegalArgumentException("not recipient")
    override val name: String = message.user.name?: message.user.userName
}