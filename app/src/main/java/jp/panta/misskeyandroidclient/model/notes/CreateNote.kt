package jp.panta.misskeyandroidclient.model.notes


import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import jp.panta.misskeyandroidclient.model.users.User
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.jvm.Throws

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
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    var files: List<File>? = null,
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val poll: CreatePoll? = null,
    val draftNoteId: Long? = null


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

fun Visibility.type(): String {
    return when(this) {
        is Visibility.Public -> {
            "public"
        }
        is Visibility.Home -> {
            "home"
        }
        is Visibility.Followers -> {
            "followers"
        }
        is Visibility.Specified -> {
            "specified"
        }
    }
}

@Throws(IllegalArgumentException::class)
fun Visibility(type: String, isLocalOnly: Boolean, visibleUserIds: List<User.Id>? = null): Visibility {
    return when(type.toLowerCase(Locale.US)){
        "public" -> Visibility.Public(isLocalOnly)
        "home" -> Visibility.Home(isLocalOnly)
        "followers" -> Visibility.Followers(isLocalOnly)
        "specified" -> Visibility.Specified(visibleUserIds?: emptyList())
        else -> throw IllegalArgumentException("public, home, followers, specified以外許可されていません。")
    }
    /*require((isLocalOnly && visibility is CanLocalOnly) || !isLocalOnly) {
        "$type では localOnlyは指定できません。"
    }*/


}

fun Visibility.isLocalOnly(): Boolean {
    return (this as? CanLocalOnly)?.isLocalOnly?: false
}

fun CreateNote.visibleUserIds(): List<String>? {
    return (this.visibility as? Visibility.Specified)?.visibleUserIds?.map {
        it.id
    }
}