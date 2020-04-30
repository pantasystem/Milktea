package jp.panta.misskeyandroidclient.viewmodel.setting.page

import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna

object PageableTemplate {
    fun globalTimeline(title: String): Page{
        return Page(null, title, null, globalTimeline = Page.GlobalTimeline())
    }
    fun hybridTimeline(title: String) =
        Page(null, title, null, hybridTimeline = Page.HybridTimeline())

    fun localTimeline(title: String) =
        Page(null, title, null, localTimeline = Page.LocalTimeline())

    fun homeTimeline(title: String) = Page(null, title, null, homeTimeline = Page.HomeTimeline())
    fun userListTimeline(listId: String): Page.UserListTimeline{
        return Page.UserListTimeline(listId)
    }

    fun userListTimeline(userList: UserList): Page{
        return Page(null, userList.name, null, userListTimeline = Page.UserListTimeline(userList.id))
    }
    fun mention(title: String): Page{
        return Page(null, title, null, mention = Page.Mention(null))
    }

    fun show(noteId: String, title: String): Page{
        return Page(null, title, null, show = Page.Show(noteId))
    }
    fun tag(tag: String): Page{
        return Page(null, tag, null, searchByTag = Page.SearchByTag(tag.replace("#", "")))
    }
    fun search(query: String): Page{
        return Page(null, query, null, search = Page.Search(query))
    }
    fun featured(title: String) = Page(null, title, null, featured = Page.Featured(null))
    fun notification(title: String) = Page(null, title, null, notification = Page.Notification())
    fun user(userId: String, title: String): Page{
        return Page(null, title, null, userTimeline = Page.UserTimeline(userId))
    }
    fun user(user: User, isUserNameDefault: Boolean): Page{
        val title = if(isUserNameDefault) user.getShortDisplayName() else user.getDisplayName()
        return Page(null, title, null, userTimeline = Page.UserTimeline(userId = user.id))
    }
    fun favorite(title: String): Page{
        return Page(null, title, null, favorite = Page.Favorite())
    }
    fun antenna(antennaId: String, title: String): Page{
        return Page(null, title, null, antenna = Page.Antenna(antennaId))
    }
    fun antenna(antenna: Antenna): Page{
        return Page(null, antenna.name, null, antenna = Page.Antenna(antenna.id))
    }

}

