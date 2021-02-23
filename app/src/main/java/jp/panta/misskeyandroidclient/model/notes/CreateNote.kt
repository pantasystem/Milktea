package jp.panta.misskeyandroidclient.model.notes


import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import jp.panta.misskeyandroidclient.model.users.User

/**
 * @param noExtractEmojis 本文からカスタム絵文字を展開しないか否か
 * @param noExtractMentions 本文からメンションを展開しないか否か
 * @param noExtractHashtags 本文からハッシュタグを展開しないか否か
 */
data class CreateNote(
    val author: Account,
    val visibility: Visibility,
    val text: String?,
    val cw: String? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    var files: List<File>? = null,
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val poll: CreatePoll? = null


)

sealed class Visibility {
    data class Public(
        override val isLocalOnly: Boolean
    ) : CanLocalOnly, Visibility()

    data class Home(
        override val isLocalOnly: Boolean
    ) : Visibility(), CanLocalOnly

    data class Followers(
        override val isLocalOnly: Boolean
    ) : Visibility(), CanLocalOnly

    data class Specified(
        val visibleUserIds: List<User.Id>
    ) : Visibility()

    /*
    NOTE: LocalOnlyはPub,Ho,Fのいずれかを選択し、そのうちLocalのユーザーに限定に公開されるというものなので、LocalOnlyというVisibilityは存在しない
    object LocalOnly : Visibility(), CanLocalOnly {
        override val isLocalOnly: Boolean = true
    }*/

}

interface CanLocalOnly {
    val isLocalOnly: Boolean
}