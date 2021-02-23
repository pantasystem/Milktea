package jp.panta.misskeyandroidclient.model.notes


import jp.panta.misskeyandroidclient.api.notes.CreateNote
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import jp.panta.misskeyandroidclient.model.users.User

/**
 * @param noExtractEmojis 本文からカスタム絵文字を展開しないか否か
 * @param noExtractMentions 本文からメンションを展開しないか否か
 * @param noExtractHashtags 本文からハッシュタグを展開しないか否か
 */
data class CreateNote(
    val visibility: CreateNote.Visibility,
    val text: String?,
    val cw: String? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    var files: List<File>? = null,
    val replyId: String? = null,
    val renoteId: String? = null,
    val poll: CreatePoll? = null


)

sealed class Visibility {
    data class Public(
        override val isLocalOnly: Boolean
    ) : LocalOnly, Visibility()

    data class Home(
        override val isLocalOnly: Boolean
    ) : Visibility(), LocalOnly

    data class Followers(
        override val isLocalOnly: Boolean
    ) : Visibility(), LocalOnly

    data class Specified(
        val visibleUserIds: List<String>
    ) : Visibility()
}

interface LocalOnly {
    val isLocalOnly: Boolean
}