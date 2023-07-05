package net.pantasystem.milktea.model.account.page

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User

class PageableTemplate(val account: Account?) {
    fun globalTimeline(title: String): Page {
        return Page(account?.accountId?: - 1, title, 0, Pageable.GlobalTimeline())
    }
    fun hybridTimeline(title: String, withFiles: Boolean? = null) =
        Page(account?.accountId?: - 1, title, 0, Pageable.HybridTimeline(withFiles = withFiles))

    fun localTimeline(title: String) =
        Page(account?.accountId?: - 1, title, 0, Pageable.LocalTimeline())

    fun homeTimeline(title: String, withFiles: Boolean? = null) = Page(account?.accountId?: - 1, title, 0, Pageable.HomeTimeline(withFiles = withFiles), isSavePagePosition = true)

    fun userListTimeline(listId: String) = Pageable.UserListTimeline(listId = listId)

    fun userListTimeline(userList: UserList): Page {
        return Page(account?.accountId?: - 1, userList.name, 0,  Pageable.UserListTimeline(userList.id.userListId))
    }
    fun mention(title: String): Page {
        return Page(account?.accountId?: - 1, title, 0, Pageable.Mention(null))
    }

    fun show(noteId: String, title: String): Page {
        return Page(account?.accountId?: - 1, title, 0, Pageable.Show(noteId))
    }
    fun tag(tag: String): Page {
        return when(account?.instanceType) {
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> Page(account.accountId, tag, 0, Pageable.Mastodon.HashTagTimeline(tag.replace("#", "")))
            else -> Page(account?.accountId?: - 1, tag, 0, Pageable.SearchByTag(tag.replace("#", "")))
        }
    }
    fun search(query: String): Page {
        return when(account?.instanceType) {
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> Page(account.accountId, query, 0, Pageable.Mastodon.SearchTimeline(query))
            else -> Page(account?.accountId?: - 1, query, 0, Pageable.Search(query))
        }
    }
    fun featured(title: String) = Page(account?.accountId?: - 1, title, 0, Pageable.Featured(null))
    fun notification(title: String) = Page(account?.accountId?: - 1, title, 0, Pageable.Notification())
    fun user(userId: String, title: String): Page {
        return Page(account?.accountId?: - 1, title, 0, Pageable.UserTimeline(userId))
    }
    fun user(user: User, isUserNameDefault: Boolean): Page {
        val title = if(isUserNameDefault) user.shortDisplayName else user.displayName
        return Page(account?.accountId?: - 1, title, 0, Pageable.UserTimeline(userId = user.id.id))
    }
    fun favorite(title: String): Page {
        return Page(account?.accountId?: - 1, title, 0, Pageable.Favorite)
    }
    fun antenna(antennaId: String, title: String): Page {
        return Page(account?.accountId?: - 1, title, 0, Pageable.Antenna(antennaId))
    }
    fun antenna(antenna: Antenna): Page {
        return Page(account?.accountId ?: antenna.id.accountId, antenna.name, 0, Pageable.Antenna(antenna.id.antennaId))
    }

    fun mastodonPublicTimeline(title: String): Page {
        return Page(
            account?.accountId ?: -1,
            title,
            0,
            Pageable.Mastodon.PublicTimeline()
        )
    }

    fun mastodonLocalTimeline(title: String): Page {
        return Page(
            account?.accountId ?: -1,
            title,
            0,
            Pageable.Mastodon.LocalTimeline(),
        )
    }

    fun mastodonHomeTimeline(title: String): Page {
        return Page(
            account?.accountId ?: -1,
            title,
            0,
            Pageable.Mastodon.HomeTimeline,
            isSavePagePosition = true,
        )
    }

    fun calckeyRecommendedTimeline(title: String): Page {
        return Page(
            account?.accountId ?: -1,
            title,
            0,
            Pageable.CalckeyRecommendedTimeline
        )
    }
}

fun Account.newPage(pageable: Pageable, name: String): Page {
    return Page(this.accountId, name, 0, pageable)
}
