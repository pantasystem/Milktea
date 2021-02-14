package jp.panta.misskeyandroidclient.model.emoji

import java.io.Serializable

@kotlinx.serialization.Serializable
data class Emoji(
    val id: String?,
    val name: String,
    val host: String?,
    val url: String?,
    val uri: String?,
    val type: String?,
    val category: String?
    //val aliases: List<String>

): Serializable{
    fun isSvg(): Boolean{
        return uri?.contains("svg") == true
                || url?.contains("svg") == true
                || type?.contains("svg") == true
    }

    constructor(name: String) : this(null, name, null, null, null, null, null)
}