package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.user.User
import java.io.Serializable
import java.util.*

sealed class Visibility : Serializable{
    data class Public(
        override val isLocalOnly: Boolean
    ) : CanLocalOnly, Visibility() {
        override fun changeLocalOnly(isLocalOnly: Boolean): CanLocalOnly {
            return this.copy(isLocalOnly = isLocalOnly)
        }
    }

    data class Home(
        override val isLocalOnly: Boolean
    ) : Visibility(), CanLocalOnly {
        override fun changeLocalOnly(isLocalOnly: Boolean): CanLocalOnly {
            return this.copy(isLocalOnly = isLocalOnly)
        }
    }

    data class Followers(
        override val isLocalOnly: Boolean
    ) : Visibility(), CanLocalOnly {
        override fun changeLocalOnly(isLocalOnly: Boolean): CanLocalOnly {
            return this.copy(isLocalOnly = isLocalOnly)
        }
    }

    data class Specified(
        val visibleUserIds: List<User.Id>
    ) : Visibility()
    /*
    NOTE: LocalOnlyはPub,Ho,Fのいずれかを選択し、そのうちLocalのユーザーに限定に公開されるというものなので、LocalOnlyというVisibilityは存在しない
    object LocalOnly : Visibility(), CanLocalOnly {
        override val isLocalOnly: Boolean = true
    }*/

    object Personal : Visibility()

    data class Limited(val circleId: String?) : Visibility()

    object Mutual : Visibility()
}

interface CanLocalOnly {
    val isLocalOnly: Boolean

    fun changeLocalOnly(isLocalOnly: Boolean): CanLocalOnly
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
        is Visibility.Limited -> "limited"
        Visibility.Mutual -> "mutual"
        Visibility.Personal -> "personal"
    }
}

fun Visibility.type4Mastodon(): String {
    return when(this) {
        is Visibility.Followers -> "private"
        is Visibility.Home -> "unlisted"
        is Visibility.Public -> "public"
        is Visibility.Specified -> "direct"
        is Visibility.Limited -> "limited"
        Visibility.Mutual -> "mutual"
        Visibility.Personal -> "personal"
    }
}

@Throws(IllegalArgumentException::class)
fun Visibility(type: String, isLocalOnly: Boolean, visibleUserIds: List<User.Id>? = null): Visibility {
    return when(type.lowercase(Locale.ROOT)){
        "public" -> Visibility.Public(isLocalOnly)
        "home" -> Visibility.Home(isLocalOnly)
        "followers" -> Visibility.Followers(isLocalOnly)
        "specified" -> Visibility.Specified(visibleUserIds ?: emptyList())
        else -> Visibility(type)
    }
}

fun Visibility(type: String, circleId: String? = null, visibilityEx: String? = null,): Visibility {
    return when(type.lowercase()) {
        "private" -> {
            when(visibilityEx) {
                "limited" -> Visibility.Limited(circleId)
                else -> Visibility.Followers(false)
            }
        }
        "unlisted" -> Visibility.Home(false)
        "public" -> Visibility.Public(false)
        "direct" -> when(visibilityEx) {
            "personal" -> Visibility.Personal
            else -> Visibility.Specified(emptyList())
        }
        "followers" -> Visibility.Followers(false)
        "home" -> Visibility.Home(false)
        else -> throw IllegalArgumentException("limited, direct, unlisted, private public, home, followers, specified以外許可されていません。与えられたデータ:$type")
    }
}

fun Visibility(type: String, circleId: String?, localOnly: Boolean?): Visibility {
    return when(type.lowercase(Locale.ROOT)){
        "public" -> Visibility.Public(localOnly ?: false)
        "home" -> Visibility.Home(localOnly ?: false)
        "followers" -> Visibility.Followers(localOnly ?: false)
        "specified" -> Visibility.Specified(emptyList())
        "limited" -> Visibility.Limited(circleId)
        "mutual" -> Visibility.Mutual
        "personal" -> Visibility.Personal
        else -> throw IllegalArgumentException("limited, direct, unlisted, private public, home, followers, specified以外許可されていません。与えられたデータ:$type")
    }
}

fun Visibility.isLocalOnly(): Boolean {
    return (this as? CanLocalOnly)?.isLocalOnly?: false
}

fun CreateNote.visibleUserIds(): List<String>? {
    return (this.visibility as? Visibility.Specified)?.visibleUserIds?.map {
        it.id
    }
}

fun Visibility.getName(softwareType: NodeInfo.SoftwareType? = null) : String{
    return when(this) {
        is Visibility.Public -> "public"
        is Visibility.Home -> when(softwareType) {
            is NodeInfo.SoftwareType.Mastodon -> "unlisted"
            else -> "home"
        }
        is Visibility.Specified -> when(softwareType) {
            is NodeInfo.SoftwareType.Mastodon -> "direct"
            else -> "specified"
        }
        is Visibility.Followers -> when(softwareType) {
            is NodeInfo.SoftwareType.Mastodon -> "private"
            else -> "followers"
        }
        is Visibility.Limited -> "limited"
        is Visibility.Mutual -> "mutual"
        is Visibility.Personal -> "personal"
    }
}