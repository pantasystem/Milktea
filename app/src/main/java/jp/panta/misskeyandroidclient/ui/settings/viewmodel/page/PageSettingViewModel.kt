package jp.panta.misskeyandroidclient.ui.settings.viewmodel.page

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.MiApplication
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.data.model.settings.SettingStore
import net.pantasystem.milktea.data.api.misskey.users.RequestUser
import net.pantasystem.milktea.data.api.misskey.users.UserDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.user.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.ui.settings.page.PageTypeNameMap
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.model.account.page.PageableTemplate
import java.lang.IllegalStateException

class PageSettingViewModel(
    val miCore: MiCore,
    val settingStore: SettingStore,
    private val pageTypeNameMap: PageTypeNameMap
) : ViewModel(), SelectPageTypeToAdd, PageSettingAction {

    class Factory(val miApplication: MiApplication) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PageSettingViewModel(
                miApplication,
                miApplication.getSettingStore(),
                PageTypeNameMap(miApplication)
            ) as T
        }
    }

    val encryption = miCore.getEncryption()

    val selectedPages = MediatorLiveData<List<net.pantasystem.milktea.model.account.page.Page>>()

    var account: net.pantasystem.milktea.model.account.Account? = null
        get() = miCore.getAccountStore().currentAccount
        private set


    val pageAddedEvent = EventBus<net.pantasystem.milktea.model.account.page.PageType>()

    val pageOnActionEvent = EventBus<net.pantasystem.milktea.model.account.page.Page>()

    val pageOnUpdateEvent = EventBus<net.pantasystem.milktea.model.account.page.Page>()

    init {

        miCore.getAccountStore().observeCurrentAccount.filterNotNull().onEach {
            account = it
            selectedPages.postValue(
                it.pages.sortedBy { p ->
                    p.weight
                }
            )
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun setList(pages: List<net.pantasystem.milktea.model.account.page.Page>) {
        selectedPages.value = pages.mapIndexed { index, page ->
            page.apply {
                weight = index + 1
            }
        }
    }

    fun save() {
        val list = selectedPages.value ?: emptyList()
        list.forEachIndexed { index, page ->
            page.weight = index + 1
        }
        Log.d("PageSettingVM", "pages:$list")
        viewModelScope.launch(Dispatchers.IO) {
            miCore.getAccountStore().replaceAllPage(list).onFailure {
                Log.e("PageSettingVM", "保存失敗", it)
            }
        }
    }

    fun updatePage(page: net.pantasystem.milktea.model.account.page.Page) {
        val pages = selectedPages.value?.let {
            ArrayList(it)
        } ?: return

        var pageIndex = pages.indexOfFirst {
            it.pageId == page.pageId && it.pageId > 0
        }
        if (pageIndex < 0) {
            pageIndex = pages.indexOfFirst {
                it.weight == page.weight && page.weight > 0
            }
        }
        if (pageIndex >= 0 && pageIndex < pages.size) {
            pages[pageIndex] = page.copy()
        }

        setList(pages)

    }

    private fun addPage(page: net.pantasystem.milktea.model.account.page.Page) {
        val list = ArrayList<net.pantasystem.milktea.model.account.page.Page>(selectedPages.value ?: emptyList())
        page.weight = list.size
        list.add(page)
        setList(list)
    }

    fun addUserPageById(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                miCore.getMisskeyAPIProvider().get(account!!).showUser(
                    RequestUser(userId = userId, i = account?.getI(encryption))
                ).throwIfHasError().body() ?: throw IllegalStateException()
            }.onSuccess {
                addUserPage(it)
            }.onFailure { t ->
                Log.e("PageSettingVM", "ユーザーの取得に失敗した", t)
            }

        }
    }

    private fun addUserPage(user: UserDTO) {
        val page = if (settingStore.isUserNameDefault) {
            net.pantasystem.milktea.model.account.page.PageableTemplate(account!!)
                .user(user.id, title = user.getShortDisplayName())
        } else {
            net.pantasystem.milktea.model.account.page.PageableTemplate(account!!)
                .user(user.id, title = user.getDisplayName())
        }
        addPage(page)
    }

    fun addUsersGalleryById(userId: String) {
        val pageable = net.pantasystem.milktea.model.account.page.Pageable.Gallery.User(userId = userId)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val user = miCore.getUserRepository()
                    .find(net.pantasystem.milktea.model.user.User.Id(accountId = account!!.accountId, id = userId))
                val name =
                    if (settingStore.isUserNameDefault) user.getShortDisplayName() else user.getDisplayName()
                val page = account!!.newPage(pageable, name = name)
                addPage(page)
            }
        }
    }

    fun removePage(page: net.pantasystem.milktea.model.account.page.Page) {
        val list = ArrayList<net.pantasystem.milktea.model.account.page.Page>(selectedPages.value ?: emptyList())
        list.remove(page)
        setList(list)
    }


    override fun add(type: net.pantasystem.milktea.model.account.page.PageType) {
        pageAddedEvent.event = type
        val name = pageTypeNameMap.get(type)
        when (type) {
            net.pantasystem.milktea.model.account.page.PageType.GLOBAL -> {
                addPage(net.pantasystem.milktea.model.account.page.PageableTemplate(account!!).globalTimeline(name))
            }
            net.pantasystem.milktea.model.account.page.PageType.SOCIAL -> {
                addPage(net.pantasystem.milktea.model.account.page.PageableTemplate(account!!).hybridTimeline(name))
            }
            net.pantasystem.milktea.model.account.page.PageType.LOCAL -> {
                addPage(net.pantasystem.milktea.model.account.page.PageableTemplate(account!!).localTimeline(name))
            }
            net.pantasystem.milktea.model.account.page.PageType.HOME -> {
                addPage(net.pantasystem.milktea.model.account.page.PageableTemplate(account!!).homeTimeline(name))
            }
            net.pantasystem.milktea.model.account.page.PageType.NOTIFICATION -> {
                addPage(net.pantasystem.milktea.model.account.page.PageableTemplate(account!!).notification(name))
            }
            net.pantasystem.milktea.model.account.page.PageType.FAVORITE -> {
                addPage(net.pantasystem.milktea.model.account.page.PageableTemplate(account!!).favorite(name))
            }
            net.pantasystem.milktea.model.account.page.PageType.FEATURED -> {
                addPage(net.pantasystem.milktea.model.account.page.PageableTemplate(account!!).featured(name))
            }
            net.pantasystem.milktea.model.account.page.PageType.MENTION -> {
                addPage(net.pantasystem.milktea.model.account.page.PageableTemplate(account!!).mention(name))
            }
            net.pantasystem.milktea.model.account.page.PageType.GALLERY_FEATURED -> addPage(account!!.newPage(
                net.pantasystem.milktea.model.account.page.Pageable.Gallery.Featured, name))
            net.pantasystem.milktea.model.account.page.PageType.GALLERY_POPULAR -> addPage(account!!.newPage(
                net.pantasystem.milktea.model.account.page.Pageable.Gallery.Popular, name))
            net.pantasystem.milktea.model.account.page.PageType.GALLERY_POSTS -> addPage(account!!.newPage(
                net.pantasystem.milktea.model.account.page.Pageable.Gallery.Posts, name))
            net.pantasystem.milktea.model.account.page.PageType.MY_GALLERY_POSTS -> addPage(account!!.newPage(
                net.pantasystem.milktea.model.account.page.Pageable.Gallery.MyPosts, name))
            net.pantasystem.milktea.model.account.page.PageType.I_LIKED_GALLERY_POSTS -> addPage(
                account!!.newPage(
                    net.pantasystem.milktea.model.account.page.Pageable.Gallery.ILikedPosts,
                    name
                )
            )
            else -> {
                Log.d("PageSettingViewModel", "管轄外な設定パターン:$type, name:$name")
            }
        }
    }

    override fun action(page: net.pantasystem.milktea.model.account.page.Page?) {
        page ?: return
        pageOnActionEvent.event = page
    }

}