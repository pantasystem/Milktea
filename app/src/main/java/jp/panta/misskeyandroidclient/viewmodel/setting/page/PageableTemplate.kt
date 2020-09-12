package jp.panta.misskeyandroidclient.viewmodel.setting.page

import android.content.Context
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.PageType
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.view.settings.page.PageTypeNameMap

class PageableTemplate(val account: Account) {
    fun globalTimeline(title: String): Page{
        return Page(account.accountId, title, 0, Pageable.GlobalTimeline())
    }
    fun hybridTimeline(title: String) =
        Page(account.accountId, title, 0, Pageable.HybridTimeline())

    fun localTimeline(title: String) =
        Page(account.accountId, title, 0, Pageable.LocalTimeline())

    fun homeTimeline(title: String) = Page(account.accountId, title, 0, Pageable.HomeTimeline())

    fun userListTimeline(listId: String) = Pageable.UserListTimeline(listId = listId)

    fun userListTimeline(userList: UserList): Page{
        return Page(account.accountId, userList.name, 0,  Pageable.UserListTimeline(userList.id))
    }
    fun mention(title: String): Page{
        return Page(account.accountId, title, 0, Pageable.Mention(null))
    }

    fun show(noteId: String, title: String): Page{
        return Page(account.accountId, title, 0, Pageable.Show(noteId))
    }
    fun tag(tag: String): Page{
        return Page(account.accountId, tag, 0, Pageable.SearchByTag(tag.replace("#", "")))
    }
    fun search(query: String): Page{
        return Page(account.accountId, query, 0, Pageable.Search(query))
    }
    fun featured(title: String) = Page(account.accountId, title, 0, Pageable.Featured(null))
    fun notification(title: String) = Page(account.accountId, title, 0, Pageable.Notification())
    fun user(userId: String, title: String): Page{
        return Page(account.accountId, title, 0, Pageable.UserTimeline(userId))
    }
    fun user(user: User, isUserNameDefault: Boolean): Page{
        val title = if(isUserNameDefault) user.getShortDisplayName() else user.getDisplayName()
        return Page(account.accountId, title, 0, Pageable.UserTimeline(userId = user.id))
    }
    fun favorite(title: String): Page{
        return Page(account.accountId, title, 0, Pageable.Favorite)
    }
    fun antenna(antennaId: String, title: String): Page{
        return Page(account.accountId, title, 0, Pageable.Antenna(antennaId))
    }
    fun antenna(antenna: Antenna): Page{
        return Page(account.accountId, antenna.name, 0, Pageable.Antenna(antenna.id))
    }



}

