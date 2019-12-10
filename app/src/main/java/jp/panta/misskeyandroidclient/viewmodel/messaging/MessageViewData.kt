package jp.panta.misskeyandroidclient.viewmodel.messaging

import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.viewmodel.notes.media.FileViewData


abstract class MessageViewData (val message: Message){
    val id = message.id
    abstract val name: String
    abstract val avatarIcon: String
    val text = message.text
    val file = if(message.file == null) null else FileViewData(message.file)
    val isRead = message.isRead
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageViewData

        if (message != other.message) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (avatarIcon != other.avatarIcon) return false
        if (text != other.text) return false
        if (file != other.file) return false
        if (isRead != other.isRead) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + avatarIcon.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (file?.hashCode() ?: 0)
        result = 31 * result + (isRead?.hashCode() ?: 0)
        return result
    }

}