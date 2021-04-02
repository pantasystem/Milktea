package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.users.User
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.jvm.Throws

sealed class Visibility : Serializable{
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
        else -> throw IllegalArgumentException("public, home, followers, specified以外許可されていません。与えられたデータ:$type")
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