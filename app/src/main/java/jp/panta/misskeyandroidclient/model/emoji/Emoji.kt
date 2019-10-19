package jp.panta.misskeyandroidclient.model.emoji

import java.io.Serializable

data class Emoji(
    val id: String?,
    val name: String,
    val host: String?,
    val url: String?,
    val uri: String?,
    val type: String?
    //val aliases: List<String>

): Serializable{
    fun isSvg(): Boolean{
        return uri?.contains("svg") == true
                || url?.contains("svg") == true
                || type?.contains("svg") == true
    }
}