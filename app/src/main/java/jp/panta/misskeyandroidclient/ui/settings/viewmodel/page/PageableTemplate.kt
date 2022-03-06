package jp.panta.misskeyandroidclient.ui.settings.viewmodel.page

import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.api.misskey.list.UserListDTO
import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import jp.panta.misskeyandroidclient.model.antenna.Antenna

class PageableTemplate(val account: Account?) {
    fun globalTimeline(title: String): Page{
        return Page(account?.accountId?: - 1, title, 0, Pageable.GlobalTimeline())
    }
    fun hybridTimeline(title: String) =
        Page(account?.accountId?: - 1, title, 0, Pageable.HybridTimeline())

    fun localTimeline(title: String) =
        Page(account?.accountId?: - 1, title, 0, Pageable.LocalTimeline())

    fun homeTimeline(title: String) = Page(account?.accountId?: - 1, title, 0, Pageable.HomeTimeline())

    fun userListTimeline(listId: String) = Pageable.UserListTimeline(listId = listId)

    fun userListTimeline(userList: UserListDTO): Page{
        return Page(account?.accountId?: - 1, userList.name, 0,  Pageable.UserListTimeline(userList.id))
    }
    fun mention(title: String): Page{
        return Page(account?.accountId?: - 1, title, 0, Pageable.Mention(null))
    }

    fun show(noteId: String, title: String): Page{
        return Page(account?.accountId?: - 1, title, 0, Pageable.Show(noteId))
    }
    fun tag(tag: String): Page{
        return Page(account?.accountId?: - 1, tag, 0, Pageable.SearchByTag(tag.replace("#", "")))
    }
    fun search(query: String): Page{
        return Page(account?.accountId?: - 1, query, 0, Pageable.Search(query))
    }
    fun featured(title: String) = Page(account?.accountId?: - 1, title, 0, Pageable.Featured(null))
    fun notification(title: String) = Page(account?.accountId?: - 1, title, 0, Pageable.Notification())
    fun user(userId: String, title: String): Page{
        return Page(account?.accountId?: - 1, title, 0, Pageable.UserTimeline(userId))
    }
    fun user(user: UserDTO, isUserNameDefault: Boolean): Page{
        val title = if(isUserNameDefault) user.getShortDisplayName() else user.getDisplayName()
        return Page(account?.accountId?: - 1, title, 0, Pageable.UserTimeline(userId = user.id))
    }
    fun favorite(title: String): Page{
        return Page(account?.accountId?: - 1, title, 0, Pageable.Favorite)
    }
    fun antenna(antennaId: String, title: String): Page{
        return Page(account?.accountId?: - 1, title, 0, Pageable.Antenna(antennaId))
    }
    fun antenna(antenna: Antenna): Page{
        return Page(antenna.id.accountId, antenna.name, 0, Pageable.Antenna(antenna.id.antennaId))
    }



}

fun Account.newPage(pageable: Pageable, name: String): Page {
    return Page(this.accountId, name, 0, pageable)
}